# AppGraph (app-wide dependency graph)

`AppGraph` is a **plain interface** (the contract), realized per platform as a Metro `@DependencyGraph`. `KaigiApp` depends only on the interface and receives it via a `context(AppGraph)` parameter, so the shared UI never knows which platform realized it.

```kotlin
interface AppGraph {
    val appNavigator: AppNavigator
    val appEntryProvider: AppEntryProvider
    val navKeySerializers: NavKeySerializers
    val swrClient: SwrClientPlus
    val themeColorSchemeSubscriptionKey: ThemeColorSchemeSubscriptionKey
    // …
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

- Two reasons the graph is realized per platform, in the terminal app module (or `app-shared/iosMain` for iOS, which has no Gradle app module):
  - **It must see every contribution.** A graph aggregates only the `@Contributes*` bindings on its own compile classpath, so it has to sit where every feature and core module is visible — at or below `app-shared`.
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

Related: [ScreenContext design](./screen-context.md) · [Per-screen graphs (@GraphExtension)](./di-screen-graph.md)
