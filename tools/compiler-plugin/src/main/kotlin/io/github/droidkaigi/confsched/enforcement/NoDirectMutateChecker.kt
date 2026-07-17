package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirPropertyAccessExpressionChecker
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol

// Soil's `mutate` is a `val: suspend (S)->T` property. Forbidding the property ACCESS (not just the
// invoke on it) also rejects aliases like `val m = x.mutate; m(id)`.
internal object NoDirectMutateChecker : FirPropertyAccessExpressionChecker(MppCheckerKind.Platform) {

    private const val SOIL_PACKAGE = "soil.query.compose"
    private const val MUTATION_TYPE = "MutationObject"
    private const val FORBIDDEN_PROPERTY = "mutate"

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirPropertyAccessExpression) {
        val callableId = expression.calleeReference.toResolvedCallableSymbol()?.callableId ?: return

        if (callableId.callableName.asString() != FORBIDDEN_PROPERTY) return
        if (callableId.packageName.asString() != SOIL_PACKAGE) return
        if (callableId.className?.shortName()?.asString() != MUTATION_TYPE) return

        reporter.reportOn(expression.source, NoDirectMutateErrors.NO_DIRECT_MUTATE, context)
    }
}
