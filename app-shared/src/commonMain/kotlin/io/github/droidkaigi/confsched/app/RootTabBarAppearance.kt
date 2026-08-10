package io.github.droidkaigi.confsched.app

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Carries the theme to shells outside the Compose tree (the iOS app), which draw the root tab bar
// natively and so cannot read MaterialTheme. IosTabBarSyncEffect keeps it in step with the
// composition; the palette is null until the first one runs.
@Inject
@SingleIn(AppScope::class)
class RootTabBarAppearance {

    val palette: StateFlow<RootTabBarPalette?>
        field = MutableStateFlow<RootTabBarPalette?>(null)

    internal fun updatePalette(palette: RootTabBarPalette) {
        this.palette.value = palette
    }
}

/**
 * What a natively drawn root tab bar takes from the theme in force.
 *
 * It is deliberately this short. A native bar draws its own material and decides its own
 * unselected colors from it; these two are what the theme still has a say in.
 *
 * @property accentArgb the color marking the destination in view, as sRGB ARGB rather than a
 *   Compose `Color`, which does not cross the framework boundary.
 * @property isDark whether the scheme in force is a dark one, so the bar's material matches it
 *   instead of the device's appearance setting. The schemes carry this rather than derive it: one
 *   of them sits close enough to the midpoint that a luminance threshold would flip on a small
 *   change to its value.
 */
data class RootTabBarPalette(
    val accentArgb: Long,
    val isDark: Boolean,
)
