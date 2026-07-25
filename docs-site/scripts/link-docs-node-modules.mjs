// VitePress runs Vite with srcDir (../docs) as the root, so the bare imports emitted into the
// markdown modules are resolved from docs/. Link the dependencies installed here into that
// directory; without the link, `vitepress dev` and `vitepress build` fail to resolve vue.
import { lstatSync, symlinkSync, unlinkSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const docsSiteDir = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const link = join(docsSiteDir, '..', 'docs', 'node_modules')

const existing = lstatSync(link, { throwIfNoEntry: false })
if (existing && !existing.isSymbolicLink()) {
  throw new Error(`${link} exists and is not a symlink; remove it and run npm install again`)
}
if (existing) {
  unlinkSync(link)
}

// 'junction' keeps this working on Windows without elevated privileges; POSIX ignores the type.
symlinkSync(join(docsSiteDir, 'node_modules'), link, 'junction')
