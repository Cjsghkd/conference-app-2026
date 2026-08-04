package io.github.droidkaigi.confsched.core.ui

object Modifier

fun safeClick(onClick: () -> Unit): () -> Unit = onClick

fun <T> safeClick(onClick: (T) -> Unit): (T) -> Unit = onClick

fun Modifier.safeClickable(onClick: () -> Unit): Modifier = this
