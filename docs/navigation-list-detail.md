# List-detail scenes (ListDetailSceneStrategy)

On EXPANDED windows — 840dp wide and up — the session screens render as **list-detail**: the timetable stays visible as the list pane while the session detail opens beside it. A MEDIUM window is one pane, because the Material-recommended directive splits the layout horizontally only from the expanded width breakpoint. This is the standard Material3 adaptive strategy, `org.jetbrains.compose.material3.adaptive:adaptive-navigation3`, passed to `NavDisplay`'s `sceneStrategies`:

```kotlin
val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()

// rootSceneStrategy is FIRST: see "Ordering" below.
sceneStrategies = listOf(rootSceneStrategy, listDetailSceneStrategy, SinglePaneSceneStrategy())
```

The library resolves for every target this app ships: android, jvm (desktop), iosArm64, iosSimulatorArm64, and wasmJs.

## Declaring the panes via entry metadata

To use the strategy, give each `NavEntry` the metadata for the pane it plays — `listPane()` on the list entry, `detailPane()` on the detail entry. The strategy groups adjacent entries that share a scene key into one `ListDetailPaneScaffold`:

```kotlin
// Timetable (the list). It already carries RootSceneStrategy.root() for the home-root predictive-back
// behavior; listPane() is merged in so the same entry is also the list pane.
entry<TimetableNavKey>(metadata = RootSceneStrategy.root() + ListDetailSceneStrategy.listPane()) { ... }

// TimetableItemDetail (the detail).
entry<TimetableItemDetailNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { ... }
```

`ListDetailSceneStrategy.listPane()` / `.detailPane()` return `Map<String, Any>` metadata, so they compose with the existing `RootSceneStrategy.root()` map with a plain `+`.

Window adaptivity is the library's own concern: `rememberListDetailSceneStrategy` reads the window size internally, collapses to a single pane on compact windows, and then returns `null` so the entry falls through to `SinglePaneSceneStrategy` — no window measurement is needed anywhere in this codebase.

## Strategy order matters

The Timetable entry carries both `RootSceneStrategy.root()` and `listPane()`, so whichever strategy comes first claims it. `rootSceneStrategy` is placed before `listDetailSceneStrategy`: with Timetable on top, back still exits the app ([`RootSceneStrategy`](./navigation-predictive-back-tabs.md)); with a detail on top, it yields and the two-pane scaffold forms. Reversed, the library claims a lone list entry on wide windows (a single-list scene with a detail placeholder) and derives `previousEntries` from the entries beneath — back from home would reveal a stashed tab instead of exiting.

## The adaptive back icon

The same detail screen appears in two situations with different affordances:

- **Single pane** (compact window, or reached by navigation) — the top bar shows **←**: "go back".
- **Detail pane** (beside the list) — **←** reads wrongly because the list is still on screen; the natural affordance is **✕**: "close this pane".

Both perform the same action (pop the detail's `NavKey`); only the icon differs, and the screen — pure `commonMain` — must not read a window size.

The library exposes exactly the signal we need to content rendered inside its scaffold: `LocalListDetailSceneScope`, a `CompositionLocal<ListDetailSceneScope?>`. It is non-null **only** while an entry is composed inside the list-detail scaffold; because the strategy yields to single-pane rendering whenever `paneCount <= 1`, a non-null scope reliably means "I am the detail pane beside a list". The detail screen reads it directly:

```kotlin
IconButton(onClick = onBack) { // the same pop, whichever icon shows
    if (LocalListDetailSceneScope.current != null) {
        Icon(Icons.Filled.Close, contentDescription = "Close")
    } else {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}
```

So the adaptive icon comes entirely from a library-provided local; the app defines no pane primitives of its own.

Related: [Root tab bar (RootTabSceneDecorator)](./navigation-root-tab-bar.md) · [Root NavEntry emulation (RootSceneStrategy)](./navigation-predictive-back-tabs.md)
