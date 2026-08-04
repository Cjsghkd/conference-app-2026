package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers

object UiComponentPreviewErrors : KtDiagnosticsContainer() {
    val UI_COMPONENT_WITHOUT_PREVIEW by error1<PsiElement, String>(
        SourceElementPositioningStrategies.DECLARATION_NAME,
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = UiComponentPreviewErrorMessages
}

object UiComponentPreviewErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("UiComponentPreview") { map ->
        map.put(
            UiComponentPreviewErrors.UI_COMPONENT_WITHOUT_PREVIEW,
            "A UI @Composable in a feature module must be inspectable without running the app: " +
                "no @Preview in this file renders ''{0}''. Add a @Preview @Composable that calls " +
                "it with sample data.",
            CommonRenderers.STRING,
        )
    }
}
