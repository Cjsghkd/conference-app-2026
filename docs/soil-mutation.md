# Soil mutation

A **mutation** is Soil's write path: toggling a favorite, updating the profile — anything that changes data rather than reads it. A presenter fires a write through a Soil mutation and observes its outcome as state — it never fires and forgets, and it never navigates imperatively; loading, success, and failure all flow back through the mutation's state and its one-off effects.

## How to use

1. **Fire with `mutateAsync`** from an `ActionEffect` handler.
2. **Reflect loading** by mapping the `MutationState` into the UiState where the screen needs it.
3. **Handle success with `MutationSuccessEffect`** — emit the follow-up (navigation, a message) as an `ActionResult`.
4. **Handle failure with `MutationErrorEffect`** (`:core:common`) — emit a `ShowMessage` result the same way.

```kotlin
val mutation = rememberMutation(presenterContext.profileMutationKey)

ActionEffect(screenChannel) { action ->
    when (action) {
        is CreateProfile -> mutation.mutateAsync(action.profile) // mutateAsync, not mutate
    }
}
// success/failure are emitted as a result via screenChannel.emit → Root's ActionResultEffect handles them
MutationSuccessEffect(mutation) {
    screenChannel.emit(NavigateToCard)
    mutation.reset() // required — see "Resetting the mutation state"
}
MutationErrorEffect(mutation) {
    screenChannel.emit(ShowMessage(it.toUserMessage()))
    mutation.reset()
}
```

## Why `mutateAsync`, not `mutate`

`mutate` awaits the result and hands it — **including any thrown exception** — straight back to the call site. That has two consequences:

- **Uncaught errors crash.** A failing fetcher rethrows at the `mutate` call, so every call site needs its own `try`/`catch`; forget one and the app crashes. Used this way, Soil degrades into a plain suspend call.
- **Feedback can be silently lost.** The result only reaches the coroutine awaiting it. If that coroutine is cancelled mid-flight (the screen leaves composition), any follow-up written after `mutate` is dropped — the user never sees the success or the error.

`mutateAsync` instead records the outcome in the mutation state owned by the `SwrClient`, and the effects consume it from there: errors surface as state (no rethrow), and `MutationSuccessEffect` / `MutationErrorEffect` track which success/error they have already handled, so the feedback survives recomposition and caller cancellation. In short, `mutateAsync` is what keeps Soil's mutation cache in the loop.

This is not just a convention: the **`NoDirectMutate` FIR checker** (`:tools:compiler-plugin`, applied to every module) makes any direct `mutate` call a **compile error**, so the misuse cannot ship — see [Enforcement](./enforcement.md).

## MutationSuccessEffect / MutationErrorEffect

Both one-off effects are the project's own (`:core:common`), replacing Soil's `MutatedEffect`. Owning the pair keeps one keying rule — the success side consumes by `replyUpdatedAt`, the error side by `errorUpdatedAt` (timestamps) — and sidesteps `MutatedEffect`'s `mutatedCount` default, which the recomposition optimizer zeroes out. The error side:

```kotlin
// Collects the state via snapshotFlow and consumes each error exactly once, keyed by
// errorUpdatedAt. MutationSuccessEffect is its mirror image (replyUpdatedAt-keyed).
@Composable
fun MutationErrorEffect(
    mutation: MutationObject<*, *>,
    onError: suspend (Throwable) -> Unit,
) {
    val mutationState by rememberUpdatedState(mutation)
    var lastConsumedKey by rememberSaveable { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) {
        snapshotFlow { mutationState as? MutationErrorObject }
            .filterNotNull()
            .collect {
                if (lastConsumedKey != it.errorUpdatedAt) {
                    lastConsumedKey = it.errorUpdatedAt
                    onError(it.error)
                }
            }
    }
}
```

## Resetting the mutation state

The consumed Success/Error stays in the `SwrClient` cache until reset or GC. Two rules keep that residue from misfiring:

- **Cross-screen residue cannot happen by construction**: every `MutationKey` implementation carries a per-screen `MutationTag` in its `MutationId`, so screens never share a mutation cache entry (enforced by the `MutationKeyMustCarryTag` checker — see [Soil keys](./soil-keys.md)).
- **`reset()` is always explicit, never hidden inside an effect.** The handler calls `mutation.reset()` itself. The effects deliberately do not reset for you: a handler is a suspend block, and if it were cancelled at a suspension point (`showSnackbar`, `delay`) before a hidden reset ran, the state would silently survive — when to consume-destructively is a per-screen decision that should be visible at the call site. That the handler resets *somewhere* is not optional, though: without it, the consumed state outlives the screen instance and re-fires on the next one — the `MutationEffectMustReset` checker ([enforcement](./enforcement.md)) rejects a `MutationSuccessEffect` / `MutationErrorEffect` handler that never calls `reset()`.

Related: [Soil keys](./soil-keys.md) · [Error handling](./error-handling.md) (result delivery via `ScreenChannel`)
