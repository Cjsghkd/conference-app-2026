package io.github.droidkaigi.confsched.feature.search

import io.github.droidkaigi.confsched.core.common.PresenterContext
import io.github.droidkaigi.confsched.core.common.ScreenContext

class SearchPresenterContext : PresenterContext

<!SCREEN_CONTEXT_IS_PRESENTER_CONTEXT!>class SearchScreenContext : ScreenContext, PresenterContext<!>

class OkSearchScreenContext(val presenterContext: SearchPresenterContext) : ScreenContext
