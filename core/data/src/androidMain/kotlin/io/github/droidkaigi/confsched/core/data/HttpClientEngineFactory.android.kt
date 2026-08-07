package io.github.droidkaigi.confsched.core.data

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
