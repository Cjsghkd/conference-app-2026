package io.github.droidkaigi.confsched.feature.about

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext
import io.github.droidkaigi.confsched.core.model.AboutScreenScope
import io.github.droidkaigi.confsched.core.model.buildconfig.BuildConfigProvider

@Inject
class AboutPresenterContext(
    val buildConfig: BuildConfigProvider,
) : PresenterContext

@Inject
@SingleIn(AboutScreenScope::class)
class AboutScreenContext(
    val presenterContext: AboutPresenterContext,
) : ScreenContext
