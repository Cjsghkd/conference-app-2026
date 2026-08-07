package io.github.droidkaigi.confsched.core.data

enum class ServerEnvironment(val baseUrl: String?) {
    Fake(baseUrl = null),
    Staging(baseUrl = "https://staging-api.droidkaigi.jp/"),
    Production(baseUrl = "https://ssot-api.droidkaigi.jp/"),
}
