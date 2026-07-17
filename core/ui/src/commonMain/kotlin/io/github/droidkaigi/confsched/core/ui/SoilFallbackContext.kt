package io.github.droidkaigi.confsched.core.ui

import soil.plant.compose.reacty.ErrorBoundaryContext

interface SoilFallbackContext

interface SoilSuspenseContext : SoilFallbackContext

interface SoilErrorContext : SoilFallbackContext {
    val errorBoundaryContext: ErrorBoundaryContext
}

internal class DefaultSoilSuspenseContext : SoilSuspenseContext

internal class DefaultSoilErrorContext(
    override val errorBoundaryContext: ErrorBoundaryContext,
) : SoilErrorContext
