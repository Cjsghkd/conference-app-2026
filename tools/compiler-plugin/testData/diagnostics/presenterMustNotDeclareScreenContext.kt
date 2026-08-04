package io.github.droidkaigi.confsched.feature.search

import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext

interface SearchScreenContext : ScreenContext

interface SearchPresenterContext : PresenterContext

context(<!PRESENTER_DECLARES_SCREEN_CONTEXT!>_: SearchScreenContext<!>, _: SearchPresenterContext)
fun searchPresenter(): Int = 0

context(_: SearchPresenterContext)
fun okSearchPresenter(): Int = 0
