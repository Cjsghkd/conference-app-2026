package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.KtRealSourceElementKind
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
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
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirAnonymousFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.utils.isInfix
import org.jetbrains.kotlin.fir.declarations.utils.isOperator
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.functionTypeKind

// Flags a lambda whose entire body is one call forwarding the lambda parameters unchanged and in
// order — the lambda can be replaced with a callable reference. Deliberately conservative: any
// shape where a reference cannot substitute (suspend or receiver-typed function type, varargs,
// named/reordered arguments, non-trivial receiver expression) is left alone. @Composable lambdas
// are excluded by choice, not by necessity: a composable reference compiles, but '::Title' at a
// content slot hides the call syntax that marks a composable.
internal object LambdaCanBeCallableReferenceChecker : FirAnonymousFunctionChecker(MppCheckerKind.Platform) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirAnonymousFunction) {
        if (declaration.source?.kind !is KtRealSourceElementKind) return
        if (declaration.receiverParameter != null) return
        if (declaration.contextParameters.isNotEmpty()) return

        val lambdaType = (declaration.typeRef as? FirResolvedTypeRef)?.coneType ?: return
        if (lambdaType.functionTypeKind(context.session) != FunctionTypeKind.Function) return

        val call = declaration.body?.statements?.singleOrNull()?.unwrapReturn() as? FirFunctionCall ?: return
        if (call.typeArguments.any { it.source?.kind is KtRealSourceElementKind }) return

        val callee = call.calleeReference.toResolvedCallableSymbol() as? FirNamedFunctionSymbol ?: return
        if (callee.name.asString() == "invoke") return
        if (callee.isSuspend || callee.isInfix || callee.isOperator) return
        if (callee.valueParameterSymbols.any { it.isVararg }) return
        if (callee.valueParameterSymbols.size != declaration.valueParameters.size) return

        val arguments = call.argumentList.arguments
        if (arguments.size != declaration.valueParameters.size) return
        val forwardsAllParameters = declaration.valueParameters.withIndex().all { (index, parameter) ->
            val access = arguments.getOrNull(index) as? FirPropertyAccessExpression ?: return@all false
            access.explicitReceiver == null &&
                access.calleeReference.toResolvedCallableSymbol() == parameter.symbol
        }
        if (!forwardsAllParameters) return

        if (!call.explicitReceiver.isStableReferenceReceiver()) return

        reporter.reportOn(
            declaration.source,
            CallableReferenceErrors.LAMBDA_CAN_BE_CALLABLE_REFERENCE,
            context,
        )
    }

    private fun FirStatement.unwrapReturn(): FirStatement =
        (this as? FirReturnExpression)?.result ?: this

    // A reference captures its receiver once, so only accept receivers whose evaluation is a plain
    // read of immutable state — `this`, an object, or a chain of val/parameter accesses.
    private fun FirExpression?.isStableReferenceReceiver(): Boolean = when (this) {
        null -> true

        is FirThisReceiverExpression -> true

        is FirResolvedQualifier -> true

        is FirPropertyAccessExpression -> {
            when (val symbol = calleeReference.toResolvedCallableSymbol()) {
                is FirValueParameterSymbol -> explicitReceiver.isStableReferenceReceiver()
                is FirPropertySymbol -> symbol.isVal && explicitReceiver.isStableReferenceReceiver()
                else -> false
            }
        }

        else -> false
    }
}

object CallableReferenceErrors : KtDiagnosticsContainer() {
    val LAMBDA_CAN_BE_CALLABLE_REFERENCE by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = CallableReferenceErrorMessages
}

object CallableReferenceErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("CallableReference") { map ->
        map.put(
            CallableReferenceErrors.LAMBDA_CAN_BE_CALLABLE_REFERENCE,
            "This lambda only forwards its arguments unchanged. Replace it with a callable " +
                "reference ('receiver::function').",
        )
    }
}
