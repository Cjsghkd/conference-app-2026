package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isUnit

internal object UiComponentRequiresPreviewChecker : FirFileChecker(MppCheckerKind.Platform) {

    @OptIn(DirectDeclarationsAccess::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) {
        val session = context.session
        val packageName = declaration.packageDirective.packageFqName.asString()
        if (!packageName.startsWith(SoilReadConfinementNames.FEATURE_PACKAGE_PREFIX)) return

        val composables = declaration.declarations
            .filterIsInstance<FirNamedFunction>()
            .filter { it.symbol.hasAnnotation(PreviewWrapperNames.COMPOSABLE_ID, session) }
            .filter { it.symbol.resolvedReturnTypeRef.coneType.isUnit }

        val (previews, components) = composables.partition { it.symbol.previewAnnotations(session).isPreview }
        if (previews.isNotEmpty()) return

        for (component in components) {
            // A ScreenContext is supplied by the screen's dependency graph, which a preview has no
            // access to, so a role-gated composable (every *ScreenRoot) cannot be called from one.
            if (component.symbol.contextParameterSymbols.isNotEmpty()) continue
            // An expect declaration has no body, and the tooling renders previews through the common
            // and Android views only, so a preview placed beside an actual would never run.
            if (component.symbol.isExpect || component.symbol.isActual) continue
            val source = component.source ?: continue
            reporter.reportOn(
                source,
                UiComponentPreviewErrors.UI_COMPONENT_WITHOUT_PREVIEW,
                component.name.asString(),
                context,
            )
        }
    }
}
