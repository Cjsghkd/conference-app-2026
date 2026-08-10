# Swift ↔ Kotlin interop

Because iOS is almost full CMP with only a native tab bar, the Swift ↔ Kotlin boundary is small. Swift Export / Swift Package Import are experimental as of 2026, so the boundary keeps a fallback (Obj-C interop / SKIE) and stays small. Within it:

- **Kotlin → Swift (Swift calls Kotlin).** The native tab bar calls Kotlin APIs (`RootTabNavigator`, the view-controller factories) through `AppShared.framework`. Xcode embeds it with `embedAndSignAppleFrameworkForXcode`, so those APIs reach Swift through the framework's generated Obj-C header: a top-level function surfaces on a `…Kt` class named after its file, and a `StateFlow` surfaces as the `Kotlinx_coroutines_coreStateFlow` protocol, collected with a `Kotlinx_coroutines_coreFlowCollector`. Swift Export (Kotlin 2.2.20+) would generate idiomatic Swift without Obj-C headers and preserve module/package structure; `:app-shared` configures it, and moving the Xcode build onto it is what remains.
- **Swift → Kotlin (using Apple frameworks): Swift Package Import.** Where iOS-specific Apple frameworks / SPM are needed, call them from Kotlin via Swift Package Import, keeping the implementation on the Kotlin side. Every sync resolves and builds the imported package graph; for sharing that work between `git worktree` checkouts, see [SwiftPM import cache across worktrees](./build-worktree-swiftpm-cache.md).

## Caveats (experimental risk)

- Both Swift Export and Swift Package Import are experimental (2026). Keep a fallback (classic Obj-C interop / SKIE) and adopt them gradually.
- coroutines/flow Swift interop is still stabilizing, so keep the state-passing boundary small.

Related: [iOS overview](./ios.md) · [CMP on iOS (framework)](./ios-cmp-framework.md) · [Swift export](https://kotlinlang.org/docs/native-swift-export.html)
