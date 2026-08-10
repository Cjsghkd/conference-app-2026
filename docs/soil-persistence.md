# Soil persistence

`buildPersistedQueryKey` persists every successful **server response** and restores it on the next launch, so persisted screens render instantly offline. The persisted payload is deliberately the server response, not the domain model: model-layer refactoring never invalidates the cache, and only a change to the server contract does.

## How it works

`buildPersistedQueryKey` is a wrapper over Soil's `buildQueryKey` with two lambdas — `fetchResponse` produces the server response (the persisted form), `transformToDomainModel` shapes it into the model (the query's `T`):

```kotlin
inline fun <T : Any, @MustBeSerializable reified RESPONSE : Any> buildPersistedQueryKey(
    id: QueryId<T>,
    persistKey: String,
    fileStorage: FileStorage,
    noinline fetchResponse: suspend QueryReceiver.() -> RESPONSE,
    noinline transformToDomainModel: (RESPONSE) -> T,
): QueryKey<T>
```

- On a successful fetch, the response is encoded with kotlinx.serialization JSON and written to the [`FileStorage`](#why-filestorage-not-datastore) under `persistKey`.
- On the next launch, Soil's `QueryKey.onPreloadData` hook decodes the stored response and re-runs `transformToDomainModel`, warming the cache before the first fetch completes.
- A payload that no longer decodes is treated as a **cache miss** (one plain refetch), and unknown JSON keys are ignored — additive server changes never invalidate the cache. Model refactoring cannot invalidate it at all, because the model never touches the persisted format.

## Compile-time gate

The serializer for `RESPONSE` is resolved **internally** from the reified type parameter rather than passed as an explicit `KSerializer` argument: the explicit argument forced callers to write `TimetableResponse.serializer()`, which the IDE renders red (it does not fully resolve the plugin-generated companion member). The reified `serializer<RESPONSE>()` lookup is not compile-checked for `@Serializable` on its own, so the gate is restored by the `MustBeSerializable` FIR checker (see [Enforcement](./enforcement.md)): a call whose `RESPONSE` lacks `@Serializable` is a compile error. The domain model needs no `@Serializable` — it is never persisted.

## Explicit persistence identity

`persistKey` is **required, with no default falling back to the runtime id**. Runtime ids may change freely — they are derived from the contract typealias FQN (`SoilIds`, see [Soil keys](./soil-keys.md)) and could be renamed or reshaped — but the persisted-cache identity must stay stable across releases, so it is named explicitly. The shared timetable query is persisted by default:

```kotlin
class DefaultTimetableQueryKey(
    private val api: TimetableApi,
    private val fileStorage: ServerEnvironmentScopedFileStorage,
) : TimetableQueryKey by buildPersistedQueryKey(
    id = SoilIds.timetableQuery,
    persistKey = "timetable", // stable, explicit persisted-cache identity
    fileStorage = fileStorage,
    fetchResponse = { api.getTimetable() }, // RESPONSE = TimetableResponse; persisted as-is
    transformToDomainModel = { response -> Timetable(items = response.toTimetableItems().toPersistentList()) },
)
```

The detail screen does not add its own persisted key — it derives its item from this cache with `rememberQuery(key, select)` (see [Soil keys](./soil-keys.md)).

## Why FileStorage, not DataStore

The payload cache is written to the project's `FileStorage` (per-key binary blobs: files on Android/desktop/iOS, IndexedDB on wasmJs) rather than DataStore Preferences. Preferences is a single map file that is loaded whole and rewritten whole — the wrong shape for independent, potentially large response blobs. On the web, DataStore's backend is `WebLocalStorage` (browser localStorage: synchronous, string-only, a roughly 5 MB quota), which suits small settings but not response payloads; IndexedDB is the browser store meant for blobs. Small settings (the theme) keep using DataStore (`ThemeStore` over `DataStore<Preferences>` — `WebLocalStorage` on wasmJs), surfaced reactively through a Soil subscription — see [Soil keys](./soil-keys.md).

Related: [Soil keys](./soil-keys.md) · [SoilDataBoundary](./soil-data-boundary.md)
