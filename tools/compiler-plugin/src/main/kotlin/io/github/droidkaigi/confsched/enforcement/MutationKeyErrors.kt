package io.github.droidkaigi.confsched.enforcement

// kotlin-compiler-embeddable relocates IntelliJ packages under org.jetbrains.kotlin.com.intellij.
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory

object MutationKeyErrors : KtDiagnosticsContainer() {
    val MUTATION_KEY_WITHOUT_TAG by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    val MUTATION_TAG_NOT_IN_ID by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = MutationKeyErrorMessages
}

object MutationKeyErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("MutationKey") { map ->
        map.put(
            MutationKeyErrors.MUTATION_KEY_WITHOUT_TAG,
            "A MutationKey implementation must take a MutationTag constructor parameter " +
                "(per-screen cache isolation)",
        )
        map.put(
            MutationKeyErrors.MUTATION_TAG_NOT_IN_ID,
            "The MutationTag parameter must be passed into the MutationId",
        )
    }
}
