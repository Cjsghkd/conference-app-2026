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

The `App` struct owns the graph and hands it to the view that hosts the Compose controller, so one graph serves the process and the native tab bar takes it the same way. Its `init` runs the app initializer before the first composition ([AppGraph and UiGraph](./di-app-graph.md)). A `UIViewControllerRepresentable` wraps the Kotlin `UIViewController` for SwiftUI (top-level Kotlin functions surface as a generated `…Kt` class named after the file):

```swift
import AppShared
import SwiftUI

@main
struct KaigiAppApp: App {
    private let appGraph = IosAppGraphKt.createIosAppGraph()

    init() {
        appGraph.appInitializer.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                KaigiAppView(appGraph: appGraph).ignoresSafeArea()
                RootTabBar(navigator: appGraph.rootTabNavigator).ignoresSafeArea()
            }
        }
    }
}

private struct KaigiAppView: UIViewControllerRepresentable {
    let appGraph: IosAppGraph

    func makeUIViewController(context: Context) -> UIViewController {
        KaigiAppViewController_iosKt.kaigiAppViewController(appGraph: appGraph)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

The host layers the native Liquid Glass tab bar above this view controller with transparent tab content; for the overlay shape and its requirements, see [Liquid Glass tab bar](./ios-liquid-glass.md).

## Linked Swift package

Swift Package Import ([Swift ↔ Kotlin interop](./ios-interop.md)) puts the imported package graph behind a generated `app-ios/KotlinMultiplatformLinkedPackage`. Its dynamic product, `KotlinMultiplatformLinkedPackageDylib`, is what `AppShared.framework` loads at runtime through `@rpath`. The Gradle `embedAndSignAppleFrameworkForXcode` task embeds `AppShared.framework` alone, so the app target must link the generated package itself for Xcode to embed and sign that dynamic framework alongside it:

```yaml
# app-ios/project.yml
packages:
  KotlinMultiplatformLinkedPackage:
    path: KotlinMultiplatformLinkedPackage
targets:
  KaigiApp:
    dependencies:
      - package: KotlinMultiplatformLinkedPackage
        product: KotlinMultiplatformLinkedPackage
```

Without it the app builds and links, then fails at launch with `Library not loaded: @rpath/KotlinMultiplatformLinkedPackageDylib.framework/…`. The Gradle task that wires the package into an existing Xcode project (`integrateLinkagePackage`) adds the product to the target but attaches it to the Frameworks build phase only when the target already has one, so declaring the dependency in `project.yml` is what keeps the generated project correct.

Related: [iOS overview](./ios.md) · [Liquid Glass tab bar](./ios-liquid-glass.md) · [AppGraph and UiGraph](./di-app-graph.md)
