package io.github.droidkaigi.confsched.app.buildconfig

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.droidkaigi.confsched.BuildKonfig
import io.github.droidkaigi.confsched.core.model.buildconfig.BuildConfigProvider

@ContributesBinding(AppScope::class)
@Inject
class DefaultBuildConfigProvider : BuildConfigProvider {
    override val versionName: String
        get() = BuildKonfig.versionName
}
