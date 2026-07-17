# Liquid Glass navigation bar

On iOS the only native UI is the navigation / tab bar, rendered in SwiftUI with the **Liquid Glass** design; the screen content behind it is drawn by Compose Multiplatform. Navigation logic (the back stack) is owned by Navigation3 on every platform, and iOS reflects that state into the native bar's appearance.

## Cross-renderer compositing

A SwiftUI `.glassEffect` top bar and floating tab capsule refract and tint the CMP (Skia/Metal) backdrop behind them: the SwiftUI glass samples the live Compose Metal layer, so as content scrolls the glass tracks the CMP colors underneath. A reference implementation of this cross-renderer compositing (SwiftUI glass over Compose Metal) lives in the `iosGlassSpike` spike (`conference-app-2026/iosGlassSpike/`), targeting iOS 26.

![iOS 26 Liquid Glass refracting and tinting the CMP (Skia/Metal) content behind the top bar and bottom tab capsule](./images/ios-liquid-glass-cmp-backdrop.png)

The top bar picks up the red card behind it and is tinted red, while the bottom floating tab capsule refracts the text behind it through glass. Captured on the iOS 26.2 simulator in light mode.

CMP requires `CADisableMinimumFrameDurationOnPhone=true` in `Info.plist`, or it aborts on launch at `PlistSanityCheck`.

Related: [iOS overview](./ios.md) · [Navigation](./navigation.md)
