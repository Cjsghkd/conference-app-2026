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
import org.jetbrains.kotlin.fir.declarations.utils.fromPrimaryConstructor
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.toResolvedPropertySymbol
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

        val exposed = declaration.exposedExpression() ?: return
        val mirrored = exposed.readOfOwnProperty() ?: return
        // A backing field is never reassigned, so only a `val` mirror can move into one.
        if (!mirrored.isVal) return
        if (mirrored.resolvedStatus.visibility != Visibilities.Private) return
        if (mirrored.getContainingClassSymbol() != containingClass) return
        // A constructor `val` has no initializer to move into a `field` clause.
        if (mirrored.fromPrimaryConstructor) return
        if (!mirrored.resolvedReturnType.isSubtypeOf(declaration.symbol.resolvedReturnType, context.session)) return

        reporter.reportOn(
            source,
            ExplicitBackingFieldErrors.PROPERTY_MUST_USE_EXPLICIT_BACKING_FIELD,
            mirrored.name.asString(),
            context,
        )
    }

    private fun FirProperty.exposedExpression(): FirExpression? {
        initializer?.let { return it }
        val body = getter?.body ?: return null
        return (body.statements.singleOrNull() as? FirReturnExpression)?.result
    }

    private fun FirExpression.readOfOwnProperty(): FirPropertySymbol? {
        val access = when (this) {
            is FirPropertyAccessExpression -> this

            // A converting call such as `mutableFoo.asStateFlow()` still exposes the receiver itself.
            is FirFunctionCall -> explicitReceiver as? FirPropertyAccessExpression

            else -> null
        } ?: return null
        val receiver = access.explicitReceiver
        if (receiver != null && receiver !is FirThisReceiverExpression) return null
        return access.calleeReference.toResolvedPropertySymbol()
    }
}

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
