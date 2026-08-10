#!/usr/bin/env node
// Generates the module dependency graph (mermaid) from the Gradle build files and injects it into
// docs/project-structure.md between the <!-- deps:start --> / <!-- deps:end --> markers.
// Run from the repository root:  node scripts/gen-module-graph.mjs
import { readFileSync, writeFileSync, readdirSync, statSync } from 'node:fs'
import { dirname, join, relative } from 'node:path'
import { fileURLToPath } from 'node:url'

const repoRoot = join(dirname(fileURLToPath(import.meta.url)), '..')
const docPath = join(repoRoot, 'docs', 'project-structure.md')
const conventionsRoot = join(repoRoot, 'gradle-conventions', 'src', 'main', 'kotlin')

const problems = []

function findFiles(dir, matches) {
  const out = []
  for (const name of readdirSync(dir)) {
    // Dot directories hold tooling state, and .claude/worktrees holds whole checkouts of this
    // repository, whose build files would otherwise be read as modules of it.
    if (name.startsWith('.') || name === 'build' || name === 'docs') continue
    const p = join(dir, name)
    if (statSync(p).isDirectory()) out.push(...findFiles(p, matches))
    else if (matches(name)) out.push(p)
  }
  return out
}

const moduleName = (file) => {
  const rel = relative(repoRoot, dirname(file))
  return rel === '' ? null : ':' + rel.split('/').join(':') // core/data -> :core:data
}

// --- Convention plugins -------------------------------------------------------------------------
// Most of the interesting edges (the preview wiring every feature inherits) are declared in the
// convention plugins rather than in the modules themselves, so those scripts are parsed too and
// their dependencies attributed to every module that applies them.

const pluginScripts = new Map() // "droidkaigi.convention.kmp-feature" -> file path
for (const file of findFiles(conventionsRoot, (n) => n.endsWith('.gradle.kts'))) {
  const id = relative(conventionsRoot, file).replace(/\.gradle\.kts$/, '').split('/').join('.')
  pluginScripts.set(id, file)
}

const catalogAliases = new Map() // "droidkaigiConventionKmpFeature" -> plugin id
for (const line of readFileSync(join(repoRoot, 'gradle', 'libs.versions.toml'), 'utf8').split('\n')) {
  const m = line.match(/^(\w+)\s*=\s*\{[^}]*\bid\s*=\s*"([^"]+)"/)
  if (m) catalogAliases.set(m[1], m[2])
}

