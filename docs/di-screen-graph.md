# Per-screen graphs (@GraphExtension)

Alongside the app-wide [AppGraph](./di-app-graph.md), **each screen has its own DI graph** — a Metro `@GraphExtension`, scoped to a per-screen marker in `:core:model` (`TimetableScreenScope`, `AboutScreenScope`, …).

The graph is contributed into the app graph through an `@ContributesTo(AppScope)` factory, and exposes the screen's `ScreenContext` as an accessor.

```kotlin
// just a marker — a sealed interface has no constructor and nothing can implement it
sealed interface TimetableScreenScope

@GraphExtension(TimetableScreenScope::class)
interface TimetableScreenGraph {
    val screenContext: TimetableScreenContext
    val screenNavigator: TimetableScreenNavigator

    @GraphExtension.Factory
    @ContributesTo(AppScope::class)
    fun interface Factory {
        fun createTimetableScreenGraph(): TimetableScreenGraph
    }
}
```

- **Why a graph per screen.** Screen-scoped bindings — the `ScreenContext`, a keyed query key, the screen Navigator — belong in a scope narrower than `AppScope`, so they can be `@SingleIn(<ScreenScope>)` and stay unreachable from the app graph.
- **How it is built.** The graph is created from its `Factory` — not resolved out of the app graph — and its `@SingleIn(<ScreenScope>)` accessor hands back a stable `ScreenContext` for as long as the graph is held.

The `ScreenContext` it exposes, and the finer scoping rules, are covered in [ScreenContext design](./screen-context.md).

Related: [AppGraph (app-wide dependency graph)](./di-app-graph.md) · [ScreenContext design](./screen-context.md)
