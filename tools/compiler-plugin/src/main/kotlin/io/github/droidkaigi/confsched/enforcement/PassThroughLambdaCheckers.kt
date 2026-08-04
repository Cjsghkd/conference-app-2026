package io.github.droidkaigi.confsched.enforcement

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.KtRealSourceElementKind
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
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.types.AbstractTypeChecker

// Complements LambdaCanBeCallableReference: this one covers a lambda that merely invokes a function
// value already in scope, which is not a callable reference and stays legal for @Composable and
// suspend types.
internal object LambdaCanBePassedDirectlyChecker : FirAnonymousFunctionChecker(MppCheckerKind.Platform) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirAnonymousFunction) {
        if (declaration.source?.kind !is KtRealSourceElementKind) return
        if (declaration.receiverParameter != null) return
        if (declaration.contextParameters.isNotEmpty()) return

        val lambdaType = (declaration.typeRef as? FirResolvedTypeRef)?.coneType ?: return

        val call = declaration.body?.statements?.singleOrNull()?.unwrapReturn() as? FirFunctionCall ?: return
        val callee = call.calleeReference.toResolvedCallableSymbol() as? FirNamedFunctionSymbol ?: return
        if (callee.name.asString() != "invoke") return

        val target = (call.explicitReceiver ?: call.dispatchReceiver) as? FirPropertyAccessExpression ?: return
        if (target.explicitReceiver != null) return
        val isStableRead = when (val symbol = target.calleeReference.toResolvedCallableSymbol()) {
            is FirValueParameterSymbol -> true
            is FirPropertySymbol -> symbol.isVal
            else -> false
        }
        if (!isStableRead) return

        // An unequal type means the lambda adapts rather than forwards — a `@Composable () -> Unit`
        // reaching a `@Composable RowScope.() -> Unit` parameter cannot be passed on its own.
        if (!AbstractTypeChecker.equalTypes(context.session.typeContext, target.resolvedType, lambdaType)) return

        val arguments = call.argumentList.arguments
        if (arguments.size != declaration.valueParameters.size) return
        val forwardsAllParameters = declaration.valueParameters.withIndex().all { (index, parameter) ->
            val access = arguments[index] as? FirPropertyAccessExpression ?: return@all false
            access.explicitReceiver == null &&
                access.calleeReference.toResolvedCallableSymbol() == parameter.symbol
        }
        if (!forwardsAllParameters) return

        reporter.reportOn(
            declaration.source,
            PassThroughLambdaErrors.LAMBDA_CAN_BE_PASSED_DIRECTLY,
            context,
        )
    }

    private fun FirStatement.unwrapReturn(): FirStatement =
        (this as? FirReturnExpression)?.result ?: this
}

object PassThroughLambdaErrors : KtDiagnosticsContainer() {
    val LAMBDA_CAN_BE_PASSED_DIRECTLY by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = PassThroughLambdaErrorMessages
}

object PassThroughLambdaErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("PassThroughLambda") { map ->
        map.put(
            PassThroughLambdaErrors.LAMBDA_CAN_BE_PASSED_DIRECTLY,
            "This lambda only invokes a function value of the same type. Pass that value directly " +
                "('content = content').",
        )
    }
}
