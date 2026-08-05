# SwiftPM import cache across worktrees

`:app-shared` declares a Swift Package Manager dependency through `swiftPMDependencies`, so every Gradle sync resolves the Firebase iOS SDK package graph and builds it with `xcodebuild`. The Kotlin Gradle plugin writes the results into three directories inside the working tree:

| Directory | Contents |
| --- | --- |
| `.swiftpm-locks/default/swiftPMCheckout` | SwiftPM checkout of the umbrella package |
| `app-shared/build/kotlin/swiftPMCheckout` | SwiftPM checkout passed to `xcodebuild` as `-clonedSourcePackagesDirPath` |
| `app-shared/build/kotlin/swiftImportDd` | `xcodebuild` derived data, passed as `-derivedDataPath` |

Together they hold roughly 3 GB. None of them is a declared Gradle output, and every SwiftPM import task carries `@DisableCachingByDefault`, so the Gradle build cache cannot carry the results from one working tree to another. Each additional `git worktree` otherwise repeats the entire resolve-and-build cycle.

## Linking a working tree to the shared store

Run the script once per working tree, before the first sync:

```sh
scripts/link-swiftpm-cache.sh
```

It replaces the three directories with symbolic links into `~/.cache/droidkaigi-conference-app-2026/swiftpm-import`. A working tree that already holds the real directories has them moved into the store; a working tree created afterwards links straight to it. Pass `--store <dir>` or export `SWIFTPM_IMPORT_CACHE` to place the store elsewhere. Re-running the script is a no-op.

## Linking every new working tree automatically

```sh
scripts/link-swiftpm-cache.sh --install-hook
```

This writes a `post-checkout` hook into the clone's shared hook directory, so every `git worktree add` from that clone links itself. Git passes the all-zero ref as the previous HEAD for `git worktree add` and `git clone` only, so ordinary branch and file checkouts skip the hook. Installing it once covers every working tree of the clone, including the ones coding agents create.

The hook is a personal setup step: hooks live outside version control, so it changes nothing for anyone else. It leaves an existing `post-checkout` hook untouched and reports the conflict instead. To honour a custom store, export `SWIFTPM_IMPORT_CACHE` from the shell that runs `git`.

`clang` and `xcodebuild` resolve symbolic links to their real paths, so the generated `.def` and `.ld` files record the store location and stay valid in every working tree. Linked working trees keep about 40 MB of build output instead of 3 GB, and `prepareKotlinIdeaImport` — the task an IDE sync runs — drops from roughly four minutes to about one.

## Constraints

- Sync one working tree at a time. `xcodebuild` does not support concurrent use of a single derived data directory, which is why the Kotlin Gradle plugin already keeps one derived data directory per SDK.
- `./gradlew clean` removes the links, not the store. Re-run the script afterwards.
- Deleting the store invalidates the `.def` files of every linked working tree. Run a sync afterwards to regenerate them.

## Remaining sync cost

About half of the remaining time is spent in `fetchUmbrellaPackageIdentifierForDefault` and `fetchSyntheticImportProjectPackages`, which ask GitHub for the current revision of each package in the graph even when the checkout is complete. That part depends on network latency and is not affected by the shared store.
