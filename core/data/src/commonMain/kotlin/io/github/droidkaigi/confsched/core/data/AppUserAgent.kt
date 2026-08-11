package io.github.droidkaigi.confsched.core.data

import io.github.droidkaigi.confsched.core.common.TargetPlatform
import io.github.droidkaigi.confsched.core.common.currentPlatform
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.UserAgent

internal fun HttpClientConfig<*>.installAppUserAgent() {
    install(UserAgent) {
        agent = "DroidKaigiConferenceApp/2026 ($userAgentPlatformToken)"
    }
}

// Renaming an enum constant must not change what goes out on the wire, so the tokens are spelled out here.
// Chromium discards a User-Agent set by the caller, so the Web token reaches a server only on WebKit and Gecko.
private val userAgentPlatformToken: String
    get() = when (currentPlatform) {
        TargetPlatform.Android -> "Android"
        TargetPlatform.Ios -> "iOS"
        TargetPlatform.Desktop -> "Desktop"
        TargetPlatform.Web -> "Web"
    }
