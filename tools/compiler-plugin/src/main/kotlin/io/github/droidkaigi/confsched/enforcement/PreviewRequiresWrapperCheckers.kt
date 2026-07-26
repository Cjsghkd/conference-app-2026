package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object PreviewWrapperNames {
    val COMPOSABLE_ID = ClassId(FqName("androidx.compose.runtime"), Name.identifier("Composable"))
    val JETBRAINS_PREVIEW_ID =
        ClassId(FqName("org.jetbrains.compose.ui.tooling.preview"), Name.identifier("Preview"))
    val ANDROIDX_PREVIEW_ID =
        ClassId(FqName("androidx.compose.ui.tooling.preview"), Name.identifier("Preview"))

    val PREVIEW_WRAPPER_ANNOTATION_ID =
        ClassId(FqName("androidx.compose.ui.tooling.preview"), Name.identifier("PreviewWrapper"))

    val WRAPPER_FUNCTIONS = setOf("KaigiPreviewWrapper", "MultiThemedPreviewTheme")
}

internal object PreviewRequiresWrapperChecker : FirSimpleFunctionChecker(MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirNamedFunction) {
        val session = context.session
        val symbol = declaration.symbol
        if (!symbol.hasAnnotation(PreviewWrapperNames.COMPOSABLE_ID, session)) return
        val isPreview = symbol.hasAnnotation(PreviewWrapperNames.JETBRAINS_PREVIEW_ID, session) ||
            symbol.hasAnnotation(PreviewWrapperNames.ANDROIDX_PREVIEW_ID, session)
        if (!isPreview) return

        // The @PreviewWrapper(wrapper = ...) route wraps at render time; no body wrapping needed.
        if (symbol.hasAnnotation(PreviewWrapperNames.PREVIEW_WRAPPER_ANNOTATION_ID, session)) return

        val topLevelCall = declaration.body?.statements?.firstOrNull()?.unwrapReturn() as? FirFunctionCall
        val callsWrapper =
            topLevelCall?.toResolvedCallableSymbol()?.name?.asString() in PreviewWrapperNames.WRAPPER_FUNCTIONS
        if (!callsWrapper) {
            reporter.reportOn(declaration.source, PreviewRequiresWrapperErrors.PREVIEW_WITHOUT_WRAPPER, context)
        }
    }

    private fun FirStatement.unwrapReturn(): FirStatement =
        if (this is FirReturnExpression) result else this
}

object PreviewRequiresWrapperErrors : KtDiagnosticsContainer() {
    val PREVIEW_WITHOUT_WRAPPER by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = PreviewRequiresWrapperErrorMessages
}

object PreviewRequiresWrapperErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("PreviewRequiresWrapper") { map ->
        map.put(
            PreviewRequiresWrapperErrors.PREVIEW_WITHOUT_WRAPPER,
            "A @Preview @Composable must render inside KaigiTheme with the preview image resolver: " +
                "annotate it with `@PreviewWrapper(wrapper = KaigiPreviewWrapper::class)` (or make " +
                "the body's top-level statement a `MultiThemedPreviewTheme { ... }` call).",
        )
    }
}
