package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object ThemeSensitiveNames {
    val THEME_SENSITIVE_CLASS_ID = ClassId(
        FqName("io.github.droidkaigi.confsched.core.common"),
        Name.identifier("ThemeSensitive"),
    )
    val THEME_SENSITIVE_FQN = THEME_SENSITIVE_CLASS_ID.asSingleFqName()

    // Reading any of these MaterialTheme members makes a function inherently theme-sensitive,
    // so such reads are automatic roots of the transitive detection (no annotation required).
    val MATERIAL_THEME_CLASS_ID = ClassId(
        FqName("androidx.compose.material3"),
        Name.identifier("MaterialTheme"),
    )
    val MATERIAL_THEME_GETTER_NAMES: Set<Name> =
        setOf("colorScheme", "typography", "shapes").mapTo(mutableSetOf(), Name::identifier)
}

// A preview is theme-sensitive when its body transitively reaches a theme read. A callee counts
// as a theme read when it (1) carries @ThemeSensitive from source or from the metadata synthesized
// by ThemeSensitiveIrExtension for cross-module callees, or (2) reads a MaterialTheme member
// (colorScheme/typography/shapes). Same-module intermediate callers are not yet annotated (IR runs
// after this checker), so their bodies are walked here via a memoized reachability search.
internal object ThemeSensitivePreviewChecker : FirSimpleFunctionChecker(MppCheckerKind.Platform) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirNamedFunction) {
        val session = context.session
        val symbol = declaration.symbol
        if (!symbol.hasAnnotation(PreviewWrapperNames.COMPOSABLE_ID, session)) return
        val preview = symbol.previewAnnotations(session)
        if (!preview.isPreview) return

        val body = declaration.body ?: return
        if (!body.referencesThemeSensitive(cache = HashMap(), visiting = HashSet())) return

        val rendersEveryScheme = declaration.valueParameters.any { parameter ->
            parameter.symbol.resolvedCompilerAnnotationsWithClassIds
                .firstOrNull {
                    it.toAnnotationClassId(session) == PreviewWrapperNames.PREVIEW_PARAMETER_ANNOTATION_ID
                }
                ?.classArgument(PreviewWrapperNames.PROVIDER_ARGUMENT_NAME) ==
                PreviewWrapperNames.KAIGI_SCHEME_PROVIDER_ID
        }
        if (!rendersEveryScheme) {
            reporter.reportOn(
                declaration.source,
                ThemeSensitiveErrors.THEME_SENSITIVE_PREVIEW_REQUIRES_MULTI_THEME,
                context,
            )
        }
    }

    private fun isMaterialThemeRead(symbol: FirCallableSymbol<*>): Boolean {
        if (symbol !is FirPropertySymbol) return false
        val callableId = symbol.callableId ?: return false
        return callableId.classId == ThemeSensitiveNames.MATERIAL_THEME_CLASS_ID &&
            callableId.callableName in ThemeSensitiveNames.MATERIAL_THEME_GETTER_NAMES
    }

    // Walks a body (already BODY_RESOLVE-resolved for same-module declarations) collecting every
    // resolved callee, delegating the theme-sensitivity decision to isThemeSensitive.
    context(context: CheckerContext)
    private fun FirElement.referencesThemeSensitive(
        cache: MutableMap<FirCallableSymbol<*>, Boolean>,
        visiting: MutableSet<FirCallableSymbol<*>>,
    ): Boolean {
        var found = false
        accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                if (found) return
                if (element is FirQualifiedAccessExpression) {
                    val callee = element.toResolvedCallableSymbol()
                    if (callee != null && isThemeSensitive(callee, cache, visiting)) {
                        found = true
                        return
                    }
                }
                element.acceptChildren(this)
            }
        })
        return found
    }

    // symbol.fir reaches another declaration's body; safe here because the checker runs after
    // BODY_RESOLVE, so same-module callees already have resolved bodies.
    @OptIn(org.jetbrains.kotlin.fir.symbols.SymbolInternals::class)
    context(context: CheckerContext)
    private fun isThemeSensitive(
        symbol: FirCallableSymbol<*>,
        cache: MutableMap<FirCallableSymbol<*>, Boolean>,
        visiting: MutableSet<FirCallableSymbol<*>>,
    ): Boolean {
        cache[symbol]?.let { return it }
        if (isMaterialThemeRead(symbol)) {
            cache[symbol] = true
            return true
        }
        if (symbol is FirNamedFunctionSymbol &&
            symbol.hasAnnotation(ThemeSensitiveNames.THEME_SENSITIVE_CLASS_ID, context.session)
        ) {
            cache[symbol] = true
            return true
        }
        // A callee already on the current path forms a cycle; treat it as not-yet-sensitive here
        // without caching, so the enclosing frame still decides the callee's real value.
        if (symbol in visiting) return false
        // Cross-module callees deserialize without a body; their transitive marker, if any, was
        // already observed above through the metadata annotation.
        val body = (symbol.fir as? FirFunction)?.body ?: run {
            cache[symbol] = false
            return false
        }
        visiting.add(symbol)
        val result = body.referencesThemeSensitive(cache, visiting)
        visiting.remove(symbol)
        cache[symbol] = result
        return result
    }
}

object ThemeSensitiveErrors : KtDiagnosticsContainer() {
    val THEME_SENSITIVE_PREVIEW_REQUIRES_MULTI_THEME by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = ThemeSensitiveErrorMessages
}

object ThemeSensitiveErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("ThemeSensitive") { map ->
        map.put(
            ThemeSensitiveErrors.THEME_SENSITIVE_PREVIEW_REQUIRES_MULTI_THEME,
            "This preview renders @ThemeSensitive content, so it must be previewed under every " +
                "theme: give it a `@PreviewParameter(KaigiSchemeProvider::class) colorScheme: " +
                "KaigiColorScheme` parameter and open `KaigiPreviewTheme(colorScheme) { … }` in the body.",
        )
    }
}
