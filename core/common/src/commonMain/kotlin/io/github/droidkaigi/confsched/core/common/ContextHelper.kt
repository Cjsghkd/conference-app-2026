package io.github.droidkaigi.confsched.core.common

inline fun <A, R> context(context: A, block: context(A) () -> R): R = block(context)
