# Preview screenshot tests

Compose `@Preview`s double as screenshot tests. **ComposablePreviewScanner** discovers every `@Preview`, and **Roborazzi** renders each one through Robolectric and compares it to a recorded golden image. The pipeline runs as an **Android host (unit) test** — no device or emulator, and no other target.

[Robot scenarios](./testing-robot.md) are captured by the same task, one image per `itShould`, so the states a preview cannot reach — loading, error, and whatever a tap leads to — are covered too.

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
- adds the `androidHostTest` dependencies (`:core:testing`, `:core:preview:impl`, and the artifacts the Roborazzi plugin verifies on the module itself),
- turns on Roborazzi's **`generateComposePreviewRobolectricTests`** with the module package (derived from the project path, matching the `android { namespace }` convention):

```kotlin
// droidkaigi.primitive.screenshot-test (excerpt)
generateComposePreviewRobolectricTests {
    enable.set(true)
    packages.set(listOf(screenshotPackage))
    includePrivatePreviews.set(true)
    robolectricConfig.set(mapOf("sdk" to "[36]", "qualifiers" to "\"w360dp-h800dp-xhdpi\""))
}
```

The Roborazzi plugin generates the parameterized Robolectric test class itself (`RoborazziPreviewParameterizedTests` under `build/generated/roborazzi/`) — the project maintains no test-class template.

## Discovery

Roborazzi's default tester, `AndroidComposePreviewTester`, discovers and captures the previews; the project maintains no tester of its own. It scans `androidx.compose.ui.tooling.preview.Preview` through `AndroidComposablePreviewScanner`, which is the annotation Compose Multiplatform previews carry (see [Preview & sample assets](./preview.md)).

`ComposablePreviewScanner` is ClassGraph-based, so it scans **compiled classes** on the JVM classpath. The `androidHostTest` classpath includes the Android target's compiled output, which contains `commonMain`, so `commonMain` previews are visible without a source-set visibility workaround.

Every preview is `private`, and the scanner skips a private method unless told otherwise, so `includePrivatePreviews.set(true)` above is load-bearing: without it a module's previews scan to nothing and the generated test class fails the run with `No tests found`.

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

## One target only

Screenshots are captured on Android alone. Desktop and iOS render the same Compose code through the same Skia backend, so a second and third image of a preview restated what the Android one already showed, at the cost of a KSP-generated registry to enumerate previews off the JVM, a generated test class per module, and Roborazzi's desktop and iOS artifacts.

The shared robot/presenter tests in `commonTest` still run on desktop (`jvmTest`) and iOS (`iosSimulatorArm64Test`) — what they no longer do is capture. The Android host-test task allow-lists the Roborazzi-generated preview tests and `*RobotTest` only: the presenter tests expect a plain JVM or native environment and fail under Robolectric.

## Scope / limitations

- Web (wasmJs) is not covered: Roborazzi has no wasm artifact.
- A locale-sensitive preview records one image per locale, but the two are identical — the locale reaches Robolectric's resource configuration and not the process default that Compose Resources reads.

Related: [Preview & sample assets](./preview.md) · [Testing overview](./testing.md) · [Convention plugins](./build-convention-plugins.md)
