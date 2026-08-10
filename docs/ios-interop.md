# Swift ↔ Kotlin interop

Because iOS is almost full CMP with only a native tab bar, the Swift ↔ Kotlin boundary is small. Swift Export / Swift Package Import are experimental as of 2026, so the boundary keeps a fallback (Obj-C interop / SKIE) and stays small. Within it:

- **Kotlin → Swift (Swift calls Kotlin): Swift Export.** The native tab bar calls Kotlin APIs (the tab selection, the view-controller factory) through the exported `AppShared` module, which Kotlin generates as idiomatic Swift with no Obj-C header in between. `:app-ios-kotlin` names the module and flattens its own package, so its declarations import directly; everything it reaches in `:app-shared` keeps its Kotlin package under the generated `ExportedKotlinPackages` namespace, and Swift shortens that with a `typealias`.
- **Swift → Kotlin (using Apple frameworks): Swift Package Import.** Where iOS-specific Apple frameworks / SPM are needed, call them from Kotlin via Swift Package Import, keeping the implementation on the Kotlin side. Every sync resolves and builds the imported package graph; for sharing that work between `git worktree` checkouts, see [SwiftPM import cache across worktrees](./build-worktree-swiftpm-cache.md).

## What the exported surface may contain

Swift Export shapes the API, so the Kotlin it is pointed at has to be written for it. `:app-ios-kotlin` exists to hold exactly that layer, and two rules govern what may cross:

- **No Compose types.** Swift Export drops `@Composable` from the function types it bridges, so a declaration carrying one exports an API Swift cannot call correctly. The graph and the composables stay private behind plain classes.
- **No enum inside a `Flow`.** The generated flow iterator casts every element through its class bridge, which a Kotlin enum — bridged as a Swift enum, a value type — fails at runtime. Wrapping the enum in a class (`RootTabSelection`) crosses intact.

## Caveats (experimental risk)

- Both Swift Export and Swift Package Import are experimental (2026). Keep a fallback (classic Obj-C interop / SKIE) and adopt them gradually.
- coroutines/flow Swift interop is still stabilizing, so keep the state-passing boundary small.

Related: [iOS overview](./ios.md) · [CMP on iOS (embedding)](./ios-cmp-embedding.md) · [Swift export](https://kotlinlang.org/docs/native-swift-export.html)
