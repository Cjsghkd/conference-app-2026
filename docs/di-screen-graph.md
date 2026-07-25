# Per-screen graphs (@GraphExtension)

Alongside the [AppGraph and UiGraph](./di-app-graph.md), **each screen has its own DI graph** — a Metro `@GraphExtension`, scoped to a per-screen marker in `:core:model` (`TimetableScreenScope`, `AboutScreenScope`, …).

The graph is contributed into the [UiGraph](./di-app-graph.md) through an `@ContributesTo(UiScope)` factory, and exposes the screen's `ScreenContext` as an accessor.

```kotlin
// just a marker — a sealed interface has no constructor and nothing can implement it
sealed interface TimetableScreenScope

@GraphExtension(TimetableScreenScope::class)
interface TimetableScreenGraph {
    val screenContext: TimetableScreenContext
    val screenNavigator: TimetableScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(UiScope::class)
    fun interface Factory {
        fun createTimetableScreenGraph(): TimetableScreenGraph
    }
}
```

- **Why a graph per screen.** Screen-scoped bindings — the `ScreenContext`, a keyed query key, the screen Navigator — belong in a scope narrower than `UiScope`, so they can be `@SingleIn(<ScreenScope>)` and stay unreachable from the app and UI graphs.
- **How it is built.** The graph is created from its `Factory` — not resolved out of the UI graph — and its `@SingleIn(<ScreenScope>)` accessor hands back a stable `ScreenContext` for as long as the graph is held.

The `ScreenContext` it exposes, and the finer scoping rules, are covered in [ScreenContext design](./screen-context.md).

Related: [AppGraph and UiGraph](./di-app-graph.md) · [ScreenContext design](./screen-context.md)
