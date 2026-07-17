package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol
import org.jetbrains.kotlin.name.ClassId

internal object SoilReadConfinementNames {
    const val SOIL_PACKAGE = "soil.query.compose"
    const val REMEMBER_QUERY = "rememberQuery"
    const val REMEMBER_SUBSCRIPTION = "rememberSubscription"
    const val REMEMBER_MUTATION = "rememberMutation"

    const val SOIL_QUERY_PACKAGE = "soil.query"
    const val MUTATE = "mutate"
    const val MUTATE_ASYNC = "mutateAsync"

    const val FEATURE_PACKAGE_PREFIX = "io.github.droidkaigi.confsched.feature"
}

private fun FirClassLikeSymbol<*>.allSuperTypeClassIds(session: FirSession): Set<ClassId> =
    lookupSuperTypes(this, lookupInterfaces = true, deep = true, useSiteSession = session)
        .mapNotNullTo(mutableSetOf()) { it.classId }

private fun FirCallableSymbol<*>.declaresContextOfType(target: ClassId, session: FirSession): Boolean =
    contextParameterSymbols.any { param ->
        val classSymbol = param.resolvedReturnTypeRef.toRegularClassSymbol(session) ?: return@any false
        classSymbol.classId == target || target in classSymbol.allSuperTypeClassIds(session)
    }

internal object SoilReadConfinementChecker : FirFunctionCallChecker(MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val session = context.session
        val callee = expression.toResolvedCallableSymbol() ?: return
        val callableId = callee.callableId ?: return
        if (callableId.packageName.asString() != SoilReadConfinementNames.SOIL_PACKAGE) return

        val requiredContext = when (callableId.callableName.asString()) {
            SoilReadConfinementNames.REMEMBER_QUERY,
            SoilReadConfinementNames.REMEMBER_SUBSCRIPTION,
            -> RoleContextNames.SCREEN_CONTEXT_ID
            SoilReadConfinementNames.REMEMBER_MUTATION -> RoleContextNames.PRESENTER_CONTEXT_ID
            else -> return
        }

        val enclosingPackage = context.containingDeclarations
            .firstNotNullOfOrNull { (it as? FirCallableSymbol<*>)?.callableId?.packageName }
            ?.asString()
            ?: return
        if (!enclosingPackage.startsWith(SoilReadConfinementNames.FEATURE_PACKAGE_PREFIX)) return

        val gated = context.containingDeclarations.any { symbol ->
            symbol is FirCallableSymbol<*> && symbol.declaresContextOfType(requiredContext, session)
        }
        if (gated) return

        val error = if (requiredContext == RoleContextNames.PRESENTER_CONTEXT_ID) {
            SoilReadConfinementErrors.MUTATION_READ_OUTSIDE_PRESENTER_CONTEXT
        } else {
            SoilReadConfinementErrors.QUERY_READ_OUTSIDE_SCREEN_CONTEXT
        }
        reporter.reportOn(expression.source, error, context)
    }
}

// A remembered mutation handle can leak out of the presenter (through UiState or a callback), so
// gating rememberMutation alone is not enough: the mutate/mutateAsync CALL must also sit inside a
// presenter-role function.
internal object MutationCallConfinementChecker : FirFunctionCallChecker(MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val session = context.session
        val callee = expression.toResolvedCallableSymbol() ?: return
        val callableId = callee.callableId ?: return
        if (callableId.packageName.asString() != SoilReadConfinementNames.SOIL_QUERY_PACKAGE) return
        val name = callableId.callableName.asString()
        if (name != SoilReadConfinementNames.MUTATE && name != SoilReadConfinementNames.MUTATE_ASYNC) return

        val enclosingPackage = context.containingDeclarations
            .firstNotNullOfOrNull { (it as? FirCallableSymbol<*>)?.callableId?.packageName }
            ?.asString()
            ?: return
        if (!enclosingPackage.startsWith(SoilReadConfinementNames.FEATURE_PACKAGE_PREFIX)) return

        val gated = context.containingDeclarations.any { symbol ->
            symbol is FirCallableSymbol<*> &&
                symbol.declaresContextOfType(RoleContextNames.PRESENTER_CONTEXT_ID, session)
        }
        if (gated) return

        reporter.reportOn(
            expression.source,
            SoilReadConfinementErrors.MUTATION_CALL_OUTSIDE_PRESENTER_CONTEXT,
            context,
        )
    }
}

object SoilReadConfinementErrors : KtDiagnosticsContainer() {
    val QUERY_READ_OUTSIDE_SCREEN_CONTEXT by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)
    val MUTATION_READ_OUTSIDE_PRESENTER_CONTEXT by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)
    val MUTATION_CALL_OUTSIDE_PRESENTER_CONTEXT by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = SoilReadConfinementErrorMessages
}

object SoilReadConfinementErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("SoilReadConfinement") { map ->
        map.put(
            SoilReadConfinementErrors.QUERY_READ_OUTSIDE_SCREEN_CONTEXT,
            "rememberQuery / rememberSubscription is a Root-role read: the enclosing function must " +
                "declare a ScreenContext context parameter (`context(_: SomeScreenContext)`). " +
                "Read Soil at the screen root, not deep in feature UI.",
        )
        map.put(
            SoilReadConfinementErrors.MUTATION_READ_OUTSIDE_PRESENTER_CONTEXT,
            "rememberMutation is a presenter-role read: the enclosing function must declare a " +
                "PresenterContext context parameter (`context(_: SomePresenterContext)`).",
        )
        map.put(
            SoilReadConfinementErrors.MUTATION_CALL_OUTSIDE_PRESENTER_CONTEXT,
            "mutate / mutateAsync may only be called from a presenter: the enclosing function must " +
                "declare a PresenterContext context parameter (`context(_: SomePresenterContext)`). " +
                "Send an action through the ScreenChannel and mutate inside the presenter's ActionEffect.",
        )
    }
}

