package io.github.droidkaigi.confsched.enforcement

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.FirTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.renderReadable

internal object RememberSerializableNames {
    const val SAVEABLE_PACKAGE = "androidx.compose.runtime.saveable"
    const val REMEMBER_SERIALIZABLE = "rememberSerializable"
}

private fun FirFunctionSymbol<*>.takesSerializer(session: FirSession): Boolean =
    valueParameterSymbols.any {
        it.resolvedReturnType.fullyExpandedType(session).classId == SerializerLookupNames.K_SERIALIZER_ID
    }

internal object RememberSerializableChecker : FirFunctionCallChecker(MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val session = context.session
        val callee = expression.toResolvedCallableSymbol() as? FirFunctionSymbol<*> ?: return
        val callableId = callee.callableId
        if (callableId.packageName.asString() != RememberSerializableNames.SAVEABLE_PACKAGE) return
        if (callableId.callableName.asString() != RememberSerializableNames.REMEMBER_SERIALIZABLE) return
        // The overloads taking a KSerializer leave the lookup to the caller; only the reified ones
        // resolve the serializer themselves.
        if (callee.takesSerializer(session)) return

        val typeRef = (expression.typeArguments.firstOrNull() as? FirTypeProjectionWithVariance)?.typeRef
        if (typeRef == null) {
            reporter.reportOn(
                expression.source,
                RememberSerializableErrors.REMEMBER_SERIALIZABLE_TYPE_NOT_SERIALIZABLE,
                "<unresolved type argument>",
                context,
            )
            return
        }

        val missing = typeRef.coneType.typeMissingSerializer(session) ?: return
        reporter.reportOn(
            expression.source,
            RememberSerializableErrors.REMEMBER_SERIALIZABLE_TYPE_NOT_SERIALIZABLE,
            missing.renderReadable(),
            context,
        )
    }
}

object RememberSerializableErrors : KtDiagnosticsContainer() {
    val REMEMBER_SERIALIZABLE_TYPE_NOT_SERIALIZABLE by error1<PsiElement, String>(
        SourceElementPositioningStrategies.DEFAULT,
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = RememberSerializableErrorMessages
}

object RememberSerializableErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("RememberSerializable") { map ->
        map.put(
            RememberSerializableErrors.REMEMBER_SERIALIZABLE_TYPE_NOT_SERIALIZABLE,
            "rememberSerializable resolves its serializer through the reified serializer<T>() lookup, " +
                "which is not compile-checked: {0} has no serializer, so saving the state would fail " +
                "only at runtime. Annotate the type with @Serializable, or pass a serializer explicitly",
            CommonRenderers.STRING,
        )
    }
}
