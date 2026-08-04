// FILE: SearchNavigator.kt
import io.github.droidkaigi.confsched.core.common.Navigator
import io.github.droidkaigi.confsched.core.common.ScreenContext

interface SearchNavigator : Navigator

class SearchScreenContext(<!NAVIGATOR_NOT_CONFINED!>val navigator: SearchNavigator<!>) : ScreenContext

data class SearchUiState(<!NAVIGATOR_NOT_CONFINED!>val navigator: SearchNavigator<!>)

// FILE: SearchScreen.kt
import androidx.compose.runtime.Composable

@Composable
fun SearchScreen(<!NAVIGATOR_NOT_CONFINED!>navigator: SearchNavigator<!>) {
}

// FILE: OkSearchScreen.kt
import androidx.compose.runtime.Composable

@Composable
fun OkSearchScreen(onNavigateToDetail: () -> Unit) {
}
