package io.github.droidkaigi.confsched.core.common

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation3.ui.NavDisplay

/**
 * NavEntry metadata that makes navigation to (and popping back to) the entry snap into place
 * with no animation. Meant for root tab destinations, where an animated transition reads as a
 * push instead of a tab switch.
 */
fun instantNavTransition(): Map<String, Any> =
    NavDisplay.transitionSpec { ContentTransform(EnterTransition.None, ExitTransition.None) } +
        NavDisplay.popTransitionSpec { ContentTransform(EnterTransition.None, ExitTransition.None) } +
        NavDisplay.predictivePopTransitionSpec { _ ->
            ContentTransform(EnterTransition.None, ExitTransition.None)
        }
