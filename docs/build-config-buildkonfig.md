# BuildKonfig (build-time values)

The `droidkaigi.primitive.buildkonfig` [convention plugin](./build-convention-plugins.md) lives in `gradle-conventions`, `:app-shared` applies it, and the About and debug screens show the real version through `BuildConfigProvider`.

## Build-time values in common code

Some values come from the build (Gradle) — the app version, build flags, environment configuration — but must be readable from common Kotlin on every platform. Android's `BuildConfig` is unavailable in KMP `commonMain`.

`com.codingfeline.buildkonfig` generates a `BuildKonfig` object in `commonMain`, so build-time state has one well-typed outlet into app code. The version fields are fed from the [version catalog](./build-version-catalog.md), keeping the build and the app on a single source.

```kotlin
// gradle-conventions/src/main/kotlin/droidkaigi/primitive/buildkonfig.gradle.kts
buildkonfig {
    packageName = "io.github.droidkaigi.confsched"
    defaultConfigs {
        buildConfigField(STRING, "versionName", libs.versions.droidkaigiApp.get())
    }
}
```

```toml
# gradle/libs.versions.toml — both the Android versionName and BuildKonfig read this
droidkaigiApp = "0.1.0"
```

## App side: read through an interface

Rather than using the generated `BuildKonfig` directly, read it through a `BuildConfigProvider` interface (in `:core:model`) so features depend only on the interface. `:app-shared` applies the plugin and contributes the implementation; the About and debug screens display the real version this way.

```kotlin
@ContributesBinding(AppScope::class)
@Inject
class DefaultBuildConfigProvider : BuildConfigProvider {
    override val versionName: String get() = BuildKonfig.versionName
}
```

## Single source of truth

- The version lives in one place in `libs.versions.toml` (`droidkaigiApp`).
- Android's `versionName` and `BuildKonfig` read the same catalog entry, so the version the OS reports and the version the app displays can never drift apart.
- Other values (the API base URL, …) can flow into `commonMain` the same way.

Related: [Convention plugins](./build-convention-plugins.md) · [Version catalog](./build-version-catalog.md)
