# Debugging

Debug-only tooling lives in a dedicated **`:feature:debug`** module so it is wired into development builds only and never ships in production. Keeping it in its own feature module means the rest of the app has no dependency on debug code, and the tooling can use the same DI / navigation seams as a normal feature. The module is not depended on unconditionally: Android wires it as `devImplementation` (the dev product flavor only), desktop and web gate it on the `-PincludeDebugFeature` Gradle property (default on for local dev), and iOS defaults that property from Xcode's build configuration. Because the debug screen contributes to the graph purely through Metro aggregation, dropping the dependency drops the screen with no other code changes. For the per-platform wiring and how to verify the exclusion, see [Keeping dev-only code out of release](./build-dev-only-exclusion.md).

The `DebugScreen` is wired like any other screen (`DebugNavKey` + `DebugScreenContext` + `DebugScreenGraph` + `DebugNavEntryProvider`). It shows the real app version via `BuildConfigProvider` and offers a **Clear persisted data** action; the remaining rows (feature flags, network log) are placeholders. Production logging is [Kermit-based](./logging.md). Network log and jetwhale integration are planned.

## Clear persisted data

The menu's **Clear persisted data** button wipes the app's persisted (and in-memory session) state in one call, so a tester can return the app to a clean slate. It is aggregated by `PersistedDataResetter` (`:core:data`, `AppScope`):

- **Preferences DataStore** (theme) — `ThemeStore.clear()` (`dataStore.edit { it.clear() }`, clearing the whole preferences file).
- **Favorites** — `FavoritesStore.clear()` (currently an in-memory stand-in).
- **Blobs** (e.g. the profile image) — a new `ByteStore.clear()`, implemented per platform: delete the blob directory on JVM/Android/iOS, and `IDBObjectStore.clear()` on the Web (wasmJs) IndexedDB actual.

`DebugScreenContext` holds the `PersistedDataResetter`, and the data-light `DebugScreenRoot` runs `clearAll()` on a local coroutine scope (a debug-only side effect, so it does not go through the mutation/presenter path), showing a "cleared ✓" confirmation.

Related: [Keeping dev-only code out of release](./build-dev-only-exclusion.md) · [Logging (Kermit)](./logging.md)
