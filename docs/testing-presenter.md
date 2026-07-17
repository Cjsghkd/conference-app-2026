# Presenter unit tests (Molecule)

A Composable presenter is a **function that returns the state of a screen**: it takes actions and data in and returns the `UiState` the screen renders, with no UI attached. It cannot be called like a plain function, though — `remember` and the effect APIs only work inside a running Compose runtime. Fortunately, combining a few tools removes that obstacle: Molecule drives the Compose runtime (no UI needed) and exposes the returned `UiState` as a `Flow`, and Turbine asserts its emissions — send an action, await the next state, assert.

## Tools

- **Molecule** (`app.cash.molecule:molecule-runtime`) … drives a `@Composable` and observes its return value (UiState) as a `Flow`.
- **Named fake keys** (small test classes delegating to `buildQueryKey {}` / `buildMutationKey {}` from `soil-query-core`) … a key whose fetcher returns a fixture (or throws) drives Soil's query/mutation state from a test:

  ```kotlin
  class FakeFavoriteTimetableItemIdMutationKey(
      mutate: suspend (TimetableItemId) -> Unit = {},
  ) : FavoriteTimetableItemIdMutationKey by buildMutationKey(
      id = MutationId("fake-favorite"),
      mutate = { mutate(it) },
  )
  ```
- **A minimal Fake of the role context (`<Feature>PresenterContext`)** … since it is a narrow interface, a minimal implementation can be passed in (a dividend of adopting role contexts for testability).
- **Turbine** … asserts `Flow` emissions.
- **kotlinx-coroutines-test** (`runTest` / `TestDispatcher`) … drives virtual time.

## Basic recipe

The Molecule scaffolding (drive the runtime, provide the `SwrClient`, supply the `PresenterContext`, assert with Turbine) is identical for every presenter test, so it is written **once**: `runPresenterTest` in `:core:testing` (`core/testing/src/commonMain/…/PresenterTest.kt`):

```kotlin
// core:testing — shared scaffolding, written once. The test drives the screen from both ends:
// `send` plays the Root's role (gated by ScreenContext), `uiStates` observes the presenter's
// return value, and `results` observes the ActionResults the presenter emits (captured by an
// ActionResultEffect composed with a test ScreenContext — the Root's role again).
class PresenterTestScope<A, R, S>(
    private val screenContext: ScreenContext,
    private val screenChannel: ScreenChannel<A, R>,
    val uiStates: ReceiveTurbine<S>,
    val results: ReceiveTurbine<R>,
) {
    fun send(action: A) = context(screenContext) { screenChannel.send(action) }
}

fun <C : PresenterContext, A, R, S> runPresenterTest(
    presenterContext: C,
    presenter: @Composable context(C) (ScreenChannel<A, R>) -> S,
    validate: suspend PresenterTestScope<A, R, S>.() -> Unit,
) = runTest {
    val screenContext = object : ScreenContext {}           // marker interface — a plain object suffices
    val screenChannel = ScreenChannel<A, R>()
    val results = Channel<R>(Channel.BUFFERED)
    val uiStateFlow = moleculeFlow(RecompositionMode.Immediate) { // drive the Compose runtime
        val client = SwrCachePlus(backgroundScope)          // a real SwrCachePlus works under Molecule (no TestSwrClientPlus needed)
        compositionLocalProviderWithReturnValue(
            LocalSwrClient provides client,
            LocalQueryClient provides client,
            LocalMutationClient provides client,
            LocalSubscriptionClient provides client,
        ) {
            context(screenContext) {                        // play the Root: capture the result side
                ActionResultEffect(screenChannel) { results.send(it) }
            }
            context(presenterContext) {                     // supply the PresenterContext (do not use with=receiver)
                presenter(screenChannel)
            }
        }
    }
    turbineScope {                                          // one flat scope for both turbines
        // Molecule re-emits on every recomposition; equal consecutive states are noise to a test.
        val uiStates = uiStateFlow.distinctUntilChanged().testIn(backgroundScope)
        val resultsTurbine = results.receiveAsFlow().testIn(backgroundScope)
        PresenterTestScope(screenContext, screenChannel, uiStates, resultsTurbine).validate()
        uiStates.cancelAndIgnoreRemainingEvents()
        resultsTurbine.cancelAndIgnoreRemainingEvents()
    }
}

// CompositionLocalProvider's content is @Composable () -> Unit, so the UiState cannot be
// returned through it directly — provide the locals via Composer.startProviders/endProviders
// instead, which lets content return a value.
@OptIn(InternalComposeApi::class)
@Composable
fun <T> compositionLocalProviderWithReturnValue(
    vararg values: ProvidedValue<*>,
    content: @Composable () -> T,
): T {
    currentComposer.startProviders(values)
    val result = content()
    currentComposer.endProviders()
    return result
}
```

