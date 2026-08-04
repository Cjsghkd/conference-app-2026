package io.github.droidkaigi.confsched.enforcement

import com.intellij.psi.PsiElement
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
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private const val FEATURE_PACKAGE_PREFIX = "io.github.droidkaigi.confsched.feature"

private val SAFE_CLICK_SINK_NAMES = setOf("safeClick", "safeClickable")
private const val ACTION_RESULT_EFFECT = "ActionResultEffect"

private val COMPOSABLE_ID = ClassId(FqName("androidx.compose.runtime"), Name.identifier("Composable"))

private fun String.isOnCallbackName(): Boolean =
    length > 2 && startsWith("on") && this[2].isUpperCase()

private fun FirTypeRef.isFunctionType(): Boolean {
    val classId = coneType.classId ?: return false
    return classId.asString().startsWith("kotlin/Function")
}

internal object NavLambdaMustFlowToSafeClickChecker : FirSimpleFunctionChecker(MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirNamedFunction) {
        val session = context.session
        val symbol = declaration.symbol

        val name = symbol.name.asString()
        if (!name.endsWith("Screen") && !name.endsWith("ScreenRoot")) return
        if (!symbol.hasAnnotation(COMPOSABLE_ID, session)) return
        val pkg = symbol.callableId.packageName.asString()
        if (!pkg.startsWith(FEATURE_PACKAGE_PREFIX)) return

        val targets = declaration.valueParameters
            .filter { it.name.asString().isOnCallbackName() && it.returnTypeRef.isFunctionType() }
            .map { it.symbol }
            .toSet()
        if (targets.isEmpty()) return

        val body = declaration.body ?: return
        val ancestors = ArrayDeque<FirElement>()
        val violations = mutableListOf<FirElement>()

        val visitor = object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                if (element is FirQualifiedAccessExpression) {
                    val referenced = element.calleeReference.toResolvedCallableSymbol()
                    if (referenced is FirValueParameterSymbol && referenced in targets) {
                        if (!isAllowedUsage(element, ancestors)) violations += element
                    }
                }
                ancestors.addLast(element)
                element.acceptChildren(this)
                ancestors.removeLast()
            }
        }
        body.accept(visitor)

        for (misuse in violations) {
            reporter.reportOn(misuse.source, SafeClickErrors.NAV_LAMBDA_MUST_FLOW_TO_SAFE_CLICK, context)
        }
    }
}

private fun isAllowedUsage(ref: FirQualifiedAccessExpression, ancestors: List<FirElement>): Boolean {
    val parent = ancestors.lastOrNull()

    if (parent is FirFunctionCall && parent.explicitReceiver === ref) {
        val lambdaIndex = ancestors.indexOfLast { it is FirAnonymousFunction }
        if (lambdaIndex < 0) return false
        val enclosingCall = (lambdaIndex - 1 downTo 0)
            .asSequence()
            .map { ancestors[it] }
            .filterIsInstance<FirFunctionCall>()
            .firstOrNull() ?: return false
        val calleeName = enclosingCall.calleeReference.toResolvedCallableSymbol()?.name?.asString()
        return calleeName in SAFE_CLICK_SINK_NAMES || calleeName == ACTION_RESULT_EFFECT
    }

    val callIndex = ancestors.indexOfLast { it is FirFunctionCall }
    if (callIndex < 0) return false
    for (i in (callIndex + 1)..ancestors.lastIndex) {
        val between = ancestors[i]
        if (between is FirAnonymousFunction || between is FirFunctionCall) return false
    }
    val call = ancestors[callIndex] as FirFunctionCall
    val calleeSymbol = call.calleeReference.toResolvedCallableSymbol() ?: return false
    val calleeName = calleeSymbol.name.asString()
    if (calleeName in SAFE_CLICK_SINK_NAMES) return true
    val calleePkg = calleeSymbol.callableId?.packageName?.asString() ?: return false
    return calleePkg.startsWith(FEATURE_PACKAGE_PREFIX)
}

object SafeClickErrors : KtDiagnosticsContainer() {
    val NAV_LAMBDA_MUST_FLOW_TO_SAFE_CLICK by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = SafeClickErrorMessages
}

object SafeClickErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("SafeClick") { map ->
        map.put(
            SafeClickErrors.NAV_LAMBDA_MUST_FLOW_TO_SAFE_CLICK,
            "A navigation-bound callback (on*) in a feature Screen must flow into a safeClick sink. " +
                "Wrap it with safeClick { … } / Modifier.safeClickable { … } (or forward it into " +
                "another feature @Composable's on* parameter). A direct invocation, or passing it " +
                "into a library click like Button(onClick = it), is not debounced — wrap it.",
        )
    }
}
