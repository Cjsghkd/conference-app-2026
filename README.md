# DroidKaigi 2026 official app

[DroidKaigi](https://2026.droidkaigi.jp) is a conference for Android developers, now in its
twelfth year. It runs for three days, 1–3 September 2026, at Bellesalle Shibuya Garden in Tokyo.

The official app is built in the open by the community that attends it — a Compose Multiplatform
app for **Android / iOS / Desktop (JVM) / Web (wasmJs)**. Anyone is welcome to help build it —
see [Contributing](CONTRIBUTING.md).

## Features

The DroidKaigi 2026 official app offers a variety of features to enhance your conference
experience:

- **Timetable**: Browse the schedule and bookmark the sessions you want to see.
- **Profile cards**: Create a card and share it with the people you meet.
- **Event map**: Find your way around the venue.
- **Contributors**: Discover the contributors behind the app.

...and more!

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
