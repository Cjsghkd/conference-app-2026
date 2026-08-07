package io.github.droidkaigi.confsched.core.data

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
