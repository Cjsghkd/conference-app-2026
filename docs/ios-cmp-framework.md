# CMP on iOS (framework)

The entire `KaigiApp` runs on iOS via `ComposeUIViewController`; every screen is `commonMain`, so no iOS-specific UI code is needed. `app-shared` is compiled into `AppShared.framework`, and the full stack — Metro, Soil, both Navigation3 groups, runtime-retain, context parameters, the custom `:tools:compiler-plugin` — links as native klibs (`linkDebugFrameworkIosSimulatorArm64`; targets are iosArm64 + iosSimulatorArm64).

## Embedding

The Kotlin side (`app-shared/src/iosMain`) exposes a graph factory and a `UIViewController` factory that receives the graph — the iOS host owns the graph's lifetime (`createGraph<…>()` is inline + reified, so Swift cannot call it directly). The bridge between the back stack and the native tab bar needs no iOS-specific wiring: `KaigiApp` runs `IosTabBarSyncEffect` internally against the app-scoped `RootTabNavigator` ([Root tab bar](./navigation-root-tab-bar.md)):

```kotlin
// app-shared/src/iosMain/…/IosAppGraph.kt
fun createIosAppGraph(): IosAppGraph = createGraph<IosAppGraph>()

// app-shared/src/iosMain/…/KaigiAppViewController.ios.kt
fun kaigiAppViewController(appGraph: IosAppGraph): UIViewController = ComposeUIViewController {
    context(appGraph) {
        KaigiApp()
    }
}
```

The Swift side owns the graph in a singleton, `KaigiAppGraphOwner` (modeled on the 2025 app's `Container.shared`), so native code anywhere — the Liquid Glass tab bar, deep-link handling — reaches the same graph the Compose UI runs on:

```swift
import AppShared

// Swift-implemented owner of the KMP app graph. Built once, accessible globally.
final class KaigiAppGraphOwner: Sendable {
    static let shared = KaigiAppGraphOwner()

    let appGraph: IosAppGraph

    private init() {
        appGraph = IosAppGraphKt.createIosAppGraph()
    }
}
```

A `UIViewControllerRepresentable` wraps the Kotlin `UIViewController` for SwiftUI (top-level Kotlin functions surface as a generated `…Kt` class named after the file):

```swift
import SwiftUI
import AppShared

struct CMPKaigiAppViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        KaigiAppViewController_iosKt.kaigiAppViewController(
            appGraph: KaigiAppGraphOwner.shared.appGraph,
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@main
struct KaigiIosApp: App {
    var body: some Scene {
        WindowGroup {
            CMPKaigiAppViewController()
                .ignoresSafeArea()
        }
    }
}
```

The host layers the native Liquid Glass tab bar above this view controller with transparent tab content; for the overlay shape and its requirements, see [Liquid Glass tab bar](./ios-liquid-glass.md).

Related: [iOS overview](./ios.md) · [Liquid Glass tab bar](./ios-liquid-glass.md) · [AppGraph and UiGraph](./di-app-graph.md)
