package io.github.droidkaigi.confsched.core.data

import io.github.droidkaigi.confsched.core.model.Staff
import io.github.droidkaigi.confsched.core.model.StaffId
import io.github.droidkaigi.confsched.core.model.StaffMember
import kotlinx.collections.immutable.toPersistentList

fun StaffListResponse.toStaff(): Staff = Staff(
    items = staff.map(StaffResponse::toStaffMember).toPersistentList(),
)

private fun StaffResponse.toStaffMember(): StaffMember = StaffMember(
    id = StaffId(id),
    username = username,
    // The payload carries four icon sizes; 128px is the largest and covers the grid cell at every density.
    iconUrl = icon128Url,
    profileUrl = profileUrl,
)
