package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.isSubtypeOf

internal object ExplicitBackingFieldRequiredChecker : FirPropertyChecker(MppCheckerKind.Platform) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        if (declaration.isLocal || !declaration.isVal) return
        if (declaration.visibility == Visibilities.Private) return
        val source = declaration.source ?: return
        val containingClass = declaration.symbol.getContainingClassSymbol() ?: return

        val exposed = declaration.initializer ?: declaration.getterResult() ?: return
        val mirrored = exposed.mirroredProperty() ?: return
        // A backing field is never reassigned, so only a `val` mirror can move into one.
        if (!mirrored.isVal) return
        if (!mirrored.isPrivateStoredMemberOf(containingClass)) return
        if (!mirrored.resolvedReturnType.isSubtypeOf(declaration.symbol.resolvedReturnType, context.session)) return

        reporter.reportOn(
            source,
            ExplicitBackingFieldErrors.PROPERTY_MUST_USE_EXPLICIT_BACKING_FIELD,
            mirrored.name.asString(),
            context,
        )
    }

    private fun FirExpression.mirroredProperty(): FirPropertySymbol? = when (this) {
        // Only a read-only view of the same instance leaves the exposed property a mirror. A
        // derived flow (`map`, `combine`) is a different object and stays its own property.
        is FirFunctionCall -> explicitReceiver?.propertyReadOnThis()?.takeIf { isReadOnlyView() }

        else -> propertyReadOnThis()
    }

    private fun FirFunctionCall.isReadOnlyView(): Boolean {
        val callableId = calleeReference.toResolvedCallableSymbol()?.callableId ?: return false
        return callableId.packageName.asString() == COROUTINES_FLOW_PACKAGE &&
            callableId.callableName.asString() in READ_ONLY_VIEW_CONVERSIONS
    }
}

private const val COROUTINES_FLOW_PACKAGE = "kotlinx.coroutines.flow"
private val READ_ONLY_VIEW_CONVERSIONS = setOf("asStateFlow", "asSharedFlow")

object ExplicitBackingFieldErrors : KtDiagnosticsContainer() {
    val PROPERTY_MUST_USE_EXPLICIT_BACKING_FIELD by error1<PsiElement, String>(
        SourceElementPositioningStrategies.DECLARATION_NAME,
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = ExplicitBackingFieldErrorMessages
}

object ExplicitBackingFieldErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("ExplicitBackingField") { map ->
        map.put(
            ExplicitBackingFieldErrors.PROPERTY_MUST_USE_EXPLICIT_BACKING_FIELD,
            "This property must hold the private property ''{0}'' as its explicit backing field. " +
                "Move the initializer of ''{0}'' into a `field = …` clause on this declaration and " +
                "delete ''{0}''.",
            CommonRenderers.STRING,
        )
    }
}
