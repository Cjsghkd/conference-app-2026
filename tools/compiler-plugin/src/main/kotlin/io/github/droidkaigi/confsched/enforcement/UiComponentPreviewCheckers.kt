package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isUnit
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid

private const val EFFECT_SUFFIX = "Effect"

internal object UiComponentRequiresPreviewChecker : FirFileChecker(MppCheckerKind.Platform) {

    @OptIn(DirectDeclarationsAccess::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) {
        val session = context.session
        val packageName = declaration.packageDirective.packageFqName.asString()
        if (!packageName.startsWith(SoilReadConfinementNames.FEATURE_PACKAGE_PREFIX)) return

        val topLevelFunctions = declaration.declarations.filterIsInstance<FirNamedFunction>()
        val composables = topLevelFunctions
            .filter { it.symbol.hasAnnotation(PreviewWrapperNames.COMPOSABLE_ID, session) }
            .filter { it.symbol.resolvedReturnTypeRef.coneType.isUnit }

        val (previews, components) = composables.partition { it.symbol.previewAnnotations(session).isPreview }
        if (components.isEmpty()) return

        val rendered = renderedFrom(previews, topLevelFunctions.associateBy { it.symbol })

        for (component in components) {
            if (component.symbol in rendered) continue
            // A ScreenContext is supplied by the screen's dependency graph, which a preview has no
            // access to, so a role-gated composable (every *ScreenRoot) cannot be called from one.
            if (component.symbol.contextParameterSymbols.isNotEmpty()) continue
            // An expect declaration has no body, and the tooling renders previews through the common
            // and Android views only, so a preview placed beside an actual would never run.
            if (component.symbol.isExpect || component.symbol.isActual) continue
            // An *Effect composable runs side effects and emits nothing, so its preview would be blank.
            if (component.name.asString().endsWith(EFFECT_SUFFIX)) continue
            val source = component.source ?: continue
            reporter.reportOn(
                source,
                UiComponentPreviewErrors.UI_COMPONENT_WITHOUT_PREVIEW,
                component.name.asString(),
                context,
            )
        }
    }

    /**
     * Every function the file's previews reach. A preview usually calls the component inside a
     * wrapper lambda and may route through a helper declared beside it, so the walk descends into
     * call arguments and follows callees that are themselves declared in this file.
     */
    private fun renderedFrom(
        previews: List<FirNamedFunction>,
        inFile: Map<FirNamedFunctionSymbol, FirNamedFunction>,
    ): Set<FirNamedFunctionSymbol> {
        val rendered = mutableSetOf<FirNamedFunctionSymbol>()
        val pending = ArrayDeque(previews)
        val walked = previews.mapTo(mutableSetOf()) { it.symbol }
        while (pending.isNotEmpty()) {
            val body = pending.removeFirst().body ?: continue
            for (callee in body.calledFunctions()) {
                rendered += callee
                val sameFile = inFile[callee] ?: continue
                if (walked.add(callee)) pending += sameFile
            }
        }
        return rendered
    }

    private fun FirElement.calledFunctions(): Set<FirNamedFunctionSymbol> {
        val callees = mutableSetOf<FirNamedFunctionSymbol>()
        accept(
            object : FirVisitorVoid() {
                override fun visitElement(element: FirElement) {
                    if (element is FirFunctionCall) {
                        val callee = element.calleeReference.toResolvedCallableSymbol()
                        if (callee is FirNamedFunctionSymbol) callees += callee
                    }
                    element.acceptChildren(this)
                }
            },
        )
        return callees
    }
}
