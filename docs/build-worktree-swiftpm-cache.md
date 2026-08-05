# SwiftPM import cache across worktrees

`:app-shared` declares a Swift Package Manager dependency through `swiftPMDependencies`, so every Gradle sync resolves the Firebase iOS SDK package graph and builds it with `xcodebuild`. The Kotlin Gradle plugin writes the results into three directories inside the working tree:

| Directory | Contents |
| --- | --- |
| `.swiftpm-locks/default/swiftPMCheckout` | SwiftPM checkout of the umbrella package |
| `app-shared/build/kotlin/swiftPMCheckout` | SwiftPM checkout passed to `xcodebuild` as `-clonedSourcePackagesDirPath` |
| `app-shared/build/kotlin/swiftImportDd` | `xcodebuild` derived data, passed as `-derivedDataPath` |

Together they hold roughly 3 GB. None of them is a declared Gradle output, and every SwiftPM import task carries `@DisableCachingByDefault`, so the Gradle build cache cannot carry the results from one working tree to another. Each additional `git worktree` otherwise repeats the entire resolve-and-build cycle.

## Setup

Install the hook once per clone. This is the setup to prefer, because a working tree that syncs before it is linked spends the full cost once and then holds its own copy:

```sh
scripts/link-swiftpm-cache.sh --install-hook
```

It writes a `post-checkout` hook into the clone's shared hook directory, so every `git worktree add` from that clone links itself — including the working trees coding agents create. Git passes the all-zero ref as the previous HEAD for `git worktree add` and `git clone` only, so ordinary branch and file checkouts skip the hook, and the hook always exits successfully so a failure cannot fail the checkout. An existing `post-checkout` hook is left untouched and reported instead.

The hook only reaches working trees created after it. Link each one that already exists:

```sh
scripts/link-swiftpm-cache.sh
```

Either path replaces the three directories with symbolic links into `~/.cache/droidkaigi-conference-app-2026/swiftpm-import`. A working tree that already holds the real directories has them moved into the store; one created afterwards links straight to it. Re-running the script is a no-op, and it refuses to overwrite existing output rather than discarding it.

Pass `--store <dir>` to place the store elsewhere; export `SWIFTPM_IMPORT_CACHE` instead to have the hook honour it too, since the hook takes no arguments.

`clang` and `xcodebuild` resolve symbolic links to their real paths, so the generated `.def` and `.ld` files record the store location and stay valid in every working tree. Linked working trees keep about 40 MB of build output instead of 3 GB, and `prepareKotlinIdeaImport` — the task an IDE sync runs — drops from roughly four minutes to about one.

## Constraints

- Sync one working tree at a time. `xcodebuild` does not support concurrent use of a single derived data directory, which is why the Kotlin Gradle plugin already keeps one derived data directory per SDK.
- `./gradlew clean` removes the links, not the store. Re-run the script afterwards.
- Deleting the store invalidates the `.def` files of every linked working tree. Run a sync afterwards to regenerate them.

## Remaining sync cost

About half of the remaining time is spent in `fetchUmbrellaPackageIdentifierForDefault` and `fetchSyntheticImportProjectPackages`, which ask GitHub for the current revision of each package in the graph even when the checkout is complete. That part depends on network latency and is not affected by the shared store.
