package io.github.droidkaigi.confsched.enforcement

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory

object NoDirectMutateErrors : KtDiagnosticsContainer() {
    val NO_DIRECT_MUTATE by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = NoDirectMutateErrorMessages
}

object NoDirectMutateErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("NoDirectMutate") { map ->
        map.put(
            NoDirectMutateErrors.NO_DIRECT_MUTATE,
            "Accessing Soil mutation 'mutate' is forbidden (calling or aliasing it bypasses the " +
                "MutationState transition, so MutatedEffect / MutationErrorEffect never fire). " +
                "Use 'mutateAsync(...)'.",
        )
    }
}
