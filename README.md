# conference-app-2026

The DroidKaigi 2026 conference app — a Compose Multiplatform application targeting **Android / iOS / Desktop (JVM) / Web (wasmJS)** from a single shared codebase.

## Running the app

```sh
# Android
./gradlew :app-android:installDevDebug

# Desktop (JVM)
./gradlew :app-desktop:run

# Web (wasmJS)
./gradlew :app-web:wasmJsBrowserDevelopmentRun
```

iOS builds through the Xcode project in `app-ios/`, which embeds the `app-shared` framework.

## Documentation

The architecture and implementation guide lives in [`docs/`](./docs/index.md), served as a VitePress site:

```sh
cd docs-site
npm install   # first time only
npm run docs:dev
```

Start with [Module structure](./docs/project-structure.md) and the [Architecture overview](./docs/architecture-overview.md).

## Verification

```sh
./gradlew :app-desktop:compileKotlinJvm :app-web:compileKotlinWasmJs :app-android:compileDevDebugKotlin :app-shared:linkDebugFrameworkIosSimulatorArm64 :feature:sessions:jvmTest
```
