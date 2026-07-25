package io.github.droidkaigi.confsched.feature.debug

import soil.query.core.ErrorRecord

data class SoilErrorsScreenUiState(
    val errors: List<ErrorRecord>,
)
