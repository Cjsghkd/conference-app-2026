package io.github.droidkaigi.confsched.app

import androidx.compose.ui.window.ComposeUIViewController
import io.github.droidkaigi.confsched.core.common.context
import platform.UIKit.UIViewController

fun kaigiAppViewController(appGraph: IosAppGraph): UIViewController = ComposeUIViewController {
    context(appGraph) {
        val backStack = rememberKaigiBackStack()
        IosTabBarSyncEffect(backStack)
        KaigiApp(backStack)
    }
}
