# Version catalog

All dependency and plugin versions live in `gradle/libs.versions.toml`, the single source of truth. Aliases are **flat camelCase** (one accessor segment under `libs.`), so every dependency is reachable by typing `libs.` and fuzzy-matching its name — no nested prefixes to remember.

```toml
[versions]
kotlin = "2.4.0"
soil = "1.0.0-alpha15"
androidxDatastore = "1.3.0-alpha09"

[libraries]
soilQueryCore = { module = "com.soil-kt.soil:query-core", version.ref = "soil" }
soilReacty   = { module = "com.soil-kt.soil:reacty",     version.ref = "soil" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
metro               = { id = "dev.zacsweers.metro",               version.ref = "metro" }
```

Modules apply plugins via `alias(libs.plugins.*)` and declare dependencies via `libs.*`. Plugin versions are declared once in the catalog, so `settings.gradle.kts` no longer pins them in `pluginManagement`.

Related: [Convention plugins](./build-convention-plugins.md) · [BuildKonfig (build-time values)](./build-config-buildkonfig.md)
