#!/usr/bin/env node
// Generates the module dependency graph (mermaid) from the Gradle build files and injects it into
// docs/project-structure.md between the <!-- deps:start --> / <!-- deps:end --> markers.
// Run from the app root:  node scripts/gen-module-graph.mjs
import { readFileSync, writeFileSync, readdirSync, statSync } from 'node:fs'
import { dirname, join, relative } from 'node:path'
import { fileURLToPath } from 'node:url'

const appRoot = join(dirname(fileURLToPath(import.meta.url)), '..') // conference-app-2026/
const docPath = join(appRoot, '..', 'docs', 'project-structure.md')

function findBuildFiles(dir) {
  const out = []
  for (const name of readdirSync(dir)) {
    if (name === 'build' || name === '.gradle' || name === '.git' || name === '.idea' || name === 'docs') continue
    const p = join(dir, name)
    if (statSync(p).isDirectory()) out.push(...findBuildFiles(p))
    else if (name === 'build.gradle.kts') out.push(p)
  }
  return out
}

const moduleName = (file) => {
  const rel = relative(appRoot, dirname(file))
  return rel === '' ? null : ':' + rel.split('/').join(':') // core/data -> :core:data
}

// Parse project(":...") deps, tracking the source-set context so *Test edges can be dotted.
const edges = []
for (const file of findBuildFiles(appRoot)) {
  const from = moduleName(file)
  if (!from) continue
  let ctx = ''
  for (const raw of readFileSync(file, 'utf8').split('\n')) {
    const line = raw.trim()
    if (line.startsWith('//')) continue // comment
    const ss = line.match(/(\w+(?:Main|Test))\.dependencies/)
    if (ss) ctx = ss[1]
    const m = line.match(/project\("(:[^"]+)"\)/)
    if (m && !line.includes('kspCommonMainMetadata')) {
      // Dev/debug-only edges: Android's per-variant `debugImplementation`, or the web/iOS
      // `-PincludeDebugFeature` gate. Rendered dotted + labelled since they ship only in dev builds.
      const debugOnly = /debugImplementation|includeDebugFeature/.test(line)
      edges.push({ from, to: m[1], test: /Test/.test(ctx), api: line.startsWith('api('), debugOnly })
    }
  }
}

// Dedup (prefer a main edge over a test edge); drop build-time-only modules.
const buildTime = (m) => m.startsWith(':tools') || m === ':compiler-plugin'
// Every feature wires up the same way, so the graph only shows one representative
// (`:feature:sessions`) plus `:feature:debug` (which has a slightly different shape).
// The full feature list lives in the table above the graph.
const shownFeatures = new Set([':feature:sessions', ':feature:debug'])
const hidden = (m) => m.startsWith(':feature:') && !shownFeatures.has(m)
const seen = new Map()
for (const e of edges) {
  if (buildTime(e.from) || buildTime(e.to)) continue
  if (hidden(e.from) || hidden(e.to)) continue
  const k = `${e.from}->${e.to}`
  if (!seen.has(k) || (seen.get(k).test && !e.test)) seen.set(k, e)
}
const finalEdges = [...seen.values()].sort((a, b) => (a.from + a.to).localeCompare(b.from + b.to))
// iOS has no Gradle module — the Xcode app consumes the `app-shared` framework — so add it by hand.
finalEdges.push({ from: ':app-ios', to: ':app-shared', test: false })

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
sub('features', 'Features — never depend on each other')
sub('core', 'Core')
for (const n of groups.other) L.push(`  ${id(n)}["${label(n)}"]`)
for (const e of finalEdges) {
  // api deps are transitive (consumers inherit them) — drawn thick to set them apart.
  L.push(e.test
    ? `  ${id(e.from)} -. "test / preview only" .-> ${id(e.to)}`
    : e.debugOnly
      ? `  ${id(e.from)} -. "dev/debug only" .-> ${id(e.to)}`
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
