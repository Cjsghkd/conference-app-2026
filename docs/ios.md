# iOS overview

iOS runs the shared Compose Multiplatform UI for every screen, with one native exception: the root tab bar, a SwiftUI view rendering the Liquid Glass design.

- The Swift implementation is minimal and the app runs on a Compose Multiplatform base. `KaigiApp` runs on a `ComposeUIViewController`, and every screen uses the shared CMP UI. Per-screen SwiftUI with KMP Presenter integration is not carried forward.
- The one native exception is the root tab bar, a SwiftUI view layered over that view controller; every screen — including screen-transition chrome — is drawn by CMP. Navigation3 owns the back stack across all platforms, and iOS mirrors the tab-related part of that state into the native bar.

## Relationship to navigation

Navigation3 owns the back stack on all four platforms. iOS reflects that state in the native tab bar through `RootTabNavigator`, a model in `:app-shared` free of UI types: Kotlin publishes the current tab (`null` hides the bar), and native tab taps come back as selections that turn into the same back-stack command the Compose bar issues on the other platforms.

## Swift ↔ Kotlin interop

The Swift ↔ Kotlin boundary stays small, around the tab bar: Swift calls Kotlin (`RootTabNavigator`, the view-controller factories) through `AppShared.framework`, and Kotlin reaches Apple frameworks through Swift Package Import, which is experimental as of 2026.

For details, see [Swift ↔ Kotlin interop](./ios-interop.md).

## Targets

iOS targets iosArm64 + iosSimulatorArm64. `app-shared`'s `AppShared.framework` links Metro, Soil, both Navigation3 groups, runtime-retain, context parameters, and the in-house `:tools:compiler-plugin` as native klibs, with screens staying entirely in `commonMain`. iosX64 (Intel simulator) is out of scope because CMP `compose.ui`, `runtime-retain`, and the Navigation3 groups are not published for the deprecated target.

The native Liquid Glass tab bar composites over the CMP backdrop on iOS 26. For the framework and embedding shape, see [CMP on iOS (framework)](./ios-cmp-framework.md); for the tab bar, see [Liquid Glass tab bar](./ios-liquid-glass.md).

Related: [CMP on iOS (framework)](./ios-cmp-framework.md) · [Liquid Glass tab bar](./ios-liquid-glass.md) · [Swift ↔ Kotlin interop](./ios-interop.md)
