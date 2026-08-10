# CMP on iOS (embedding)

The entire `KaigiApp` runs on iOS via `ComposeUIViewController`; every screen is `commonMain`, so the only iOS-specific UI is the root tab bar. The full stack — Metro, Soil, both Navigation3 groups, runtime-retain, context parameters, the custom `:tools:compiler-plugin` — links as native klibs, for iosArm64 + iosSimulatorArm64.

Xcode consumes that stack through [Swift Export](./ios-interop.md), which produces Swift sources and a static library rather than a framework. Swift Export refuses Compose types, so the exported surface lives in its own module:

| Module | Role |
| --- | --- |
| `:app-shared` | the Compose UI, the DI graph, and everything shared with the other platforms |
| `:app-ios-kotlin` | the Compose-free API Swift calls, plus the `swiftExport { }` configuration that names the exported module `AppShared` |

## Embedding

`:app-shared` exposes a graph factory and a `UIViewController` factory that receives the graph (`createGraph<…>()` is inline + reified, so Swift cannot call it directly). `:app-ios-kotlin` wraps both in one class, which keeps the graph private and gives Swift a single entry point:

```kotlin
// app-ios-kotlin/src/iosMain/…/KaigiAppHost.kt
class KaigiAppHost(swiftPackageLicensesJson: String) {
    private val graph: IosAppGraph = createIosAppGraph(swiftPackageLicensesJson)

    val currentTab: Flow<RootTabSelection?> = graph.rootTabNavigator.currentTab.map { tab ->
        tab?.let(::RootTabSelection)
    }

    fun initialize() = graph.appInitializer.initialize()
    fun selectTab(tab: RootTab) = graph.rootTabNavigator.select(tab)
    fun viewController(): UIViewController = kaigiAppViewController(graph)
}
```

The `App` struct owns the host and hands it to both the view that carries the Compose controller and the native tab bar, so one graph serves the process. Its `init` runs the app initializer before the first composition ([AppGraph and UiGraph](./di-app-graph.md)). A `UIViewControllerRepresentable` wraps the Kotlin `UIViewController` for SwiftUI:

```swift
import AppShared
import SwiftUI

@main
struct KaigiAppApp: App {
    private let host = KaigiAppHost(swiftPackageLicensesJson: swiftPackageLicensesJson())

    init() {
        host.initialize()
    }

    var body: some Scene {
        WindowGroup {
            ZStack(alignment: .bottom) {
                KaigiAppView(host: host)
                RootTabBarView(host: host)
            }
            .ignoresSafeArea()
        }
    }
}

private struct KaigiAppView: UIViewControllerRepresentable {
    let host: KaigiAppHost

    func makeUIViewController(context: Context) -> UIViewController { host.viewController() }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

The host layers the native Liquid Glass tab bar above this view controller with transparent tab content; for the overlay shape and its requirements, see [Liquid Glass tab bar](./ios-liquid-glass.md).

## The Xcode build

A pre-build script runs `:app-ios-kotlin:embedSwiftExportForXcode`, which compiles the exported module and drops the `.swiftmodule` directories, the bridge module maps and `libAppShared.a` into `BUILT_PRODUCTS_DIR`. The target picks them up through `SWIFT_INCLUDE_PATHS`, `LIBRARY_SEARCH_PATHS` and `-lAppShared`.

That script unsets `SWIFT_INCLUDE_PATHS` for the nested Gradle build:

```yaml
# app-ios/project.yml
script: cd "$SRCROOT/.." && env -u SWIFT_INCLUDE_PATHS ./gradlew :app-ios-kotlin:embedSwiftExportForXcode
```

Gradle runs an `xcodebuild` of its own for the generated Swift package, and that build reads the inherited value. Left set, the module maps already copied into `BUILT_PRODUCTS_DIR` by the previous build collide with the ones the nested build is compiling, and the second build onward fails with `redefinition of module 'KotlinRuntime'`.

Compose resources reach the app bundle from the same task: the Compose Gradle plugin registers `syncSwiftExportBinaryComposeResourcesForIos` against the Swift Export binary and makes `embedSwiftExportForXcode` depend on it. The binary is declared by `:app-ios-kotlin`, so that module applies the Compose Gradle plugin even though it holds no Compose code — without it the bundle ships no `compose-resources` directory and every resource lookup fails at runtime.

## Linked Swift package

Swift Package Import ([Swift ↔ Kotlin interop](./ios-interop.md)) puts the imported package graph behind a generated `app-ios/KotlinMultiplatformLinkedPackage`. Its dynamic product, `KotlinMultiplatformLinkedPackageDylib`, is what the exported Kotlin code loads at runtime through `@rpath`, so the app target must link the generated package for Xcode to embed and sign that dynamic framework:

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
