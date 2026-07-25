package io.github.droidkaigi.confsched.core.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.droidkaigi.confsched.core.model.SoilIds
import io.github.droidkaigi.confsched.core.model.Timetable
import io.github.droidkaigi.confsched.core.model.TimetableQueryKey
import kotlinx.collections.immutable.toPersistentList

@Inject
@ContributesBinding(AppScope::class)
class DefaultTimetableQueryKey(
    private val api: TimetableApi,
    private val fileStorage: ServerEnvironmentScopedFileStorage,
) : TimetableQueryKey by buildPersistedQueryKey(
    id = SoilIds.timetableQuery,
    persistKey = "timetable",
    fileStorage = fileStorage,
    fetchResponse = { api.getTimetable() },
    transformToDomainModel = { response -> Timetable(items = response.toTimetableItems().toPersistentList()) },
)
