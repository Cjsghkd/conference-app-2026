package io.github.droidkaigi.confsched.enforcement

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirQualifiedAccessExpressionChecker
import org.jetbrains.kotlin.fir.expressions.FirCheckedSafeCallSubject
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val COMPOSE_RUNTIME_PACKAGE = FqName("androidx.compose.runtime")
private val COMPOSE_SAVEABLE_PACKAGE = FqName("androidx.compose.runtime.saveable")
private val COMPOSE_RETAIN_PACKAGE = FqName("androidx.compose.runtime.retain")

// rememberCoroutineScope is deliberately absent: it produces a handle whose immediate use
// (rememberCoroutineScope().launch { … }) is the idiomatic form.
private val RETAINED_VALUE_PRODUCERS = setOf(
    CallableId(COMPOSE_RUNTIME_PACKAGE, Name.identifier("remember")),
    CallableId(COMPOSE_RUNTIME_PACKAGE, Name.identifier("rememberUpdatedState")),
    CallableId(COMPOSE_SAVEABLE_PACKAGE, Name.identifier("rememberSaveable")),
    CallableId(COMPOSE_SAVEABLE_PACKAGE, Name.identifier("rememberSerializable")),
    CallableId(COMPOSE_RETAIN_PACKAGE, Name.identifier("retain")),
)

internal object RememberResultMustBeBoundChecker : FirQualifiedAccessExpressionChecker(MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirQualifiedAccessExpression) {
        val receiver = expression.explicitReceiver?.unwrapSafeCallSubject() as? FirFunctionCall ?: return
        val callee = receiver.toResolvedCallableSymbol() as? FirFunctionSymbol<*> ?: return
        if (callee.callableId !in RETAINED_VALUE_PRODUCERS) return
        val source = receiver.source ?: return

        reporter.reportOn(source, RememberBindingErrors.REMEMBER_RESULT_MUST_BE_BOUND, context)
    }
}

// A safe call resolves its selector against a subject standing in for the receiver, so the
// expression written in source is reached through that subject.
private fun FirExpression.unwrapSafeCallSubject(): FirExpression =
    (this as? FirCheckedSafeCallSubject)?.originalReceiverRef?.value ?: this

object RememberBindingErrors : KtDiagnosticsContainer() {
    val REMEMBER_RESULT_MUST_BE_BOUND by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = RememberBindingErrorMessages
}

object RememberBindingErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("RememberBinding") { map ->
        map.put(
            RememberBindingErrors.REMEMBER_RESULT_MUST_BE_BOUND,
            "A remembered value must be bound to a local before it is used: a call to remember, " +
                "rememberSaveable, rememberSerializable, rememberUpdatedState or retain must not " +
                "be the explicit receiver of a call or a property access. Assign it to a val and " +
                "go through that val ('val invoker = remember { … }' then 'invoker.invoke(…)'). " +
                "Passing the result as an argument is unaffected.",
        )
    }
}
