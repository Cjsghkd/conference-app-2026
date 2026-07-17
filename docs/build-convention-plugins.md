# Convention plugins

Build configuration is easily duplicated across modules — the same plugins, targets, and opt-ins. Spelling it out in every `build.gradle.kts` lets the configs **drift apart** and turns each change into a multi-place edit. **Convention plugins** keep that shared setup in one place, applied per module in a single line — here as **precompiled Kotlin scripts** (`*.gradle.kts` in the `gradle-conventions` included build).

## Primitives and conventions

The plugins come in two kinds:

- **Primitives** (`droidkaigi/primitive/*.gradle.kts`) — one concern each: `kmp`, `kmp.compose`, [`buildkonfig`](./build-config-buildkonfig.md), [`screenshot-test`](./testing-preview-screenshot.md), …
- **Conventions** (`droidkaigi/convention/*.gradle.kts`) — a per-group recipe that **composes primitives**: e.g. `kmp-feature` pulls in `kmp` + `kmp.compose` and adds serialization, Metro, and KSP.

The allowed dependency directions:

```text
○ module     → convention
○ module     → primitive
○ convention → primitive
○ primitive  → primitive
✗ convention → convention
✗ primitive  → convention
```

Only two directions are disallowed — **`primitive → convention`** and **`convention → convention`**: nothing below the convention layer pulls in a convention, and conventions never chain. Everything else composes downward.

## What one looks like

The plugin id is the file's path under `src/main/kotlin` with `/` and the package as prefix, so `droidkaigi/primitive/kmp.compose.gradle.kts` becomes `droidkaigi.primitive.kmp.compose`. Because the `gradle-conventions` build applies `kotlin-dsl`, **just adding the file precompiles it into a Gradle plugin** — no registration.

```kotlin
// gradle-conventions/src/main/kotlin/droidkaigi/primitive/kmp.compose.gradle.kts
package droidkaigi.primitive

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}
```

`kotlin-dsl` compiles that script into a `Plugin<Project>` wrapper that runs the script body — this is what makes it a plugin:

```kotlin
// gradle-conventions/build/…/Kmp_composePlugin.kt (generated) — id: droidkaigi.primitive.kmp.compose
package droidkaigi.primitive

class Kmp_composePlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class.forName("droidkaigi.primitive.Kmp_compose_gradle")   // the compiled script body
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)   // runs it against `target`
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
```

A convention composes primitives and adds its group's build logic:

```kotlin
// gradle-conventions/src/main/kotlin/droidkaigi/convention/kmp-feature.gradle.kts
package droidkaigi.convention

plugins {
    id("droidkaigi.primitive.kmp")
    id("droidkaigi.primitive.kmp.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("dev.zacsweers.metro")
    id("com.google.devtools.ksp")
}
// + feature-specific build logic (e.g. run :tools:ksp-processor in kspCommonMainMetadata)
```

Related: [Version catalog](./build-version-catalog.md) · [BuildKonfig (build-time values)](./build-config-buildkonfig.md)
