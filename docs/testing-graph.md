# Test graph (TestingScope)

`TestingScope` is the DI scope of a test. Fakes are contributed into it from `:core:testing`, and each screen declares a `<Screen>ScreenTestGraph` in its `commonTest` source set that hands back the contexts a test needs. [Presenter tests](./testing-presenter.md) take the `PresenterContext` from it; [Robot tests](./testing-robot.md) take the `ScreenContext`.

Because Metro constructs those contexts, a new dependency on a `PresenterContext` or `ScreenContext` is satisfied by adding one fake — no test is edited.

## Fakes

A fake binds the same interface the production graph binds, so it must be contributed to `TestingScope`:

```kotlin
// :core:testing — one fake per role, shared by every feature
@Inject @SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class)
class FakeKaigiLogger : KaigiLogger { … }
```

Soil key fakes delegate to `buildQueryKey` / `buildSubscriptionKey` / `buildMutationKey`. Those expressions cannot reference the class under construction, so the mutable part is passed as a constructor parameter — `FakeFixture` for a query or subscription, `FakeMutationState` for a mutation's recorded calls and armed failure. A query fake extends `FakeKeyControl`, which exposes that fixture as the surface a test drives:

```kotlin
@SingleIn(TestingScope::class)
@ContributesBinding(TestingScope::class, binding = binding<SponsorsQueryKey>())
class FakeSponsorsQueryKey private constructor(
    fixture: FakeFixture<Sponsors>,
) : FakeKeyControl<Sponsors>(fixture),
    SponsorsQueryKey by buildQueryKey(
        id = QueryId("fake-sponsors"),
        fetch = { fixture.await() },
    ) {
    @Inject constructor() : this(FakeFixture(Sponsors(groups = persistentListOf())))
}
```

`FakeKeyControl` adds a second supertype, so `@ContributesBinding` must name the bound type explicitly.

## Loading and error states

The fetch runs through `FakeFixture.await()`, which is where a test reaches the two states [`SoilDataBoundary`](./soil-data-boundary.md) renders around its content:

| Call | Effect on the boundary |
| --- | --- |
| `hold()` | the fetch suspends, so the `Suspense` loading fallback stays on screen |
| `release()` | the fetch completes with the current value and the content replaces the fallback |
| `failWith(…)` | the fetch throws, so the `ErrorBoundary` error fallback renders |

A Robot exposes them as scenario steps:

```kotlin
fun setupLoadingContent() {
    graph.sponsorsQueryKey.hold()
    setupContent(Sponsors(groups = persistentListOf()))
}

fun releaseLoad(sponsors: Sponsors) {
    graph.sponsorsQueryKey.set(sponsors)
    graph.sponsorsQueryKey.release()
    composeUiTest.waitForIdle()
}
```

Every `MutationKey` must carry a `MutationTag` ([Mutations](./soil-mutation.md)) — including a fake, since the checker also runs over test sources. `:core:testing` provides one tag for all test graphs; each test owns its `SwrClient`, so the per-screen cache isolation the tag exists for is already satisfied.

## Graph

The graph is accessors only: the contexts, plus each fake the test configures or inspects. `additionalScopes` admits the screen scope, because a `ScreenContext` is `@SingleIn(<Screen>ScreenScope::class)`:

```kotlin
@DependencyGraph(scope = TestingScope::class, additionalScopes = [SponsorsScreenScope::class])
interface SponsorsScreenTestGraph {
    val screenContext: SponsorsScreenContext
    val presenterContext: SponsorsPresenterContext
    val sponsorsQueryKey: FakeSponsorsQueryKey
}
```

The production `Default<…>Key` bindings live in `:core:data`, which is not on a feature's test classpath, so admitting the screen scope pulls in no competing binding.

## Lifetime

A graph fixes one wiring, so anything that varies is mutable state inside a fake — `set(…)` / `hold()` / `release()` for a query, `failWith(…)` to arm a mutation to throw, `invocations` to read what reached it.

Each holder of a graph creates its own, and the graph is never shared:

- A presenter test class holds one. Each test method runs on a fresh instance, hence a fresh graph.
- A Robot holds one. `runRobotTest` builds a fresh Robot per `itShould`, hence a fresh graph, and `setupContent` re-reads the fixture through a new `SwrClient` so a scenario may call it more than once.

Related: [Testing overview](./testing.md) · [Presenter unit tests (Molecule)](./testing-presenter.md) · [Robot pattern tests](./testing-robot.md)
