package io.github.droidkaigi.confsched.core.data

import io.github.droidkaigi.confsched.core.model.MultiLangText

/** A side the payload leaves empty falls back to the other, so neither language renders blank. */
internal fun LocaledResponse.toMultiLangText(): MultiLangText = MultiLangText(
    ja = ja.ifEmpty { en },
    en = en.ifEmpty { ja },
)
