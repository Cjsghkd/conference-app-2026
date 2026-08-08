# Preview screenshot tests

Compose `@Preview`s double as screenshot tests. **ComposablePreviewScanner** discovers every `@Preview`, and **Roborazzi** renders each one through Robolectric and compares it to a recorded golden image. The pipeline runs as an **Android host (unit) test** — no device or emulator.

## How it is wired

Every feature module is covered: the `droidkaigi.convention.kmp-feature` convention applies the `droidkaigi.primitive.screenshot-test` primitive ([Convention plugins](./build-convention-plugins.md)), so a module needs no line of its own.

```kotlin
// droidkaigi.convention.kmp-feature (excerpt)
plugins {
    id("droidkaigi.primitive.kmp")
    id("droidkaigi.primitive.kmp.compose")
    id("droidkaigi.primitive.screenshot-test")
    …
}
```

The primitive owns everything the module would otherwise copy:

- applies the Roborazzi Gradle plugin (record / verify / compare tasks),
- enables `withHostTest { isIncludeAndroidResources = true }` on the KMP Android library target — the equivalent of the classic `testOptions.unitTests.isIncludeAndroidResources = true`; the source set is `androidHostTest` (not `androidUnitTest`),
- adds the `androidHostTest` dependencies (`:core:testing`, `:core:preview:impl`, and the artifacts the Roborazzi plugin verifies on the module itself) and the `commonTest` ones the generated test class needs,
- turns on Roborazzi's **`generateComposePreviewRobolectricTests`** with the module package (derived from the project path, matching the `android { namespace }` convention):

```kotlin
// droidkaigi.primitive.screenshot-test (excerpt)
generateComposePreviewRobolectricTests {
    enable.set(true)
    packages.set(listOf(screenshotPackage))
    robolectricConfig.set(mapOf("sdk" to "[34]", "qualifiers" to "\"w360dp-h800dp-xhdpi\""))
}
```

The Roborazzi plugin generates the parameterized Robolectric test class itself (`RoborazziPreviewParameterizedTests` under `build/generated/roborazzi/`) — the project maintains no test-class template.

## Discovery

Roborazzi's default tester, `AndroidComposePreviewTester`, discovers and captures the previews; the project maintains no tester of its own. It scans `androidx.compose.ui.tooling.preview.Preview` through `AndroidComposablePreviewScanner`, which is the annotation Compose Multiplatform previews carry (see [Preview & sample assets](./preview.md)).

`ComposablePreviewScanner` is ClassGraph-based, so it scans **compiled classes** on the JVM classpath. The `androidHostTest` classpath includes the Android target's compiled output, which contains `commonMain`, so `commonMain` previews are visible without a source-set visibility workaround.

## `@PreviewParameter` expansion

`TimetableScreenPreview` takes a `@PreviewParameter(KaigiSchemeProvider::class)` colour scheme (see [Preview & sample assets](./preview.md)). The scanner honours `@PreviewParameter` and expands that single `@Preview` into **one `ComposablePreview` per `KaigiColorScheme`** — five parameterized cases, producing five goldens (`…TimetableScreenPreview_0.png` … `_4.png`).

## Tasks

Each task runs across every feature module; prefix it with a project path (`:feature:sessions:…`) to scope it to one.

| Task | Purpose |
| --- | --- |
| `recordRoborazziAndroidHostTest` | Render previews and (re)write the goldens. |
| `verifyRoborazziAndroidHostTest` | Render and fail on any pixel diff against the recorded goldens. |
| `compareRoborazziAndroidHostTest` | Render, compare, and emit diff images (no build failure). |

Goldens are written to `<module>/build/outputs/roborazzi/` and are not committed, so `verify` needs a `record` run to compare against; a CI golden store is an open decision. Because previews already inject sample data and a `PreviewImageResolver`, the screenshots are deterministic and need no network — see [Preview image enum generation](./preview-image-enum.md).

## Desktop and iOS

The same previews are captured on desktop and iOS. Classpath scanning does not exist off the JVM, so `:tools:ksp-processor` generates a per-module **`PreviewRegistry`** — an object enumerating every `@Preview` function (following meta-annotations) as a composable lambda that applies the function's `@PreviewWrapper` and expands its `@PreviewParameter` across the provider's values. Registry entries are named after the function's qualified name, while the Android goldens carry Roborazzi's own id, which also names the file class (`…component.DayTabRowKt.DayTabRowPreview`), so the two sets line up by function name rather than character for character. The `screenshot-test` plugin generates a single `PreviewScreenshotTest` into `commonTest`; it calls `capturePreviews` (`:core:testing`), an expect/actual function whose desktop and iOS actuals render every registry entry through `runComposeUiTest` and capture it with Roborazzi's `roborazzi-compose-desktop` / `roborazzi-compose-ios` artifacts. The Android and wasmJs actuals are no-ops.

| Task | Output |
| --- | --- |
| `recordRoborazziJvm` | `screenshots/desktop/` in the module |
| `recordRoborazziIosSimulatorArm64` | `build/outputs/roborazzi/screenshots/ios/` |

The shared robot/presenter tests in `commonTest` also run on desktop (`jvmTest`) and iOS (`iosSimulatorArm64Test`). The Android host-test task is filtered to the Roborazzi-generated preview tests only — the shared tests expect a plain JVM or native environment and fail under Robolectric.

## Scope / limitations

- Web (wasmJs) is not covered: Roborazzi has no wasm artifact.

Related: [Preview & sample assets](./preview.md) · [Testing overview](./testing.md) · [Convention plugins](./build-convention-plugins.md)
