package io.github.droidkaigi.confsched.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import io.github.droidkaigi.confsched.core.common.LocalSafeClickInvoker


fun Modifier.safeClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val invoker = LocalSafeClickInvoker.current
    clickable(enabled = enabled, onClickLabel = onClickLabel, role = role) {
        invoker.invoke(onClick)
    }
}

@Composable
fun safeClick(onClick: () -> Unit): () -> Unit {
    val invoker = LocalSafeClickInvoker.current
    return remember(invoker, onClick) { { invoker.invoke(onClick) } }
}

@Composable
fun <T> safeClick(onClick: (T) -> Unit): (T) -> Unit {
    val invoker = LocalSafeClickInvoker.current
    return remember(invoker, onClick) { { arg: T -> invoker.invoke { onClick(arg) } } }
}
