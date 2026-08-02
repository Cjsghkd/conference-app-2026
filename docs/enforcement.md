# Enforcement

On the premise that **AI is the primary author of the code**, the architecture enforces the correct way of writing and makes incorrect ways fail to compile.

## Principles (priority order of enforcement mechanisms)

1. **Make illegal states unrepresentable via API/types** — strongest, **Kotlin-version-independent**, understood by the IDE. Only valid code can be written.
2. **K2 FIR Checker** — turn **binary rules** that types cannot express into compile errors. The extension API is unstable and must be maintained per Kotlin version.
3. **Review / tests** — things that cannot be decided statically, such as data volume or semantic dependencies.

"Eliminate what can be eliminated at level 1 (write no plugin)," "use level 2 only for binaries that types can't express," "fuzzy goes to 3."

## Enforcement map

Violating any rule below fails compilation. Type/boundary rules need no plugin; FIR checkers each get a subsection below (rejected example and reason).

| Rule | Mechanism |
| --- | --- |
| Actions consumed only in the Presenter / results only in the Root | Type — `context(_: …Context)` |
| Results emitted only inside an effect | Type — `emit` is `suspend` |
| Features cannot touch the ScreenChannel receiving side | Visibility — `internal` + module boundary |
| Cross-feature isolation (no importing another feature's `NavKey`) | Module boundary — no Gradle edge between features (`:feature:debug`, dev-only tooling, is exempt) |
| `NavKey` is `@Serializable` | Hand-written serializer registration — a miss is a compile error |
| Preview assets do not enter production | Module boundary — release excludes `:core:preview:impl` |
| `@MustBeSerializable` type arguments are `@Serializable` | FIR `MustBeSerializable` |
| No direct `mutate` call | FIR `NoDirectMutate` |
| Presenter must not declare [`ScreenContext`](./screen-context.md) | FIR `PresenterMustNotDeclareScreenContext` |
| ScreenContext is not a subtype of PresenterContext | FIR `ScreenContextMustNotBePresenterContext` |
| No presenter effect in a screen root | FIR `NoPresenterEffectInScreenRoot` |
| `Navigator` confined to NavEntry | FIR `NavigatorConfinedToNavEntry` |
| Every [`MutationKey`](./soil-mutation.md) carries a `MutationTag` | FIR `MutationKeyMustCarryTag` |
| Screen does not read Soil directly (role-gated) | FIR `SoilReadConfinement` |
| `@Preview` requires a sanctioned wrapper | FIR `PreviewRequiresWrapper` |
| Nav callbacks flow through the UI debounce | FIR `NavLambdaMustFlowToSafeClick` |
| Nav-only click not routed through the presenter | FIR `NoForwardOnlyAction` |
| Theme-dependent previews use `@PreviewParameter` | FIR read + IR `@ThemeSensitive` metadata |
| Argument-forwarding lambdas use callable references | FIR `LambdaCanBeCallableReference` |
| Pass-through lambdas pass the function value itself | FIR `LambdaCanBePassedDirectly` |
| Mutation effect handlers call `reset()` | FIR `MutationEffectMustReset` |
| Platform-confined common declarations carry a platform prefix | FIR `PlatformOnlyNaming` |
| A screen-level composable is the only component in its file | FIR `ScreenIsSoleComponentInFile` |
| Content lambdas nest at most four levels deep | FIR `ComposableNestingDepth` |
| A private property exposed by a wider one uses an explicit backing field | FIR `ExplicitBackingFieldRequired` |
| A private `var` exposed read-only uses `private set` | FIR `PrivateSetRequired` |

> All implemented FIR checkers live in `:tools:compiler-plugin` and are applied to every module. **Roles are identified by the context-parameter type together with `*Presenter`/`*ScreenRoot` naming, not by annotations.**

## FIR checkers (rejected example and reason)

### `NoDirectMutate`

```kotlin
// in a presenter
LaunchedEffect(Unit) {
    bookmarkMutation.mutate(itemId)     // ERROR: NoDirectMutate
    val m = bookmarkMutation.mutate     // ERROR: aliasing is rejected too
}
```

Why: `mutate` bypasses the `MutationState` transition, so `MutatedEffect` / `MutationErrorEffect` never fire — use `mutateAsync(...)`. Soil's `mutate` is a `val: suspend (S) -> T` property, and the checker forbids **any access** to that property, so both the direct call and a desugared alias are compile errors.

### `PresenterMustNotDeclareScreenContext`

```kotlin
context(_: SearchScreenContext, _: SearchPresenterContext) // ERROR on the ScreenContext param
@Composable
fun searchPresenter(): SearchUiState { … }
```

Why: a presenter takes ONLY a `PresenterContext`; declaring a `ScreenContext`-derived context parameter would let it consume Root-role dependencies.

### `ScreenContextMustNotBePresenterContext`

```kotlin
// ERROR: implements BOTH
class SearchScreenContext : ScreenContext, PresenterContext
```

Why: an is-a relationship leaks Root and presenter roles into one type. Use composition — hold a `PresenterContext` as a property: `class SearchScreenContext(val presenterContext: SearchPresenterContext) : ScreenContext`.

### `NoPresenterEffectInScreenRoot`

```kotlin
context(screenContext: SearchScreenContext)
@Composable
fun SearchScreenRoot(...) {
    context(screenContext.presenterContext) {
        searchPresenter()           // OK: presenter launch is the sole exception
        ActionEffect(channel) { … } // ERROR: presenter-only effect in the Root
    }
}
```

Why: the Root narrow-opens a `PresenterContext` scope only to invoke the `*Presenter` function; any other call requiring a `PresenterContext` context parameter (`ActionEffect` / `ScreenChannel.emit`, etc.) is presenter-only and seals that block's remaining hole.

### `NavigatorConfinedToNavEntry`

```kotlin
class SearchScreenContext(val navigator: SearchNavigator) : ScreenContext // ERROR
```

Why: navigation reaches the Root as lambdas — a `Navigator` type may appear only in NavEntryProvider wiring (and core nav infrastructure), never in a `ScreenContext`/`PresenterContext`, a presenter/`@Composable` signature, or a `UiState`/`Action`/`ActionResult`. A `Navigator` that can't be received can't be misused, so the signature-level check suffices.

### `MutationKeyMustCarryTag`

```kotlin
class BookmarkMutationKey(
    private val itemId: TimetableItemId, // ERROR: no MutationTag parameter
) : MutationKey<Unit, TimetableItemId> by buildMutationKey(
    id = MutationId("bookmark/$itemId"), // ERROR: tag not passed into the id
    …
)
```

Why: without a `MutationTag` folded into the `MutationId`, per-screen mutation caches collide (Query/Subscription keys are deliberately shared, mutation keys are not). Both the constructor parameter and its reference inside the `id` argument of a `build*MutationKey` delegation are required.

### `SoilReadConfinement`

```kotlin
@Composable
fun SearchResultList(...) {              // no ScreenContext/PresenterContext param
    val items = rememberQuery(key)       // ERROR: Root-role read outside the root
}
```

Why: reads are role-gated — `rememberQuery`/`rememberSubscription` require an enclosing `ScreenContext` context parameter (Root role); `rememberMutation` requires a `PresenterContext` one. Read Soil at the screen root, not deep in feature UI. (The app shell and `:core` infrastructure are out of scope.)

### `PreviewRequiresWrapper`

```kotlin
@Preview
@Composable
private fun SearchScreenPreview() {
    SearchScreen(uiState = fakeState) // ERROR: not wrapped
}
```

Why: every `@Preview` (JetBrains or AndroidX) must render inside `KaigiTheme` with the preview image resolver, which the wrapper supplies — annotate the function with `@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)`, or make `KaigiPreviewTheme(colorScheme) { … }` the body's top-level statement when the preview picks its own colour scheme. Both checkers read the annotations through one level of meta-annotation, so a multi-preview annotation carrying them counts. See [Preview & sample assets](./preview.md).

### `NavLambdaMustFlowToSafeClick`

```kotlin
@Composable
fun SearchScreen(onItemClick: (TimetableItemId) -> Unit) {
    Button(onClick = { onItemClick(id) }) { … } // ERROR: not debounced
    // OK: Modifier.safeClickable { onItemClick(id) } or safeClick(onItemClick)
}
```

Why: the debounce lives at the UI interaction point, so every `on[A-Z]*` parameter of a feature `*Screen`/`*ScreenRoot` must reach a `safeClick`/`safeClickable` sink (directly, forwarded to another feature `@Composable`'s `on*`, or invoked inside a `safeClick`/`safeClickable`/`ActionResultEffect` lambda). See [Safe click](./navigation-navigator.md#safe-click-navigation-debounce).

Conservative: it matches parameter *name*, so non-navigating `on*` callbacks are also forced through a sink.

### `NoForwardOnlyAction`

```kotlin
ActionEffect(channel) { action ->
    when (action) {
        is Search.ItemClicked -> screenChannel.emit(NavigateToDetail(action.id)) // ERROR
    }
}
```

Why: a handler whose only effectful statement is a `ScreenChannel.emit(...)` merely forwards the action back out as a result — meaningless indirection. Wire the UI callback straight from the Screen to the Root's navigation lambda (see [Error handling](./error-handling.md)). Only a branch/body reduced to that single `emit` is flagged.

### `MustBeSerializable`

```kotlin
// declaration side: the requirement is declared on the type parameter
inline fun <T : Any, @MustBeSerializable reified RESPONSE : Any> buildPersistedQueryKey(…)

// call site
data class SearchResponse(…)                                   // no @Serializable
buildPersistedQueryKey(id, persistKey = "…", byteStore = …,
    fetchResponse = { searchResponse },                        // ERROR: RESPONSE not @Serializable
    transformToDomainModel = { … })
```

Why: a missing `@Serializable` would only fail at runtime when persistence serializes; this checker restores the compile-time gate the reified serializer lookup removed. The check is driven by the `@MustBeSerializable` annotation (`:core:common`) on a type parameter — not a hard-coded callable and argument index — so signature changes cannot silently detach it, and any function can opt in. An unresolvable classifier (type parameter, local/anonymous type) is rejected rather than silently allowed.

### `MutationEffectMustReset`

```kotlin
MutationErrorEffect(favoriteMutation) { error ->   // ERROR: no reset() in the handler
    screenChannel.emit(ShowMessage(error.toUserMessage()))
}
```

Why: the consumed Success/Error stays in the Soil cache beyond the screen instance, so a handler that never calls `mutation.reset()` re-fires the stale result on the next instance of the screen. Only the presence of a `reset()` call inside the handler is checked — where in the handler it runs is up to the use case.

### `PlatformOnlyNaming`

```kotlin
// commonMain
@PlatformOnly(TargetPlatform.Ios)
fun HapticsSyncEffect(...) { … }   // ERROR: name must start with "Ios"

fun IosHapticsSyncEffect(...) { … } // ERROR: "Ios" prefix without @PlatformOnly
```

Why: a declaration in a common source set that only has an effect on one platform must say so in its name, and a platform-prefixed name must be backed by `@PlatformOnly` (`:core:common`) so the prefix cannot lie or go stale. The reverse rule applies only to top-level declarations under `commonMain`; platform source sets use platform-prefixed names freely.

### `ScreenIsSoleComponentInFile`

```kotlin
// TimetableScreen.kt
@Composable
fun TimetableScreen(...) { … }

@Composable
private fun TimetableCard(...) { … }   // ERROR: move it to TimetableCard.kt
```

Why: the file path is the component's identity, so an agent locates and edits a component without reading the screen it happens to sit in. A file declaring a top-level `Unit`-returning `@Composable` named `*Screen`/`*ScreenRoot` may declare no other UI component; `@Preview` functions and value-returning composables (presenters, `safeClick`) are exempt. The extracted component becomes `internal` — file-private visibility is not load-bearing here, since the module boundary already confines it to its feature.

### `LambdaCanBeCallableReference`

```kotlin
TimetableScreenRoot(
    onNavigateToDetail = { id -> navigator.openSessionDetail(id) }, // ERROR
    // OK: onNavigateToDetail = navigator::openSessionDetail
)
```

Why: a lambda whose entire body is one call forwarding the lambda parameters unchanged is noise — write the callable reference. The checker skips every shape a reference cannot substitute: `suspend` or receiver-typed function types, varargs, infix/operator calls, explicit type arguments, and receivers that are not a plain `this`/object/`val` chain (a reference captures its receiver once, so a mutable receiver would change semantics).

`@Composable` lambdas are excluded by choice rather than necessity. A composable reference compiles, but `::Title` at a content slot hides the call syntax that marks a composable in Compose code. Forwarding a composable value is covered by `LambdaCanBePassedDirectly` instead.

### `LambdaCanBePassedDirectly`

```kotlin
Wrapper(content = { content() })                     // ERROR: content = content
Modifier.safeClickable { onOpenSoilErrors() }        // ERROR: safeClickable(onOpenSoilErrors)
flow.collect { block(it) }                           // ERROR: collect(block)

RowScopeConsumer(content = { content() })            // OK: adapts () -> Unit to RowScope.() -> Unit
```

Why: a lambda that does nothing but invoke a function value already in scope is one indirection with no meaning — pass the value. This is not a callable reference, so it stays available where `LambdaCanBeCallableReference` does not apply, including `@Composable` and `suspend` function types.

The lambda's type and the value's type must be equal, which keeps adaptations out: a `@Composable () -> Unit` cannot reach a `@Composable RowScope.() -> Unit` parameter on its own. The value must also be a parameter or a `val`, since a `var` could change between the point the lambda is created and the point it runs.

### `ComposableNestingDepth`

```kotlin
@Composable
fun TimetableScreen(uiState: TimetableScreenUiState) {
    Scaffold { padding ->                 // 1
        Column(Modifier.padding(padding)) {   // 2
            LazyColumn {                  // 3
                items(uiState.sessions) { item ->  // 4
                    Card {                // ERROR: 5
                        Text(item.title)
                    }
                }
            }
        }
    }
}
```

Why: a deeply nested tree hides the structure of the screen. A `@Composable` function may nest content lambdas at most **four** levels deep; the fifth level must move into its own `@Composable` function (`TimetableCard` above). In a screen file, [`ScreenIsSoleComponentInFile`](#screenissolecomponentinfile) then gives that component its own file.

A lambda counts towards the depth only when its body emits UI, so `onClick`, `remember`, and coroutine bodies are free. Builder lambdas that wrap content — `forEach`, `LazyListScope` — do count, because they add a level of braces the reader has to follow. The error is reported on the call that owns the offending lambda.

### `ExplicitBackingFieldRequired`

```kotlin
class ServerEnvironmentStore {
    private val mutableEnvironment = MutableStateFlow(ServerEnvironment.Staging)
    val environment: StateFlow<ServerEnvironment> = mutableEnvironment.asStateFlow() // ERROR

    // OK:
    val environment: StateFlow<ServerEnvironment>
        field = MutableStateFlow(ServerEnvironment.Staging)
}
```

Why: a private property paired with a wider one that merely re-exposes it duplicates one piece of state under two names, and nothing keeps the pair in sync as the class grows. Kotlin's explicit backing field states the same intent in one declaration — the field type inside the class, the property type outside it. The checker fires only when the rewrite is mechanical: both properties are read-only, the private property's type is already a subtype of the exposed type, and its value comes from an initializer rather than a constructor parameter or a getter. Between the two, either a plain read or a read-only view of the same instance (`asStateFlow` / `asSharedFlow`) qualifies; a conversion that widens beyond a subtype (`Channel.receiveAsFlow()`) and a derived value (`Flow.map`) are left alone.

### `PrivateSetRequired`

```kotlin
class Counter {
    private var mutableCount = 0
    val count: Int get() = mutableCount // ERROR

    // OK:
    var count: Int = 0
        private set
}
```

Why: the same duplicated-state problem as `ExplicitBackingFieldRequired`, for the case the field's type cannot express — a private `var` reassigned inside the class and read from outside. `private set` narrows the setter alone, so the pair collapses into one declaration. The two rules partition by type: identical types are a `private set` job, a strictly narrower private type is an explicit backing field one.

## Review + tests (fuzzy)

Rules that depend on data volume or semantics stay out of static enforcement: heavy shaping belonging in the data layer, validity of an emitted result, and correctness of mutation-result handling — ensured by AI review rules + Presenter/Screen tests.

**Eliminate via types what types can eliminate** (required serializer, context param, `suspend`, `internal`), **use FIR checkers for binaries types can't express**, and **leave fuzzy cases to review**. Even when AI writes the code, the type and FIR layers hold because **compilation fails**.

Related: [Architecture overview](./architecture-overview.md) · [Building a screen](./building-a-screen.md) · [ScreenContext design](./screen-context.md) · [Error handling](./error-handling.md)
