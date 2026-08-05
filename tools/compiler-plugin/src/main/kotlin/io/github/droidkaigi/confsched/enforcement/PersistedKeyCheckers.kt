package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.FirTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object PersistedKeyNames {
    val MUST_BE_SERIALIZABLE_ID = ClassId(
        FqName("io.github.droidkaigi.confsched.core.common"),
        Name.identifier("MustBeSerializable"),
    )
}

// Driven by the @MustBeSerializable annotation on a type parameter, not by a hard-coded callable
// and argument index, so signature changes cannot silently detach the check.
internal object MustBeSerializableChecker : FirFunctionCallChecker(MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callee = expression.toResolvedCallableSymbol() as? FirFunctionSymbol<*> ?: return
        val session = context.session

        callee.typeParameterSymbols.forEachIndexed { index, typeParameter ->
            if (!typeParameter.hasAnnotation(PersistedKeyNames.MUST_BE_SERIALIZABLE_ID, session)) return@forEachIndexed

            val typeRef = (expression.typeArguments.getOrNull(index) as? FirTypeProjectionWithVariance)?.typeRef
            if (typeRef == null) {
                reporter.reportOn(
                    expression.source,
                    PersistedKeyErrors.PERSISTED_KEY_TYPE_NOT_SERIALIZABLE,
                    "<unresolved type argument>",
                    context,
                )
                return@forEachIndexed
            }

            val missing = typeRef.coneType.typeMissingSerializer(session)
            if (missing != null) {
                reporter.reportOn(
                    expression.source,
                    PersistedKeyErrors.PERSISTED_KEY_TYPE_NOT_SERIALIZABLE,
                    missing.renderReadable(),
                    context,
                )
            }
        }
    }
}
