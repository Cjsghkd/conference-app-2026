# Clock (KaigiClock)

The app reads the current time through one injected seam, `KaigiClock` (`:core:common`), and never through `Clock.System` directly. Production binds it to the system clock; dev builds bind it to a clock the debug tooling can move; tests substitute a clock they control. Because every reading goes through the same interface, moving time moves the whole app at once.

`Instant` is a stdlib type (`kotlin.time`) as of Kotlin 2.3, so `KaigiClock` needs no library behind it; kotlinx-datetime supplies the calendar side (time zones, `LocalDateTime`, formatting), and its own `Instant` / `Clock` are deprecated aliases for the stdlib ones.

```kotlin
interface KaigiClock {
    fun now(): Instant
}

@Inject
@ContributesBinding(AppScope::class)
class SystemKaigiClock : KaigiClock {
    override fun now(): Instant = Clock.System.now()
}
```

`KaigiClock` declares `now()` itself rather than extending `kotlin.time.Clock`. Reading the time is all the app asks of it, and an interface that states its one method says so at the declaration instead of sending the reader to a supertype.

## Rules

- Production and feature code inject `KaigiClock`. `Clock.System.now()` appears only where the clock itself is built: a `KaigiClock` implementation, and the offset store that measures a shift against the unshifted system clock.
- Presenters take the clock from their `PresenterContext`, like any other dependency.
- A UI composable must not read the clock. It renders what the presenter computed from it — see [Reaching the UI](#reaching-the-ui).
- A test that depends on the time uses `FakeClock` (`:core:testing`) rather than the real clock.

## Reaching the UI

The clock is not a `CompositionLocal`. A `CompositionLocal` carries a value that differs by position in the composition ([CompositionLocal review](./compositionlocal-review.md)), and every one the app owns is there for that reason: the snackbar host belongs to a nav entry, the preview image resolver exists in a preview and not in production, the applied scheme is dark inside one subtree and not another. The time is the same wherever it is asked for, and a shift applied for debugging is one shift over the whole app — a seam that lets a subtree answer differently offers a freedom nothing needs and nothing checks.

That holds because no component decides anything from the time. What a component wants is an answer — whether a session is running, how long until the next one starts, where a now-line sits — and answers are what a presenter computes and a `UiState` carries, which [`UiComponentTakesWhatItReads`](./enforcement.md#uicomponenttakeswhatitreads) keeps true as components are written. It also puts the decision where [`FakeClock` pins it in a fast unit test](./testing-presenter.md), rather than behind a screenshot or a Robot scenario.

The cost is one `UiState` field per value that ticks. Where recomposing a whole screen every second is too coarse, the ticking part takes a UiState type of its own rather than the clock reaching further down — see [Presenter performance](./presenter-performance.md).

Timing an animation is a different question: elapsed time comes from a monotonic source (`TimeSource.Monotonic`, `withFrameNanos`), which a shifted or resynchronised wall clock cannot disturb.

## Shifting time in a dev build

`:feature:debug` replaces the production binding with `DebugKaigiClock`, which adds an offset held by `KaigiClockOffsetStore` (an `AppScope` singleton):

```kotlin
@ContributesBinding(AppScope::class, replaces = [SystemKaigiClock::class])
class DebugKaigiClock(private val offsetStore: KaigiClockOffsetStore) : KaigiClock {
    override fun now(): Instant = Clock.System.now() + offsetStore.offset.value
}
```

Setting a time stores the difference between it and the system clock, so the app **keeps ticking at real speed** from the instant you chose — time-dependent behaviour (a session starting, a countdown) still progresses. `reset()` returns the offset to zero.

The offset lives in memory only: it lasts for the session and a restart returns the app to the system clock. Because the whole store lives in `:feature:debug`, a release build has no offset to apply — see [Keeping dev-only code out of release](./build-dev-only-exclusion.md).

Two front ends write to the same store:

| Front end | Where | Offers |
| --- | --- | --- |
| Debug screen, "Clock" section | in the app | the current time, conference-day presets, an ISO-8601 field, and reset |
| The app's JetWhale plugin | on the desktop host, and over its MCP server | the same set, plus tool calls an AI agent can make |

Both read and write `KaigiClockOffsetStore`, so a shift made on either side shows up on the other.

The presets compose their instants from `DroidKaigi2026Day` (`:core:model`), which holds the date each conference day falls on and derives its own `9/2`-style label from it. `at(hour, minute)` resolves a wall-clock time against `ConferenceTimeZone`, declared beside it as a fixed offset because Japan does not observe daylight saving. Writing a conference date anywhere else is what let the presets drift from the timetable once already.

## JetWhale plugin

The plugin is built in this repository, in the layout JetWhale's [plugin guide](https://kitakkun.github.io/JetWhale/guide/developing-plugins) describes: an agent half inside the app, a host half loaded by the desktop debugger, and a protocol module shared by both.

| Module / class | Side | Responsibility |
| --- | --- | --- |
| `:tools:jetwhale-plugin:protocol` | shared | the `@Serializable` messages and `KAIGI_PLUGIN_ID` |
| `KaigiAgentPlugin` (`:feature:debug`) | app | answers state requests, applies shifts, and reports debug-screen shifts back as events |
| `:tools:jetwhale-plugin:host` | desktop | the plugin UI and the `io.github.droidkaigi.confsched2026.clock.*` MCP tools |

The app contributes **one** plugin, `io.github.droidkaigi.confsched2026`, and the clock is its first control rather than its subject. A `pluginId` is what pairs the two halves and what the drawer lists, so one id per debug control would multiply the pairing, the `onPrepare` exchange, the enable switch and the drawer icon for knobs that all belong to the same tool. A second control is another message type in the protocol module and another section in the host UI. The id carries the year for the same reason the application id does — a host installation holds one jar per id.

The host half compiles against the Compose and host-SDK versions of the JetWhale release it targets, as `compileOnly` — the host supplies them at runtime, so they must not be bundled. The version catalog keeps those pins separate from the app's own Compose version.

The preset list travels over the wire rather than being restated on the host, so the app is the only side that decides which instants to offer.

Run the host with the plugin loaded:

```shell
./gradlew :tools:jetwhale-plugin:host:runJetWhaleHot   # host + hot reload on source changes
./gradlew :tools:jetwhale-plugin:host:installPlugin    # install into ~/.jetwhale/plugins/
```

## Tests

`FakeClock` (`:core:testing`) binds `KaigiClock` in `TestingScope`, so a presenter that takes the clock resolves it from the screen's [test graph](./testing-graph.md) with no test edited. The same accessor is how a test drives it:

```kotlin
graph.clock.instant = Instant.parse("2026-09-02T10:00:00+09:00")
// … exercise the presenter …
graph.clock.advanceBy(40.minutes)
```

Its default instant is Day 1 during session hours, so a test that never sets the time still reads an instant during the event.

Related: [Debugging](./debugging.md) · [Test graph (TestingScope)](./testing-graph.md) · [Keeping dev-only code out of release](./build-dev-only-exclusion.md)
