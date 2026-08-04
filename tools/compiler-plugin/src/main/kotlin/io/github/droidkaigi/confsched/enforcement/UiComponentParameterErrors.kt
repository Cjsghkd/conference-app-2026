package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers

object UiComponentParameterErrors : KtDiagnosticsContainer() {
    val UI_COMPONENT_IGNORES_PARAMETER_PROPERTIES by error2<PsiElement, String, String>(
        SourceElementPositioningStrategies.NAME_IDENTIFIER,
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = UiComponentParameterErrorMessages
}

object UiComponentParameterErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("UiComponentParameter") { map ->
        map.put(
            UiComponentParameterErrors.UI_COMPONENT_IGNORES_PARAMETER_PROPERTIES,
            "A UI @Composable takes what it reads: ''{0}'' carries {1}, which this component never " +
                "reads. Declare a UiState type for this component holding only the properties it " +
                "reads, or take those properties as separate parameters.",
            CommonRenderers.STRING,
            CommonRenderers.STRING,
        )
    }
}
