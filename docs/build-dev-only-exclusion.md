# Keeping dev-only code out of release

Dev-only assets and screens are excluded from production **by the shape of the dependency graph**, never by runtime `if`s. Two instances of the same principle:

## Preview assets — module boundary

`:core:preview:impl` (image binaries + resolver) is depended on **only by test / preview source sets**, plus a `compileOnly` (and `androidRuntimeClasspath`) entry so Android Studio can render previews; neither reaches the release runtime classpath, so the assets cannot ship. `:core:preview:api` (the type-safe contract, no binaries) and `:core:preview:wrapper` (which carries `:impl` as `compileOnly`) are what production code may see. Details: [Preview](./preview.md).

## The debug screen — per-platform gating

`:feature:debug` contributes its screen purely via Metro `@ContributesIntoSet`, so removing the module from a compile classpath cleanly removes the screen. It is wired per platform:

| Platform | Wiring | Excluded when |
| --- | --- | --- |
| Android | `"devImplementation"(project(":feature:debug"))` in `app-android` (dev/prod product flavors; dev installs alongside prod via the `.dev` id suffix) | prod flavor variants |
| Desktop | `-PincludeDebugFeature` Gradle property (default `true`) in `app-desktop` | distribution builds pass `-PincludeDebugFeature=false` |
| Web | `-PincludeDebugFeature` Gradle property (default `true`) conditionally adds the dependency to `wasmJsMain` | production bundles pass `-PincludeDebugFeature=false` |
| iOS | same property, but it **defaults from Xcode's `CONFIGURATION` env var** (`embedAndSignAppleFrameworkForXcode` exports it) | automatically on Release builds from Xcode; `-PincludeDebugFeature` overrides either way |

Web and iOS need the property because their debug/release outputs (webpack modes; debug/release frameworks) share **one compilation** — a per-buildType dependency is not expressible there.

Verify exclusion by inspecting the classpath, e.g. `./gradlew :app-android:dependencies --configuration releaseCompileClasspath | grep debug` (expect nothing).

Related: [Module structure](./project-structure.md) · [Debugging](./debugging.md)
