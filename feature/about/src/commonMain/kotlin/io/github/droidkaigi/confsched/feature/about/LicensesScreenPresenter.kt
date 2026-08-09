package io.github.droidkaigi.confsched.feature.about

import androidx.compose.runtime.Composable
import com.mikepenz.aboutlibraries.Libs

@Composable
context(_: LicensesPresenterContext)
fun licensesScreenPresenter(libs: Libs): LicensesScreenUiState {
    return LicensesScreenUiState(libs = libs)
}
