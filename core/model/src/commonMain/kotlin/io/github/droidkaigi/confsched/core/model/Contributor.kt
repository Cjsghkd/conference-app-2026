package io.github.droidkaigi.confsched.core.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class ContributorId(val value: Long)

@Serializable
data class Contributor(
    val id: ContributorId,
    val username: String,
    val iconUrl: String,
    val profileUrl: String,
)

@Serializable
data class Contributors(
    val items: PersistentList<Contributor>,
)
