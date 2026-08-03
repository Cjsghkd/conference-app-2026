package io.github.droidkaigi.confsched.core.model

import kotlinx.collections.immutable.PersistentList
import kotlin.jvm.JvmInline

@JvmInline
value class ContributorId(val value: Long)

data class Contributor(
    val id: ContributorId,
    val username: String,
    val iconUrl: String,
    val profileUrl: String,
)

data class Contributors(
    val items: PersistentList<Contributor>,
)
