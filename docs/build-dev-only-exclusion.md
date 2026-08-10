# Keeping dev-only code out of release

Dev-only assets and screens are excluded from production **by the shape of the dependency graph**, never by runtime `if`s. Two instances of the same principle:

## Preview assets — module boundary

`:core:preview:impl` (image binaries + resolver) is depended on **only by test / preview source sets**, plus a `compileOnly` (and `androidRuntimeClasspath`) entry so Android Studio can render previews; neither reaches the release runtime classpath, so the assets cannot ship. `:core:preview:api` (the type-safe contract, no binaries) and `:core:preview:wrapper` (which carries `:impl` as `compileOnly`) are what production code may see. Details: [Preview](./preview.md).

## The debug screen — per-platform gating

`:feature:debug` contributes its screen purely via Metro `@ContributesIntoSet`, so removing the module from a compile classpath cleanly removes the screen. Every platform **excludes it by default** and adds it back only for a build that identifies itself as a development build:

| Platform | Wiring | Included when |
| --- | --- | --- |
| Android | `"devImplementation"(project(":feature:debug"))` in `app-android` (dev/prod product flavors; dev installs alongside prod via the `.dev` id suffix) | the dev flavor is built |
| Desktop | conditional dependency on `jvmMain` in `app-desktop` | `run`, or one of Compose Hot Reload's run tasks, is among the requested Gradle tasks |
| Web | conditional dependency on `wasmJsMain` in `app-web` | `wasmJsBrowserDevelopmentRun` is among the requested Gradle tasks |
| iOS | conditional dependency on `iosMain` in `app-ios-kotlin` | Xcode exports `CONFIGURATION=Debug` to `embedSwiftExportForXcode` |

`-PincludeDebugFeature=true|false` overrides the platform default in either direction. It is the only way to put the debug screen into a desktop distributable, a web bundle, or a Gradle-driven iOS compilation — for example `./gradlew :app-ios-kotlin:compileKotlinIosSimulatorArm64 -PincludeDebugFeature=true` to compile the iOS graph with the debug feature present.

Android expresses the gate as a product flavor, which cannot be bypassed. The other three platforms have no such variant to hang the dependency on: the webpack modes, the debug and release iOS builds, and Compose Desktop's release distribution each reuse **one compilation**, so the gate has to read the requested tasks or Xcode's build configuration instead. Because neither signal is exhaustive, the fallback is exclusion — an unrecognised build loses the debug screen, which surfaces immediately in development, rather than shipping it. `droidkaigi.includeDebugFeature` in `gradle-conventions` holds that rule for all three.

Verify exclusion by inspecting the classpath, e.g. `./gradlew :app-desktop:dependencies --configuration jvmRuntimeClasspath | grep debug` (expect nothing).

Related: [Module structure](./project-structure.md) · [Debugging](./debugging.md)
