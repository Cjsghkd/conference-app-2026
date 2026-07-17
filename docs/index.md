# DroidKaigi/conference-app-2026 Architecture Documentation

A set of documents covering the architecture and implementation policy of the DroidKaigi 2026 conference app.

## Document map

- [Module structure](./project-structure.md) … the `:core:*` / `:feature:*` / `:app-*` / tooling module groups and what each contains
- [Platforms & modules](./platforms-and-modules.md) … the four platforms and what belongs in each module
- [Architecture overview](./architecture-overview.md) … how the app works end to end, from the platform entry point to a rendered, interactive screen
- [Error handling](./error-handling.md) … the two-layer error model and how one-off events (navigation, messages) flow through Soil-derived effects and the ScreenChannel
- [Presenter performance](./presenter-performance.md) … dividing responsibilities by pushing heavy computation into the data layer
- [Enforcement](./enforcement.md) … making invalid code uncompilable via types and FIR checkers
- [Building a screen](./building-a-screen.md) … implementing one screen end to end using TimetableScreen as an example (steps and checklist)
- [ScreenContext design](./screen-context.md) … concrete class + retain, role-context separation (composition, capability gating)
- [Navigation overview](./navigation.md) … a per-screen Navigator (hand-written) + `@ContributesIntoSet` + KSP-generated [NavKey serializers](./navigation-navkey-serializers.md) for "no central editing, no missed registrations"
- [Soil mutation](./soil-mutation.md) … `mutateAsync` + `MutationSuccessEffect` + failure handling
- [BuildKonfig (build-time values)](./build-config-buildkonfig.md) … exposing build-time values (version and other build state) to common code from a single source
- [iOS overview](./ios.md) … almost full CMP with only the navigation bar in Liquid Glass
- [Logging (Kermit)](./logging.md) … a single AppScope Kermit `Logger` with KMP-native writers per platform (incl. wasmJs=console), no expect/actual

