package io.github.droidkaigi.confsched.core.data

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

internal actual fun httpClientEngineFactory(): HttpClientEngineFactory<*> = Js
