package io.github.droidkaigi.confsched.core.data

enum class ServerEnvironment(val baseUrl: String?) {
    Fake(baseUrl = null),
    Staging(baseUrl = "https://ssot-api-staging.an.r.appspot.com/"),

    // The production host is not confirmed yet; update before release.
    Production(baseUrl = "https://ssot-api.an.r.appspot.com/"),
}
