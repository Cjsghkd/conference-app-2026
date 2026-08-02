# Debugging

Debug-only tooling lives in a dedicated **`:feature:debug`** module so it is wired into development builds only and never ships in production. The module carries two things: the in-app debug screen, and the JetWhale agent that connects the running app to a desktop debugger. Keeping it in its own feature module means the rest of the app has no dependency on debug code, and the tooling can use the same DI / navigation seams as a normal feature. The module is not depended on unconditionally: Android wires it as `devImplementation` (the dev product flavor only), desktop and web gate it on the `-PincludeDebugFeature` Gradle property (default on for local dev), and iOS defaults that property from Xcode's build configuration. Because both the screen and the agent reach the graph purely through Metro aggregation, dropping the dependency drops them with no other code changes. For the per-platform wiring and how to verify the exclusion, see [Keeping dev-only code out of release](./build-dev-only-exclusion.md).

The `DebugScreen` is wired like any other screen (`DebugNavKey` + `DebugScreenContext` + `DebugScreenGraph` + `DebugNavEntryProvider`). It shows the real app version via `BuildConfigProvider` and offers a **Clear persisted data** action; the remaining rows (feature flags, network log) are placeholders. Production logging is [Kermit-based](./logging.md); HTTP traffic is inspected through JetWhale rather than an in-app log.

## Clear persisted data

The menu's **Clear persisted data** button wipes the app's persisted (and in-memory session) state in one call, so a tester can return the app to a clean slate. It is aggregated by `PersistedDataResetter` (`:core:data`, `AppScope`):

- **Preferences DataStore** (theme) — `ThemeStore.clear()` (`dataStore.edit { it.clear() }`, clearing the whole preferences file).
- **Favorites** — `FavoritesStore.clear()` (currently an in-memory stand-in).
- **Blobs** (e.g. the profile image) — a new `ByteStore.clear()`, implemented per platform: delete the blob directory on JVM/Android/iOS, and `IDBObjectStore.clear()` on the Web (wasmJs) IndexedDB actual.

`DebugScreenContext` holds the `PersistedDataResetter`, and the data-light `DebugScreenRoot` runs `clearAll()` on a local coroutine scope (a debug-only side effect, so it does not go through the mutation/presenter path), showing a "cleared ✓" confirmation.

## JetWhale agent

[JetWhale](https://kitakkun.github.io/JetWhale/) is a desktop debugger that a running app connects to over a WebSocket. Dev builds attach three of its plugins: the **Nav3 Navigator** (the live Navigation 3 back stack, with push / pop / reorder driven from the host), the **Network Inspector** (HTTP transactions and response mocking), and the **Compose Semantics Inspector** (the Compose node tree, with each node's own semantics actions invocable). The host exposes all three over an MCP server as well, so an AI agent can drive the app through the same operations.

JetWhale is on trial here. Its agent runtime and Ktor network adapter have Maven Central releases, but the Nav3 and Compose Semantics agents do not yet — those two exist only as snapshots. So the catalog pins `1.0.0-alpha11-SNAPSHOT` for every JetWhale artifact, keeping one version across them, and `settings.gradle.kts` declares the Central snapshots repository, restricted to the `com.kitakkun.jetwhale` group. Moving to a release is a version-catalog edit once alpha11 is published.

### Running it

Install the host from the [JetWhale releases page](https://github.com/kitakkun/JetWhale/releases) and launch it; it listens for debuggees on port **5080**. Run a dev build, and the app appears as a session in the host once the first composition connects the agent. Android devices and emulators reach the host through `adb reverse tcp:5080 tcp:5080`, which the host wires up automatically unless ADB auto port mapping is turned off in its settings.

| Target | Reaches the host | Compose Semantics Inspector |
| --- | --- | --- |
| Android (dev flavor) | via `adb reverse`, automatic | yes |
| Desktop | `localhost` | yes |
| Web | `localhost` | no |
| iOS Simulator | `localhost` | no |
| iOS device | needs the host's LAN address over `wss` | no |

The Nav3 Navigator and the Network Inspector work on every target. The Compose Semantics Inspector needs a probe that finds the platform's Compose roots, and JetWhale ships one for Android and desktop only; elsewhere it reports an empty tree.

A physical iPhone does not see the host on `localhost`. Connecting one means pointing `JetWhaleDebugger` at the machine's LAN address over the host's secure WebSocket port, and adding `NSLocalNetworkUsageDescription` to the iOS app's `Info.plist` so iOS permits local-network access. The upstream [Secure connections (wss)](https://kitakkun.github.io/JetWhale/guide/getting-started#secure-connections-wss) guide covers the certificate side.

### How it is wired

Production code sees only two seams in `:core:common`, one per thing the host reaches. Both are `fun interface`s with a `@Composable operator fun invoke`, so an injected instance is called exactly like the plain composable effects beside it:

```kotlin
fun interface BackStackDebuggingEffect {
    @Composable
    operator fun invoke(backStack: NavBackStack<NavKey>)
}

fun interface SemanticsDebuggingEffect {
    @Composable
    operator fun invoke()
}
```

`KaigiApp` composes both next to the `NavDisplay`:

```kotlin
uiGraph.backStackDebuggingEffect(backStack)
uiGraph.semanticsDebuggingEffect()
```

The bindings are `NoopBackStackDebuggingEffect` and `NoopSemanticsDebuggingEffect` unless `:feature:debug` is on the classpath, in which case one `JetWhaleDebugger` replaces both through Metro — the same aggregation the debug screen uses.

`JetWhaleDebugger` is an `AppScope` singleton, and each plugin reaches the app through a seam that already exists:

- **Nav3** — `Nav3KeyCodec.openPolymorphic` takes the merged `SerializersModule` the back stack is already built from, so the host can decode and construct every `NavKey` in the app. See [NavKey serializer aggregation](./navigation-navkey-serializers.md).
- **Network** — the interceptor attaches to the injected `HttpClient` through Ktor's `HttpSend`, leaving the `:core:data` provider untouched. `HttpSend` cannot unregister an interceptor and does not reject duplicates, so the singleton scope is what keeps each transaction recorded once.
- **Compose semantics** — `SemanticsProbe()` is an `expect` function; the Android and desktop actuals install JetWhale's probe, and the iOS and Web actuals do nothing.

Because the attachment is created on first access, the connection opens with the first composition rather than at process start. Requests issued before that are not captured.

Related: [Keeping dev-only code out of release](./build-dev-only-exclusion.md) · [Logging (Kermit)](./logging.md)
