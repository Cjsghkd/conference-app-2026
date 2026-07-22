# Swift ↔ Kotlin interop

Because iOS is almost full CMP with only a native tab bar, the Swift ↔ Kotlin boundary is small. Swift Export / Swift Package Import are experimental as of 2026, so the boundary keeps a fallback (Obj-C interop / SKIE) and stays small. Within it:

- **Kotlin → Swift (Swift calls Kotlin): Swift Export.** The native tab bar calls Kotlin APIs (`RootTabNavigator`, the view-controller factories) through Swift Export (Kotlin 2.2.20+), which generates idiomatic Swift without Obj-C headers and preserves module/package structure.
- **Swift → Kotlin (using Apple frameworks): Swift Package Import.** Where iOS-specific Apple frameworks / SPM are needed, call them from Kotlin via Swift Package Import, keeping the implementation on the Kotlin side.

## Caveats (experimental risk)

- Both Swift Export and Swift Package Import are experimental (2026). Keep a fallback (classic Obj-C interop / SKIE) and adopt them gradually.
- coroutines/flow Swift interop is still stabilizing, so keep the state-passing boundary small.

Related: [iOS overview](./ios.md) · [CMP on iOS (framework)](./ios-cmp-framework.md) · [Swift export](https://kotlinlang.org/docs/native-swift-export.html)
