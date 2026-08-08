package androidx.compose.runtime.retain

import androidx.compose.runtime.Composable

@Composable
fun <T> retain(calculation: () -> T): T = calculation()

@Composable
fun <T> retain(key1: Any?, calculation: () -> T): T = calculation()
