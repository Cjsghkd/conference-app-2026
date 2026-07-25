# Preview & sample assets

Compose `@Preview`s need sample data and images, but those assets must **not ship in release builds**.

## Module split (production isolation)

- `:core:preview:api` — the pure contract: a type-safe `PreviewImage` enum, the `PreviewImageResolver` interface, `LocalPreviewImageResolver` (default `null`), the `PreviewScope` marker, and `NoopPreviewImageResolver` (the `@ContributesBinding(PreviewScope)` default, which resolves nothing). No image binaries.
- `:core:preview:impl` — the image binaries (Compose Resources) and `DefaultPreviewImageResolver`, contributed with `@ContributesBinding(PreviewScope, replaces = [NoopPreviewImageResolver::class])` so it overrides the no-op default wherever `:impl` is on the classpath.
- `:core:preview:wrapper` — the Metro `PreviewGraph` (`@DependencyGraph(PreviewScope)`) and `KaigiPreviewWrapper`, the wrapper features attach to their previews.

```text
core/preview/
├─ api/src/commonMain/kotlin/.../preview/
│    PreviewImage.kt            # the type-safe enum (generated)
│    PreviewImageResolver.kt    # contract + LocalPreviewImageResolver
│    PreviewScope.kt            # Metro scope marker
│    NoopPreviewImageResolver.kt # @ContributesBinding(PreviewScope) default; resolves nothing
├─ impl/src/commonMain/
│    ├─ kotlin/.../preview/impl/
│    │    DefaultPreviewImageResolver.kt   # @ContributesBinding(PreviewScope, replaces=[Noop]); URL -> resource
│    └─ composeResources/drawable/
│         *.png                            # the image binaries
└─ wrapper/src/commonMain/kotlin/.../preview/wrapper/
     PreviewGraph.kt                  # @DependencyGraph(PreviewScope)
     KaigiPreviewWrapper.kt           # builds the resolver via createGraph<PreviewGraph>()
```

`KaigiPreviewWrapper` and `PreviewGraph` live in `:wrapper`, not `:impl` or `:api`. The wrapper takes `:core:preview:impl` as a `compileOnly` dependency: Metro aggregates the contributed `DefaultPreviewImageResolver` binding at the wrapper's compile time, while `:impl` stays off production classpaths. Kotlin/Native and wasm rely on partial linkage to tolerate the dangling reference, and `Wrap` never runs in production. Keeping the graph out of `:api` also avoids a cycle: `:api -> :impl` would clash with `:impl -> :api`.

Production depends on `:core:preview:wrapper` (through the feature convention, see below) but never on `:core:preview:impl`, so the image binaries are physically excluded from release. Only preview / test builds put `:impl` on the classpath, sharing the same sample data with screenshot tests and fake builds.

## Preview wrapper

Each `@Preview` carries `@PreviewWrapper(KaigiPreviewWrapper::class)` (both from `androidx.compose.ui.tooling.preview`). `KaigiPreviewWrapper` implements `PreviewWrapperProvider`, and its `Wrap` applies `KaigiTheme` (fixed `KaigiColorScheme.MorningMist`) and provides the `PreviewImageResolver` — created lazily via `createGraph<PreviewGraph>().previewImageResolver` — through `LocalPreviewImageResolver`, so `RemoteImage` resolves `preview://` URLs to local drawables. (`@PreviewWrapper` targets functions only, so it cannot be placed on a multi-preview meta-annotation.)

```kotlin
@PreviewWrapper(KaigiPreviewWrapper::class)
@Preview
@Composable
fun AboutScreenPreview() {
    AboutScreen(/* sample */)
}
```

Theme-sensitive previews take the other route: they omit `@PreviewWrapper` and make `MultiThemedPreviewTheme { … }` the body's top-level call (see [Multi-theme previews](#multi-theme-previews)). The `PreviewRequiresWrapperChecker` FIR checker accepts either form.

## Multi-theme previews

