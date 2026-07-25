# NavEntry aggregation (NavEntryProvider)

Editing a central `NavDisplay` for every new destination invites merge conflicts, so **each feature contributes its own entry** and `NavDisplay` reads the aggregated provider.

## Feature side: contribute an entry to the set

```kotlin
interface NavEntryProvider {
    fun EntryProviderScope<NavKey>.register()
}

@ContributesIntoSet(UiScope::class)
@Inject
class TimetableNavEntryProvider(
    private val screenGraphFactory: TimetableScreenGraph.Factory, // the per-screen graph factory (contributed to UiScope)
) : NavEntryProvider {
    override fun EntryProviderScope<NavKey>.register() {
        entry<TimetableNavKey> {
            val graph = retain { screenGraphFactory.createTimetableScreenGraph() } // retain scope supplied by RetainNavEntryDecorator
            context(graph.screenContext) {
                TimetableScreenRoot(onNavigateToDetail = { graph.screenNavigator.openSessionDetail(it) })
            }
        }
    }
}
```

## App-shared side: aggregate once

```kotlin
@Inject
@SingleIn(UiScope::class)
class AppEntryProvider(providers: Set<NavEntryProvider>) {
    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
        providers.forEach { provider ->
            with(provider) {
                register()
            }
        }
    }
}
```

- Metro's `@ContributesIntoSet` aggregates across module boundaries with **zero extra configuration** (a feature's entry reaches the `UiGraph` in app-shared).
- A screen that takes an argument (detail) builds a **per-id graph** with `retain(key) { screenGraphFactory.create(key.id) }`; the `@SingleIn` ScreenContext (and, where present, the screen Navigator) are read from that retained graph ([ScreenContext](./screen-context.md)).

## KaigiApp side: NavDisplay reads the aggregated provider

```kotlin
NavDisplay(
    backStack = backStack,
    // …
    entryProvider = uiGraph.appEntryProvider.entryProvider,
)
```

Adding a screen never touches `KaigiApp` — a new feature's `NavEntryProvider` lands in the set, and `NavDisplay` picks it up through the same one line.

Related: [NavKey serializer aggregation (NavKeySerializersProvider)](./navigation-navkey-serializers.md) · [Navigator](./navigation-navigator.md)
