# iOS overview

iOS runs the shared Compose Multiplatform UI for every screen, with one native exception: the navigation bar, implemented in SwiftUI with the Liquid Glass design.

- The Swift implementation is minimal and the app runs on a Compose Multiplatform base. `KaigiApp` runs on a `ComposeUIViewController`, and every screen uses the shared CMP UI. Per-screen SwiftUI with KMP Presenter integration is not carried forward.
- The one native exception is the navigation bar (tab bar and screen-transition chrome), rendered in SwiftUI with the Liquid Glass design; the screen content inside it is drawn by CMP. Navigation3 owns the back stack across all platforms, and iOS reflects that state in the native bar's appearance.

## Relationship to navigation

Navigation3 owns the back stack on all four platforms. Only iOS reflects that state in the appearance of the native Liquid Glass navigation bar, which requires a boundary that composes the native bar's appearance with the navigation state Navigation3 holds.

## Swift ↔ Kotlin interop

The Swift ↔ Kotlin boundary stays small, around the navigation bar: Swift calls Kotlin (`KaigiApp`, Nav3 state) through Swift Export, and Kotlin reaches Apple frameworks through Swift Package Import. Both are experimental as of 2026, so the boundary keeps a fallback and stays minimal.

For details, see [Swift ↔ Kotlin interop](./ios-interop.md).

## Targets

iOS targets iosArm64 + iosSimulatorArm64. `app-shared`'s `AppShared.framework` links Metro, Soil, both Navigation3 groups, runtime-retain, context parameters, and the in-house `:tools:compiler-plugin` as native klibs, with screens staying entirely in `commonMain`. iosX64 (Intel simulator) is out of scope because CMP `compose.ui`, `runtime-retain`, and the Navigation3 groups are not published for the deprecated target.

The native Liquid Glass bar composites over the CMP backdrop on iOS 26. For the framework and embedding shape, see [CMP on iOS (framework)](./ios-cmp-framework.md); for the navigation bar, see [Liquid Glass navigation bar](./ios-liquid-glass.md).

Related: [CMP on iOS (framework)](./ios-cmp-framework.md) · [Liquid Glass navigation bar](./ios-liquid-glass.md) · [Swift ↔ Kotlin interop](./ios-interop.md)
