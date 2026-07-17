# Logging (Kermit)

Production logging uses [Kermit](https://github.com/touchlab/Kermit), a KMP-native logging library, bound once in common code as a single `Logger` provided through the app graph.

## Why Kermit

[Kermit](https://github.com/touchlab/Kermit) (Touchlab) is a KMP-native logging library. It ships **platform-native writers built in** and picks the right sink per platform automatically: Logcat on Android, `os_log` on iOS, SLF4J on Desktop (JVM), and `console` on Web (wasmJs).

That built-in per-platform behaviour is the reason for choosing Kermit: a single `co.touchlab.kermit.Logger` binding in common code covers every target with **no `expect`/`actual` and no per-platform provider**. Contrast this with the [ByteStore / DataStore seam](./debugging.md), which *does* need per-platform providers — the Android backing needs a `Context`, so those bindings live on each platform graph. The Logger has no such platform coupling.

## The KaigiLogger facade

Application code logs through the project-owned `KaigiLogger` interface (`:core:common`) rather than Kermit's `Logger` directly. `KermitKaigiLogger` is the single `@SingleIn(AppScope::class)` implementation, contributed with `@ContributesBinding` and exposed on [`AppGraph`](./di-app-graph.md):

```kotlin
interface KaigiLogger {
    fun debug(message: () -> String)
    fun info(message: () -> String)
    fun warn(message: () -> String)
    fun error(throwable: Throwable?, message: () -> String)
}
```

The facade keeps call sites off the Kermit API and closes the static escape hatch (`co.touchlab.kermit.Logger.w { … }` bypasses injection entirely). `AppNavigator` (`:core:common`) is the first real call site — it logs each navigation command:

```kotlin
class AppNavigator(private val logger: KaigiLogger) : Navigator {
    fun goTo(key: NavKey) {
        logger.debug { "goTo $key" }
        // …
    }
}
```

## Crash reporting

`KaigiLogger.error` additionally forwards to a `CrashReporter` (`:core:common`). The default binding (`CrashReporterDefaults`) reports nowhere. Android and iOS replace it with Firebase Crashlytics implementations written entirely in Kotlin (app-shared `androidMain` / `iosMain`). On Android the SDK is a plain Gradle dependency; on iOS it is declared through the `swiftPMDependencies` Swift Package Manager import, and the generated cinterop exposes `FIRApp`/`FIRCrashlytics` to `iosMain`. Both reporters stay no-op until the Firebase project configuration is bundled. Only error-level logs ever leave the device; debug/info/warn are muted in production via the injected `MinLogSeverity` (the debug build replaces it with Verbose).

Related: [Debugging](./debugging.md) · [AppGraph (app-wide dependency graph)](./di-app-graph.md)
