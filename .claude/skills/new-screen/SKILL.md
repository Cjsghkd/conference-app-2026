---
name: new-screen
description: Scaffold every file a new screen needs (ScreenContext, PresenterContext, ScreenRoot, Screen, Contract, Presenter, NavKey, NavEntryProvider, ScreenNavigator, per-screen Graph, scope marker, Default navigator) across core:model / feature / app-shared. Use when the user asks to add or scaffold a new screen or feature module. Args: feature module name (lowercase) and screen name (PascalCase).
---

# new-screen

Scaffolds a complete screen following docs/building-a-screen.md.

## Steps

1. Determine the feature module (lowercase, e.g. `sponsors`) and the screen name (PascalCase, e.g. `Sponsors`) from the user's request. If either is ambiguous, ask.
2. Run from the repo root:

   ```
   scripts/new-screen.sh --feature <feature> --screen <ScreenName>
   ```

   The script prints a summary of created/skipped files. It never overwrites existing files. If the feature module does not exist, the script stops and asks for `--create-module`; confirm with the user that a new Gradle module is intended, then re-run with the flag (it scaffolds build.gradle.kts, the settings include, and the app-shared dependency).
3. Verify the build: `./gradlew :app-desktop:compileKotlinJvm`.
4. Relay the script's summary and next steps to the user (how to navigate to the screen, where to add Soil keys / navigator methods).

## Notes

- Generated code already complies with the FIR checkers (rememberSafeClick around navigator calls, role-context gating, @Serializable NavKey).
- The generated screen is intentionally minimal (title + back). Data reads are added by hand: Soil keys in core:model/core:data, SoilDataBoundary in the Root — see docs/building-a-screen.md.

## Completion checklist

Verify each item once the screen is fleshed out (scaffold + hand-written parts):

- [ ] Domain models (UI-independent) in `:core:model`; Key **contracts** are typealiases in `:core:model`, with ids read from the KSP-generated `SoilIds`.
- [ ] `Default*Key` impls in `:core:data`: `buildPersistedQueryKey` (persist by default — explicit `persistKey`, reified FIR-gated `@Serializable` `T`) or plain Soil `buildQueryKey`, `buildSubscriptionKey`, `buildMutationKey` (takes a `MutationTag`, bound per-screen scope). A derived read reuses a shared key via `rememberQuery(key, select)` rather than adding a new one. Move heavy shaping into `fetch`.
- [ ] `<Feature>PresenterContext : PresenterContext` (only the presenter-role dependencies).
- [ ] `<Feature>ScreenContext` (concrete `@Inject`, holds `presenterContext` by composition, not is-a).
- [ ] `Action` / `ActionResult` / `UiState` (UiState uses immutable collections).
- [ ] The presenter has only `context(presenterContext:)`, is `@Composable`, and is compute-light. Input via `ActionEffect`; `emit` from `MutationSuccessEffect`/`MutationErrorEffect`.
- [ ] The Screen renders only (never touches Soil or the channel directly). Every Compose view other than `<Feature>Screen` carries a kind suffix (`View`/`Button`/`Item`…; bare names forbidden).
- [ ] Root: `SoilDataBoundary` → `retainScreenChannel` → `ActionResultEffect` (result → `snackbarHostState.showSnackbar` / presenter-originated navigation) → `val ui = context(screenContext.presenterContext) { presenter }` (wrap only the presenter) → `Screen(ui, real-work onClick → send, navigation-only onClick = the Root's nav lambda forwarded straight through)`. A screen with no action and no one-off drops the channel entirely.
- [ ] `NavKey` (`@Serializable`, commonMain) + the per-screen `@GraphExtension` (scope marker in `:core:model`, `@Provides` the screen's `MutationTag`) + NavDisplay entry: `retain { factory.create<Feature>ScreenGraph() }` (with a NavKey argument, `retain(key) { factory.create(key.id) }`), then `context(graph.screenContext) { Root(onNavigate = safeClick { ... }) }`.
- [ ] Presenter test (`runPresenterTest`) + Screen test (Robot / Roborazzi).
