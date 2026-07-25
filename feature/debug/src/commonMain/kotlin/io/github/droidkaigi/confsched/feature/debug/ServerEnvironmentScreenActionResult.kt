package io.github.droidkaigi.confsched.feature.debug

sealed interface ServerEnvironmentScreenActionResult {
    data object ServerSelected : ServerEnvironmentScreenActionResult
}
