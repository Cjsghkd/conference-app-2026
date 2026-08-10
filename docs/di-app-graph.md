# AppGraph and UiGraph

Dependencies live in two nested graphs with distinct lifetimes:

- **`AppGraph`** (`AppScope`) lives for the process — data layer, Soil client, logging.
- **`UiGraph`** (`UiScope`) lives for one UI instance — navigation state such as `AppNavigator` and the aggregated `AppEntryProvider`. On Android each `Activity` gets its own `UiGraph`, so several concurrently launched activities never share a back-stack navigator.

## AppGraph

`AppGraph` is a **plain interface** (the contract), realized per platform as a Metro `@DependencyGraph`. `KaigiApp` depends only on the interface and receives it via a `context(AppGraph)` parameter, so the shared UI never knows which platform realized it.

```kotlin
interface AppGraph {
    val uiGraph: UiGraph
    val appInitializer: AppInitializer // run by the platform entry point during startup
    val rootTabNavigator: RootTabNavigator // bridge to shells outside the Compose tree
}

@DependencyGraph(scope = AppScope::class)
interface AndroidAppGraph : AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AndroidAppGraph
    }

    @Provides @SingleIn(AppScope::class)
    fun provideByteStore(context: Context): ByteStore = createByteStore(context)
}
```

- Two reasons the graph is realized per platform, in the terminal app module:
  - **It must see every contribution.** A graph aggregates only the `@Contributes*` bindings on its own compile classpath, so it has to sit where every feature and core module is visible. `app-shared` carries them as `api` dependencies and every terminal module depends on it, so each graph sees the whole set.
  - **Each platform may hand in its own dependencies.** The graph's factory lets the platform host inject platform-specific values at creation — on Android the `Context` (`create(context)`), and on iOS potentially a Kotlin interface whose implementation lives in Swift. Platforms that need nothing extra build with a no-arg `createGraph()`.
- Everything else is contributed across modules with `@Contributes*`, so the graphs carry almost no hand-wiring.

## Generated API providers

A Ktorfit API interface in `:core:data` marked `@ProvidedApi` drives KSP to generate a per-API provider trio: a `<Api>Provider` interface, a `Default<Api>Provider` bound with `@ContributesBinding(AppScope)` that builds the API over the production `Ktorfit`, and a `provide<Api>` bridge that exposes the API itself to the graph.

```kotlin
// generated for @ProvidedApi SessionsApi
@Inject @SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultSessionsApiProvider(ktorfit: Ktorfit) : SessionsApiProvider {
    override val api: SessionsApi = ktorfit.createSessionsApi()
}
```

`:feature:debug` swaps the default in dev builds: `EnvironmentAwareSessionsApiProvider` is contributed with `@ContributesBinding(AppScope::class, replaces = [DefaultSessionsApiProvider::class])`, routing calls to the fake or the environment the debug screen selects. Because the replacement rides the same binding, the graph resolves it automatically wherever `:feature:debug` is on the classpath.

## Cross-cutting bindings

Some bindings are declared directly on the graph rather than generated. `CommonAppBindings` (`app-shared`) provides the Soil `SwrClientPlus` and the `ErrorRelay` it consumes; the relay is always bound, retaining only the latest record, so error surfacing needs no per-platform wiring. See [Error handling](./error-handling.md).

## UiGraph

`UiGraph` is a `@GraphExtension(UiScope::class)` of the app graph, declared in `app-shared`. `AppGraph` declares it as a plain accessor — every read of `appGraph.uiGraph` builds a new graph — and `KaigiApp` holds one with `retain`, so it survives configuration changes but is torn down with its UI instance:

```kotlin
@GraphExtension(UiScope::class)
interface UiGraph {
    val appNavigator: AppNavigator
    val appEntryProvider: AppEntryProvider
    val swrClient: SwrClientPlus
    val themeColorSchemeSubscriptionKey: ThemeColorSchemeSubscriptionKey
    // …
}

// KaigiApp
val uiGraph = retain { appGraph.uiGraph }
```

A `@GraphExtension.Factory` is only required where the creation itself must be *injected* into another class, or where the graph takes arguments. `AppGraph` is handed to `KaigiApp` directly, so the accessor suffices.

Anything whose lifetime is one UI instance binds into `UiScope`: `AppNavigator` is `@SingleIn(UiScope::class)`, features contribute `NavEntryProvider`s with `@ContributesIntoSet(UiScope::class)`, and the per-screen graph factories are contributed with `@ContributesTo(UiScope::class)` — so screen graphs are extensions of `UiGraph` and can inject UI-scoped bindings. Process-lifetime bindings must not depend on UI-scoped ones; Metro rejects the reverse edge at compile time.

Accessors follow the consumer, not the binding's scope: everything the UI shell reads — including app-scoped bindings such as the `SwrClientPlus` or the logger — is exposed on `UiGraph`, while `AppGraph` keeps only what must be reachable before or outside a UI instance (the `UiGraph` accessor, the `AppInitializer` the entry point runs during startup, and the Swift-facing `RootTabNavigator`).

Related: [ScreenContext design](./screen-context.md) · [Per-screen graphs (@GraphExtension)](./di-screen-graph.md)
