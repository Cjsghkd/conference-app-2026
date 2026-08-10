# Liquid Glass tab bar

On iOS the only native UI is the root tab bar: a SwiftUI view whose surface is `glassEffect`, the system's Liquid Glass material on iOS 26. Every screen behind it is drawn by Compose Multiplatform. Navigation logic (the back stack) is owned by Navigation3 on every platform, and iOS mirrors the tab-related part of that state into the native bar.

The material comes from the system modifier rather than a reimplementation, so the bar keeps the system appearance and future OS refinements. Below iOS 26 the surface falls back to `ultraThinMaterial`, which the app's iOS 16 deployment target still has to render.

## Overlay embedding

The bar is chrome only. It is bottom-aligned over the full-screen `ComposeUIViewController`, so the single Navigation3 back stack keeps owning every screen and all tab switching:

```swift
ZStack(alignment: .bottom) {
    KaigiAppView(host: host)                            // full-screen ComposeUIViewController
    RootTabBarView(                                     // only the bar's own area
        currentTab: host.currentTab.asAsyncSequence().map { $0?.tab },
        select: host.selectTab(tab:)
    )
}
.ignoresSafeArea()
```

`RootTabBarView` lays out to the bar and nothing else, so every point outside it belongs to Compose already. That is what keeps the embedding free of UIKit: no `hitTest` override, no background clearing, and no `UIViewControllerRepresentable` around the bar. A tap inside the bar is claimed by the SwiftUI button and never reaches the Compose layer; when a detail screen hides the bar, the same area returns to Compose.

The bar's geometry mirrors `KaigiNavigationBarDefaults`, which the Compose bar on the other platforms lays itself out from — 300 pt wide at most, 56 pt tall, 49 pt above the bottom edge. The Compose view controller is a sibling of the bar rather than its parent, so it does not inherit a bottom inset: root destinations reserve the room themselves, and a scrollable adds `KaigiNavigationBarDefaults.occupiedHeight` (117 dp) to its bottom content padding, which clears the bar exactly.

Scroll-driven bar behaviors are unavailable: Compose scrolling is invisible to SwiftUI, so the bar stays fully visible. The glass itself still refracts and tints the scrolling Compose content behind it.

## State bridge

`RootTab` and `RootTabNavigator` (`:app-shared`, free of UI types) form the model both sides share:

- **Kotlin → Swift**: `RootTabNavigator.currentTab: StateFlow<RootTab?>` drives the bar's selection; `null` (a non-tab entry on top, that is, a detail screen) hides the bar. Swift reaches it as `KaigiAppHost.currentTab: Flow<RootTabSelection?>`, the enum wrapped in a class because [Swift Export cannot carry an enum through a `Flow`](./ios-interop.md).
- **Swift → Kotlin**: tab taps call `select(tab)`; `IosTabBarSyncEffect` (inside `KaigiApp`) turns each selection into `AppNavigator.moveToTop(tab.key)` — the same command the Compose bar issues on the other platforms.

`RootTab.label` names each destination on both sides: the Compose bar gives it to its icon as a content description, and the SwiftUI bar as an accessibility label. The icon has no shared form — Compose names a destination with a Material `ImageVector` and Swift with an SF Symbol — so the symbol names live in the Swift bar.

`RootTabSceneDecorator` (the Compose bottom bar) is not applied on iOS; the native bar replaces it. `rememberRootTabSceneDecorator` returns `null` when `currentPlatform` is `TargetPlatform.Ios`. For the tab-switching semantics, see [Root tab bar](./navigation-root-tab-bar.md).

## Cross-renderer compositing

The Liquid Glass bar refracts and tints the CMP (Skia/Metal) backdrop behind it: the glass samples the live Compose Metal layer, so as content scrolls the glass tracks the CMP colors underneath. This compositing requires iOS 26.

![iOS 26 Liquid Glass refracting and tinting the CMP (Skia/Metal) content behind the top bar and bottom tab capsule](./images/ios-liquid-glass-cmp-backdrop.png)

The top bar picks up the red card behind it and is tinted red, while the bottom floating tab capsule refracts the text behind it through glass. Captured on the iOS 26.2 simulator in light mode.

CMP requires `CADisableMinimumFrameDurationOnPhone=true` in `Info.plist`, or it aborts on launch at `PlistSanityCheck`.

## Alternative: one Compose instance per tab

An alternative embedding gives each tab of a native `TabView` its own `ComposeUIViewController` as real content instead of the overlay. It buys native tab-switch transitions and automatic safe-area propagation, and requires:

- **Per-tab back stacks.** The single `NavBackStack` splits into one stack per tab. `AppNavigator.moveToTop` reordering — the cross-platform tab-switch model — no longer applies on iOS, and navigator commands must route to the selected tab's stack.
- **Per-tab state plumbing.** Back-stack persistence, `RetainNavEntryDecorator` scopes, and the snackbar and overlay hosts multiply per stack, and back semantics diverge from the other platforms: back no longer falls through stashed tabs, so the [`RootSceneStrategy`](./navigation-predictive-back-tabs.md) model does not carry over.
- **Deep-link routing.** A deep link resolves to a tab first, then pushes onto that tab's stack.

Scroll-driven bar behaviors remain unavailable in this embedding too — the content inside each tab is still Compose, not a native `UIScrollView`. The overlay embedding is the default because it keeps the navigation model identical across platforms and requires no change to the shared navigation code.

Related: [iOS overview](./ios.md) · [Root tab bar](./navigation-root-tab-bar.md) · [Navigation](./navigation.md)
