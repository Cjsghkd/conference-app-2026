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
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol

private const val ACTION_EFFECT = "ActionEffect"
private const val EMIT = "emit"
private const val SCREEN_CHANNEL = "ScreenChannel"
private const val CORE_COMMON_PACKAGE = "io.github.droidkaigi.confsched.core.common"

// A presenter's `ActionEffect(channel) { ... }` handler whose only effect is forwarding the action
// back out as a result (`channel.emit(...)`) is meaningless indirection: the click should be wired
// straight from the Screen to the Root. Matching is deliberately conservative — a branch (or the
// whole handler body) whose single effectful statement is an `emit(...)` call on a ScreenChannel.
internal object NoForwardOnlyActionChecker : FirFunctionCallChecker(MppCheckerKind.Platform) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val calleeName = expression.calleeReference.toResolvedCallableSymbol()?.name?.asString()
        if (calleeName != ACTION_EFFECT) return

        val handler = expression.argumentList.arguments
            .filterIsInstance<FirAnonymousFunctionExpression>()
            .firstOrNull() ?: return
        val body = handler.anonymousFunction.body ?: return

        val statements = body.effectfulStatements()
        val single = statements.singleOrNull() ?: return

        when (single) {
            is FirWhenExpression -> for (branch in single.branches) {
                val branchStatement = branch.result.effectfulStatements().singleOrNull() ?: continue
                if (branchStatement.isScreenChannelEmit()) {
                    reporter.reportOn(branchStatement.source, NoForwardOnlyActionErrors.NO_FORWARD_ONLY_ACTION, context)
                }
            }

            else -> if (single.isScreenChannelEmit()) {
                reporter.reportOn(single.source, NoForwardOnlyActionErrors.NO_FORWARD_ONLY_ACTION, context)
            }
        }
    }
}

private fun FirElement.effectfulStatements(): List<FirStatement> = when (this) {
    is FirBlock -> statements
    is FirStatement -> listOf(this)
    else -> emptyList()
}

private fun FirStatement.isScreenChannelEmit(): Boolean {
    val call = this as? FirFunctionCall ?: return false
    val symbol = call.calleeReference.toResolvedCallableSymbol() ?: return false
    val callableId = symbol.callableId ?: return false
    return callableId.callableName.asString() == EMIT &&
        callableId.className?.shortName()?.asString() == SCREEN_CHANNEL &&
        callableId.packageName.asString() == CORE_COMMON_PACKAGE
}

object NoForwardOnlyActionErrors : KtDiagnosticsContainer() {
    val NO_FORWARD_ONLY_ACTION by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = NoForwardOnlyActionErrorMessages
}

object NoForwardOnlyActionErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("NoForwardOnlyAction") { map ->
        map.put(
            NoForwardOnlyActionErrors.NO_FORWARD_ONLY_ACTION,
            "This action handler only forwards the action as a result. Wire the UI callback " +
                "directly to the Root instead of routing it through the presenter.",
        )
    }
}
