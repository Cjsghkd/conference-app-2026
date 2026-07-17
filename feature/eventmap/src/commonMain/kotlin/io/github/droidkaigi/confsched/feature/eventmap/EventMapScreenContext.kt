package io.github.droidkaigi.confsched.feature.eventmap

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext
import io.github.droidkaigi.confsched.core.model.EventMapScreenScope

@Inject
class EventMapPresenterContext : PresenterContext

@Inject
@SingleIn(EventMapScreenScope::class)
class EventMapScreenContext(
    val presenterContext: EventMapPresenterContext,
) : ScreenContext