// Only droidkaigi plugin scripts live in this build; third-party ids carry no project dependencies.
function appliedPluginIds(text) {
  const ids = new Set()
  for (const m of text.matchAll(/alias\(libs\.plugins\.(\w+)\)/g)) {
    const id = catalogAliases.get(m[1])
    if (id?.startsWith('droidkaigi.')) ids.add(id)
  }
  for (const m of text.matchAll(/id\("(droidkaigi\.[^"]+)"\)/g)) ids.add(m[1])
  return ids
}

function scriptsFor(buildFile) {
  const files = [buildFile]
  const pending = [...appliedPluginIds(readFileSync(buildFile, 'utf8'))]
  const done = new Set()
  while (pending.length) {
    const id = pending.pop()
    if (done.has(id)) continue
    done.add(id)
    const file = pluginScripts.get(id)
    if (!file) {
      problems.push(`${relative(repoRoot, buildFile)}: applies unknown plugin id "${id}"`)
      continue
    }
    files.push(file)
    pending.push(...appliedPluginIds(readFileSync(file, 'utf8')))
  }
  return files
}

// --- Dependency parsing -------------------------------------------------------------------------
// Each Gradle configuration maps to how the edge is drawn. An unrecognised one is a hard error: a
// silently mislabelled edge is worse than no graph at all.
const QUALIFIER = {
  implementation: null,
  api: null,
  compileOnly: 'compileOnly',
  devImplementation: 'dev/debug only',
  debugImplementation: 'dev/debug only',
  androidRuntimeClasspath: 'androidRuntimeClasspath',
}
// Applied as a compiler/KSP/Gradle plugin, never a runtime dependency.
const BUILD_TIME_CONFIG = /^ksp/
const QUALIFIER_ORDER = ['compileOnly', 'androidRuntimeClasspath', 'dev/debug only']

const DEP = /(?:"(\w+)"|(\w+))\(\s*project\("(:[^"]+)"\)/g
const ADD_DEP = /add\(\s*"(\w+)"\s*,\s*project\("(:[^"]+)"\)/g

function parseDeps(file, from) {
  const found = []
  let ctx = ''
  for (const raw of readFileSync(file, 'utf8').split('\n')) {
    const line = raw.trim()
    if (line.startsWith('//')) continue
    if (/^dependencies\s*\{/.test(raw)) ctx = '' // a top-level block, not a source set
    const ss =
      line.match(/(\w+(?:Main|Test))\.dependencies/) ||
      line.match(/sourceSets\.named\("(\w+)"\)/) ||
      line.match(/val (\w+) by getting/)
    if (ss) ctx = ss[1]
    if (!line.includes('project("')) continue

    const decls = [
      ...[...line.matchAll(DEP)].map((m) => ({ config: m[1] ?? m[2], to: m[3] })),
      ...[...line.matchAll(ADD_DEP)].map((m) => ({ config: m[1], to: m[2] })),
    ]
    if (decls.length !== (line.match(/project\("/g) ?? []).length) {
      problems.push(`${relative(repoRoot, file)}: cannot read the configuration of "${line}"`)
      continue
    }
    for (const { config, to } of decls) {
      if (BUILD_TIME_CONFIG.test(config)) continue
      if (!(config in QUALIFIER)) {
        problems.push(`${relative(repoRoot, file)}: unknown configuration "${config}" on "${line}"`)
        continue
      }
      // The web/desktop/iOS apps gate the debug feature on a Gradle property rather than a variant.
      const qualifier = line.includes('includeDebugFeature') ? 'dev/debug only' : QUALIFIER[config]
      found.push({ from, to, test: /Test/.test(ctx), api: config === 'api', qualifier })
    }
  }
  return found
}

const declarations = []
for (const buildFile of findFiles(repoRoot, (n) => n === 'build.gradle.kts')) {
  const from = moduleName(buildFile)
  if (!from) continue
  for (const script of scriptsFor(buildFile)) declarations.push(...parseDeps(script, from))
}

// --- Edges --------------------------------------------------------------------------------------
const buildTime = (m) => m.startsWith(':tools')
// Every feature wires up the same way, so the graph only shows one representative
// (`:feature:sessions`) plus `:feature:debug` (which has a slightly different shape).
// The full feature list lives in the table above the graph.
const shownFeatures = new Set([':feature:sessions', ':feature:debug'])
const hidden = (m) => m.startsWith(':feature:') && !shownFeatures.has(m)

// One edge per module pair. A production declaration outranks a test-only one and supplies the
// labels; qualifiers accumulate, so the same pair declared compileOnly *and* on the Android runtime
// classpath is labelled with both.
const merged = new Map()
for (const d of declarations) {
  if (d.from === d.to) continue
  if (buildTime(d.from) || buildTime(d.to)) continue
  if (hidden(d.from) || hidden(d.to)) continue
  const key = `${d.from}->${d.to}`
  const e = merged.get(key) ?? { from: d.from, to: d.to, test: true, api: false, qualifiers: new Set() }
  if (!d.test) {
    if (e.test) { e.test = false; e.api = false; e.qualifiers.clear() }
    e.api ||= d.api
    if (d.qualifier) e.qualifiers.add(d.qualifier)
  }
  merged.set(key, e)
}
const finalEdges = [...merged.values()].sort((a, b) => (a.from + a.to).localeCompare(b.from + b.to))
// The Xcode app is not a Gradle module: it links the Swift-exported `:app-ios-kotlin`.
finalEdges.push({ from: ':app-ios', to: ':app-ios-kotlin', test: false, api: false, qualifiers: new Set() })

if (problems.length) {
  console.error('Refusing to write the graph:')
  for (const p of problems) console.error(`  ${p}`)
  process.exit(1)
}

// --- Rendering ----------------------------------------------------------------------------------
const nodes = new Set()
for (const e of finalEdges) { nodes.add(e.from); nodes.add(e.to) }
const id = (m) => m.replace(/^:/, '').replace(/[:-]/g, '_')
const label = (m) => (m === ':app-ios' ? 'app-ios (Xcode)' : m.replace(/^:/, ''))
const groupOf = (m) => m.startsWith(':app') ? 'apps' : m.startsWith(':feature') ? 'features' : m.startsWith(':core') ? 'core' : 'other'
const groups = { apps: [], features: [], core: [], other: [] }
for (const n of [...nodes].sort()) groups[groupOf(n)].push(n)

// `layout: elk` (the loader is registered in the VitePress theme) routes this graph with
// far fewer edge crossings than the default dagre layout.
const L = [
  '---',
  'config:',
  '  layout: elk',
  '  elk:',
  // NETWORK_SIMPLEX balances nodes horizontally (the default BRANDES_KOEPF leaves the
  // top row off-centre). mergeEdges is intentionally left off: bundling edges into a
  // shared trunk made `app-shared -> core:data` and `feature:debug -> core:data` merge,
  // which read as "every feature depends on core:data". Keep each edge distinct.
  '    nodePlacementStrategy: NETWORK_SIMPLEX',
  '---',
  'graph TD',
]
const sub = (key, title) => {
  if (!groups[key].length) return
  L.push(`  subgraph ${key}["${title}"]`)
  for (const n of groups[key]) L.push(`    ${id(n)}["${label(n)}"]`)
  L.push('  end')
}
sub('apps', 'Platform apps')
sub('features', 'Features')
sub('core', 'Core')
for (const n of groups.other) L.push(`  ${id(n)}["${label(n)}"]`)
for (const e of finalEdges) {
  const qualifiers = QUALIFIER_ORDER.filter((q) => e.qualifiers.has(q))
  // api deps are transitive (consumers inherit them) — drawn thick to set them apart.
  L.push(e.test
    ? `  ${id(e.from)} -. "test / preview only" .-> ${id(e.to)}`
    : qualifiers.length
      ? `  ${id(e.from)} -. "${qualifiers.join(' / ')}" .-> ${id(e.to)}`
      : e.api
        ? `  ${id(e.from)} ==> ${id(e.to)}`
        : `  ${id(e.from)} --> ${id(e.to)}`)
}
const mermaid = '```mermaid\n' + L.join('\n') + '\n```'

const doc = readFileSync(docPath, 'utf8')
const START = '<!-- deps:start -->', END = '<!-- deps:end -->'
const re = new RegExp(`${START}[\\s\\S]*?${END}`)
if (!re.test(doc)) {
  console.error(`Markers ${START} / ${END} not found in ${docPath}`)
  process.exit(1)
}
writeFileSync(docPath, doc.replace(re, `${START}\n\n${mermaid}\n\n${END}`))
console.log(`Wrote ${finalEdges.length} edges across ${nodes.size} modules.`)
