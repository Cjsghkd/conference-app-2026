package io.github.droidkaigi.confsched.enforcement

// kotlin-compiler-embeddable relocates IntelliJ packages under org.jetbrains.kotlin.com.intellij.
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory

object NavigatorErrors : KtDiagnosticsContainer() {
    val NAVIGATOR_NOT_CONFINED by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = NavigatorErrorMessages
}

object NavigatorErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("Navigator") { map ->
        map.put(
            NavigatorErrors.NAVIGATOR_NOT_CONFINED,
            "A Navigator must stay confined to the NavEntry layer. A Navigator type may appear only " +
                "in NavEntryProvider wiring (and the core nav infrastructure), NOT in a " +
                "ScreenContext/PresenterContext, a presenter / @Composable signature, or a " +
                "UiState/Action/ActionResult. The Root receives navigation as lambdas instead.",
        )
    }
}