Each feature test is then just the faked context, the presenter call, and the actions/assertions:

```kotlin
@Test
fun bookmark_event_marks_session() = runPresenterTest(
    presenterContext = TimetablePresenterContext(           // directly new up the concrete @Inject class
        favoriteTimetableItemIdMutationKey = FakeFavoriteTimetableItemIdMutationKey(),
    ),
    presenter = { channel -> timetableScreenPresenter(channel, fakeTimetable) },
) {
    assertEquals(expectedInitial, uiStates.awaitItem())
    send(TimetableScreenAction.Bookmark(id))
    assertEquals(expectedBookmarked, uiStates.awaitItem())
    assertEquals(TimetableScreenActionResult.ShowMessage(bookmarked), results.awaitItem())
}
```

Both ends of the `ScreenChannel` are gated: `send` requires a [`ScreenContext`](./screen-context.md), and results can only be read through an `ActionResultEffect` (also `ScreenContext`-gated; the channels themselves are `internal` to `:core:common`). The scaffold therefore plays the Root's role with a test `ScreenContext` — `send` opens it around `screenChannel.send`, and an `ActionResultEffect` composed next to the presenter captures the emitted results into the `results` turbine.

The presenter has no loading-to-content transition: the Root's [`SoilDataBoundary`](./soil-data-boundary.md) and `rememberQuery` handle loading, and the presenter receives an already-loaded `Timetable`. Presenter tests assert state transitions only.

## Points of craft

1. **`RecompositionMode.Immediate`**: executes recomposition immediately without waiting for the frame clock (the crux of the test).
2. **Supplying dependencies**: the presenter requires `rememberMutation` / `SwrClientProvider` / `LocalClock` plus a `PresenterContext` (supplied via `context(presenterContext){}`). Inside the test composition, supply **a real `SwrCachePlus(backgroundScope)` (no `TestSwrClientPlus` needed) plus per-key `buildQueryKey{}`/`buildMutationKey{}` plus `runTest` virtual time plus a Fake of the role interface**.
3. **Verifying success/failure**: to fire a `MutationSuccessEffect` / `MutationErrorEffect`, **give the fake key a `mutate` that succeeds or throws** and **assert the emission on the `results` turbine** → even the one-off wiring can be verified.
4. **Input/output**: feed actions via the scope's `send(action)` to drive UiState transitions, and assert the result side on the `results` turbine.
5. **Multiplatform**: the presenter is pure logic in commonMain, so **running it on the JVM is sufficient** (rendering-free logic verification does not need any UI target).

## Position in the test pyramid

- **Presenter test (Molecule)**: fast verification of state and logic without rendering UI.
- **Screen test ([Robot](./testing-robot.md) + Roborazzi)**: rendering + screenshot + behavior.

The presenter layer sits beneath Robot/Roborazzi: cheap to run, so state transitions are covered here, leaving the screen layer to rendering concerns.

Related: [Testing overview](./testing.md) · [Robot pattern tests](./testing-robot.md)
