package io.github.droidkaigi.confsched.core.common

data class UserMessage(val text: String)

fun Throwable.toUserMessage(): UserMessage = UserMessage(message ?: "Unexpected error")
