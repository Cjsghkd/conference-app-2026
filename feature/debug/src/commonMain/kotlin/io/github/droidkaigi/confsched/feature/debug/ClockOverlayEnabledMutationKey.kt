package io.github.droidkaigi.confsched.feature.debug

import dev.zacsweers.metro.Qualifier
import soil.query.MutationKey

typealias ClockOverlayEnabledMutationKey = MutationKey<Unit, Boolean>

/**
 * Separates this key from the other `MutationKey<Unit, Boolean>` here. Both carry a qualifier, so
 * neither is the unqualified one an injection site reaches by forgetting its own.
 */
@Qualifier
annotation class ClockOverlayEnabled
