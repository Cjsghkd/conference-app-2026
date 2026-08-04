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
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid

private const val CORE_COMMON_PACKAGE = "io.github.droidkaigi.confsched.core.common"
private val EFFECT_NAMES = setOf("MutationSuccessEffect", "MutationErrorEffect")
private const val SOIL_COMPOSE_PACKAGE = "soil.query.compose"
private const val MUTATION_OBJECT = "MutationObject"
private const val RESET_PROPERTY = "reset"

// A consumed mutation state survives in the Soil cache beyond the screen instance; without an
// explicit reset() the next instance of the screen re-fires the stale success/error. The handler
// must therefore call reset() somewhere — when is up to the use case.
internal object MutationEffectMustResetChecker : FirFunctionCallChecker(MppCheckerKind.Platform) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callableId = expression.calleeReference.toResolvedCallableSymbol()?.callableId ?: return
        if (callableId.callableName.asString() !in EFFECT_NAMES) return
        if (callableId.packageName.asString() != CORE_COMMON_PACKAGE) return

        val handler = expression.argumentList.arguments
            .filterIsInstance<FirAnonymousFunctionExpression>()
            .lastOrNull() ?: return
        if (handler.anonymousFunction.containsResetInvoke()) return

        reporter.reportOn(
            expression.source,
            MutationEffectResetErrors.MUTATION_EFFECT_MUST_RESET,
            context,
        )
    }

    private fun FirElement.containsResetInvoke(): Boolean {
        var found = false
        accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                if (found) return
                if (element is FirFunctionCall && element.isResetInvoke()) {
                    found = true
                    return
                }
                element.acceptChildren(this)
            }
        })
        return found
    }

    // Soil's `reset` is a `val: suspend () -> Unit` property, so `mutation.reset()` desugars to
    // invoke on the reset property access — match that receiver, like NoDirectMutateChecker.
    private fun FirFunctionCall.isResetInvoke(): Boolean {
        val receiver = explicitReceiver as? FirResolvable ?: return false
        val callableId = receiver.toResolvedCallableSymbol()?.callableId ?: return false
        return callableId.callableName.asString() == RESET_PROPERTY &&
            callableId.packageName.asString() == SOIL_COMPOSE_PACKAGE &&
            callableId.className?.shortName()?.asString() == MUTATION_OBJECT
    }
}

object MutationEffectResetErrors : KtDiagnosticsContainer() {
    val MUTATION_EFFECT_MUST_RESET by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = MutationEffectResetErrorMessages
}

object MutationEffectResetErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("MutationEffectReset") { map ->
        map.put(
            MutationEffectResetErrors.MUTATION_EFFECT_MUST_RESET,
            "This handler never calls reset() on the mutation. The consumed state stays in the " +
                "Soil cache and re-fires on the next instance of the screen — call " +
                "'mutation.reset()' once the result is handled (the timing is up to the use case).",
        )
    }
}
