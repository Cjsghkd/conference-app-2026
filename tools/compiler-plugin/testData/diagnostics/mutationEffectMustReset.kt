import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.MutationErrorEffect
import soil.query.compose.MutationObject

@Composable
fun withoutReset(mutation: MutationObject<Unit, String>) {
    <!MUTATION_EFFECT_MUST_RESET!>MutationErrorEffect(mutation) { error ->
        report(error)
    }<!>
}

@Composable
fun withReset(mutation: MutationObject<Unit, String>) {
    MutationErrorEffect(mutation) { error ->
        report(error)
        mutation.reset()
    }
}

fun report(error: Throwable) {
}
