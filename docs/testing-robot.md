# Robot pattern tests

End-to-end screen behaviour is tested with the **Robot pattern** in a BDD (behavior-driven development) style, so a scenario reads as behaviour. The scaffolding lives in `:core:testing`; the first real screen test is `TimetableScreenRobotTest` in `:feature:sessions`.

## What

- A **`Robot`** per screen encapsulates that screen's interactions (`setupContent`, taps) and assertions (`check…`) over a `ComposeUiTest`. Scenarios compose them.
- A small **BDD DSL** — `describe` / `doIt` / `itShould` — builds a scenario tree that flattens to one runnable block per `itShould`: each block replays the `doIt` steps in scope, then runs its assertion against a fresh composition, so assertions stay isolated.

## How

Screen-level Compose testing runs on the **JVM** through Compose Multiplatform's `runComposeUiTest` (from `org.jetbrains.compose.ui:ui-test`; the desktop actual and — via `compose.desktop.currentOs` — the Skiko native runtime ship alongside it). The BDD DSL itself is pure Kotlin in `commonMain`, so Android/iOS Robots can follow later via `expect/actual`; the Robot base is JVM-first for now.

A Robot builds the screen with a concrete [`ScreenContext`](./screen-context.md) constructed directly (no Metro graph) from **fake Soil keys** — `buildQueryKey` / `buildSubscriptionKey` / `buildMutationKey` with immediate, network-free lambdas — and wires the `SwrClient` locals via `SwrClientProvider`. The screen reads `LocalSnackbarHostState` (provided in production by `snackbarNavEntryDecorator`), so the test scaffold provides one too.

## The real scenario

```kotlin
runRobotTest(robotFactory = { TimetableScreenRobot(this) }) {
    describe("when the timetable has loaded") {
        doIt { setupContent(sampleTimetable) }
        itShould("show Day1 sessions") {
            checkSessionDisplayed("Day1 A")
            checkSessionDoesNotExist("Day2 A")
        }
        describe("and the Day2 tab is tapped") {
            doIt { clickDayTab(DroidKaigi2026Day.Day2) }
            itShould("swap the list to Day2 sessions") {
                checkSessionDisplayed("Day2 A")
                checkSessionDoesNotExist("Day1 A")
            }
        }
    }
}
```

Complements the per-screen [Preview screenshot tests](./testing-preview-screenshot.md) (which cover static rendering) by exercising interaction and state.

Related: [Testing overview](./testing.md) · [Presenter unit tests (Molecule)](./testing-presenter.md)
