package io.github.droidkaigi.confsched.core.model

import kotlinx.collections.immutable.PersistentList
import kotlin.jvm.JvmInline

@JvmInline
value class StaffId(val value: Long)

data class StaffMember(
    val id: StaffId,
    val username: String,
    val iconUrl: String,
    val profileUrl: String,
)

data class Staff(
    val items: PersistentList<StaffMember>,
) {
    companion object
}
