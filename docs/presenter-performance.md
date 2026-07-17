# Presenter performance

The Presenter is a `@Composable` function. Under that constraint, responsibilities are divided so that heavy data shaping does not stall a frame.

## The cost of shaping in a Composable presenter

Writing heavy synchronous computation (`groupBy` / `sortedBy` / `associate` / JOIN / index construction) in the body of a `@Composable` Presenter makes it **run on the main thread on every recomposition**, making one frame heavy. It surfaces as the data volume grows.

## Where each kind of computation runs

| What to do | Means | Thread it runs on / characteristics |
| --- | --- | --- |
| **Do structural shaping in the data layer from the start** (primary) | `fetch`/`transform` of [`QueryKey`](./soil-keys.md)/`SubscriptionKey` | **`Default.limitedParallelism(1)` (background) + cache + only when data changes.** Works on all platforms |
| **Light combining** | Combine in `rememberQuery(k1,k2,transform)` or in the [`SoilDataBoundary`](./soil-data-boundary.md) content | Main, but **only when a source state changes** (not every recomposition). Negligible if small |
| **Heavy combining (syncing multiple independent sources)** | Just return `combine(...)` from a `SubscriptionKey` | **Background** (SwrCache worker) + recomputed on source change + cached. **The main choice for heavy combining** |
| **Heavy combining (JOIN from a single response)** | JOIN in a derived `QueryKey`'s `fetch` | Background + cache |
| **When only heavy UI-state-dependent derivation remains** | `produceState(keys){ withContext(Dispatchers.Default){…} }` | Parallel + memoized on native, memoized only on wasm. **Fallback** |
| **(Optional, native advanced)** Offload the whole Presenter to background | Turn into a Flow with a background Molecule | Has a pitfall: CompositionLocal is not inherited/propagated. Not the default |
| **Do not do** | Use `rememberQuery(key, select)` / `transform` for perf purposes | **Main, every recomposition, not memoized** = equivalent to writing directly in the presenter. Not a perf fix |

> The rationale: `fetch`/`subscribe` run on the background SwrCache worker, while `select`/combine run on the main thread.

> **As a rule, do not write `flowOn`/`withContext(Dispatchers.Default)` inside `fetch`/`subscribe`.** `fetch`/`subscribe` already run on the SwrCache worker (background), so escaping to another context is unnecessary and obscures intent. The only exception is when **a single transform is extremely heavy and is measurably making other queries wait**.

## Boundary of the intermediate model (how far may a QueryKey return)

- **May return**: reusable, **UI-independent**, already-shaped (JOIN/sort/index done) **domain-convenience models** (e.g. `SessionWithSpeakers`).
- **Must not return**: **screen-specific UiState** tied to screen layout/screen state (selected tab, editing, loading flag) (e.g. `SessionScreenUiState`).
- Criterion: **reusable across multiple screens/tests and contains no view-state → data layer**. A shape for rendering/operating one screen → Presenter output.

## Presenter responsibility boundary

**"The Presenter does not shape data. It shapes UI state."**

- **Owns**: transient UI state (selected tab, editing, expanded), event consumption (`ActionEffect(screenChannel)`), launching mutations (`mutateAsync`), one-off output (`screenChannel.emit`), and **cheaply assembling the UiState from already-shaped data + UI state**.
- **Does not own**: heavy data shaping/JOIN/sort/index → **data layer (QueryKey)**. Loading/error boundary and Coroutine/Dispatcher management → **`SoilDataBoundary`/Soil**. Rendering, navigation policy, theme → **UI/Root**.

### Pushing shaping into the data layer removes the "drawbacks of going async"

Offloading to background with `produceState` on the Presenter side (1) makes the value `T?`, increasing `if (x == null) Loading()`, and (2) bloats the Presenter with Coroutine/Dispatcher/loading management.
**If shaping is pushed into `fetch`/`transform` (the data layer), `SoilDataBoundary` handles loading/error at the boundary and passes non-null, ready data to the content lambda**, so neither (1) nor (2) occurs. That is, the data-layer approach simultaneously satisfies "offload to background" and "keep the Presenter thin".

## Enforcing the rule (ensuring a thin Presenter)

- **No hard gate (a compiler plugin making `groupBy` etc. a compile error).** "Heaviness" depends on data volume and cannot be judged statically, so a static rule would produce many false positives (cheap usages) and misses. This differs in nature from forbidding `mutate` (binary, always wrong).
- **Do not enforce; instead provide the right path (derived QueryKey/Subscription) + review + this guideline to steer** — at most a review nudge ("consider the data layer") for `groupBy`/`sortedBy`/`associate`/`distinctBy` inside a `@Composable` presenter.

## Minimal code example

> Simplified for reading. `buildPersistedQueryKey` has no separate `transform` parameter; the shaping goes at the end of `fetch`, on the same background worker. The Root reads via `rememberQuery` under a `ScreenContext` (see [enforcement](./enforcement.md), `SoilReadConfinement`).

```kotlin
// Data layer: structural shaping in transform (background, cached, only when data changes)
val sessionsByDayKey = buildPersistedQueryKey<SessionsResponse, Map<Day, List<Session>>>(
    id = QueryId("sessionsByDay"),
    fetch = { httpClient.get(".../sessions").body() },
    transform = { res -> res.toSessions().groupBy { it.day } },  // heavy shaping goes here
)

// Heavy combining: just return combine from a SubscriptionKey (background, recomputed on source change, cached)
//    flowOn is unnecessary — subscribe is already collected on the SwrCache worker (background)
class SessionWithSpeakersKey : SubscriptionKey<List<SessionWithSpeakers>> by buildSubscriptionKey(
    id = SubscriptionId("sessionWithSpeakers"),
    subscribe = {
        combine(sessionsFlow, speakersFlow) { sessions, speakers ->
            sessions.map { it.withSpeakers(speakers) }  // heavy JOIN
        }
    },
)

// Presenter: just reads already-shaped data. Only cheap UI selection
@Composable
fun timetableScreenPresenter(/* context: TimetablePresenterContext */): TimetableUiState {
    val byDay = soil.query(sessionsByDayKey)        // already shaped
    var selectedDay by remember { mutableStateOf(Day.Day1) }
    return TimetableUiState(
        day = selectedDay,
        sessions = byDay[selectedDay].orEmpty(),     // just looks up a bucket (cheap)
    )
}
```

Related: [Architecture overview](./architecture-overview.md) · [Soil keys](./soil-keys.md) · [SoilDataBoundary](./soil-data-boundary.md) · [Building a screen](./building-a-screen.md)