`@MultiThemedPreview` (in `:core:preview:api`) marks a **top-level, argument-less `@Composable`** preview body. `:tools:ksp-processor` (the second processor, alongside the [NavKey serializer](./navigation-navkey-serializers.md) one — same `kspCommonMainMetadata` wiring) expands each annotated function into **one** generated `@Preview` that takes `@PreviewParameter(KaigiSchemeProvider::class) colorScheme: KaigiColorScheme` and wraps a call to the body in `MultiThemedPreviewTheme(colorScheme)`. Because `PreviewParameterProvider` yields one render per value, the tooling produces one tile per `KaigiColorScheme` (the parameter type must be an enum, not an inline value class).

```kotlin
// hand-written (feature:sessions/commonMain)
@MultiThemedPreview
@Composable
fun TimetableScreenPreview() {
    TimetableScreen(uiState = /* sample */, onBookmarkClick = {}, /* … */)
}

// GENERATED into build/generated/ksp/metadata/commonMain — one @Preview, N themed tiles
@Preview
@Composable
public fun TimetableScreenPreviewMultiThemed(
    @PreviewParameter(provider = KaigiSchemeProvider::class) colorScheme: KaigiColorScheme,
) {
    MultiThemedPreviewTheme(colorScheme) { TimetableScreenPreview() }
}
```

The theme envelope (`MultiThemedPreviewTheme`, applying `KaigiTheme`) lives in `:core:preview:api`, not `:impl`, so the generated code — which lands in each feature's `commonMain` — depends only on production-safe modules; the preview **image resolver** stays an `:impl` (test-only) concern. The type-safe preview-image enum is generated separately — see [Preview image enum generation](./preview-image-enum.md).

## Wiring (production stays asset-free)

The `droidkaigi.convention.kmp-feature` plugin gives every feature `implementation(project(":core:preview:wrapper"))` in `commonMain`, so `KaigiPreviewWrapper` is referenceable next to each `@Preview`. The wrapper carries `:core:preview:impl` only as `compileOnly`, so the image binaries and `DefaultPreviewImageResolver` never reach production runtime classpaths; where `:impl` is absent, the Metro graph falls back to `NoopPreviewImageResolver` and previews would render blank (which never happens in production, since `Wrap` runs only on preview / screenshot classpaths).

`:core:preview:impl` still has to be on the classpath that *renders* previews, yet it is absent from `releaseRuntimeClasspath`. The non-production paths that pull it in:

- **Android Studio `@Preview` rendering** — the `kmp-feature` convention adds `"androidRuntimeClasspath"(project(":core:preview:impl"))` (and `compileOnly(project(":core:preview:impl"))` in `androidMain`) so the drawable resources are visible to the IDE preview renderer; the `kmp.compose` primitive adds `"androidRuntimeClasspath"(libs.composeUiTooling)` for `ComposeViewAdapter`. Neither configuration feeds the release runtime classpath.
- **Tests / CI** — depend on `:core:preview:impl` from a test source set (e.g. `jvmTest`). `:core:preview:wrapper`'s `PreviewWiringTest` proves `PreviewGraph` resolves the contributed `DefaultPreviewImageResolver` from there, and maps `preview://` URLs to `DrawableResource`s.

## Android Studio preview rendering

Android Studio renders these `commonMain` previews through the Android target: the `kmp.compose` primitive puts `ui-tooling` (`ComposeViewAdapter`) on the `androidRuntimeClasspath`, and the `kmp-feature` convention adds `:core:preview:impl` there so the drawable resources resolve at render time (see [Wiring](#wiring-production-stays-asset-free)). When the interactive pane cannot render a preview, building and running the app (`./gradlew :app-android:assembleDevDebug`) shows the real screens, and the Roborazzi + ComposablePreviewScanner pipeline renders previews headlessly — see [Testing overview](./testing.md).

## Screenshot tests

Roborazzi + ComposablePreviewScanner honour `@PreviewWrapper` / `@PreviewParameter` so the same previews can drive screenshot tests — see [Testing overview](./testing.md).

Related: [Testing overview](./testing.md) · [Convention plugins](./build-convention-plugins.md)
