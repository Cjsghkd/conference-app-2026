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
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.types.equalTypes

internal object PrivateSetRequiredChecker : FirPropertyChecker(MppCheckerKind.Platform) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        if (declaration.isLocal || !declaration.isVal) return
        if (declaration.visibility == Visibilities.Private) return
        val source = declaration.source ?: return
        val containingClass = declaration.symbol.getContainingClassSymbol() ?: return

        val mirrored = declaration.getterResult()?.propertyReadOnThis() ?: return
        if (mirrored.isVal) return
        if (!mirrored.isPrivateStoredMemberOf(containingClass)) return
        // A narrower private type is a job for an explicit backing field, not for `private set`.
        if (!mirrored.resolvedReturnType.equalTypes(declaration.symbol.resolvedReturnType, context.session)) return

        reporter.reportOn(
            source,
            PrivateSetErrors.PROPERTY_MUST_USE_PRIVATE_SET,
            mirrored.name.asString(),
            context,
        )
    }
}

object PrivateSetErrors : KtDiagnosticsContainer() {
    val PROPERTY_MUST_USE_PRIVATE_SET by error1<PsiElement, String>(
        SourceElementPositioningStrategies.DECLARATION_NAME,
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = PrivateSetErrorMessages
}

object PrivateSetErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("PrivateSet") { map ->
        map.put(
            PrivateSetErrors.PROPERTY_MUST_USE_PRIVATE_SET,
            "This property must be a `var` with a `private set` instead of a read-only mirror of " +
                "the private property ''{0}''. Move the initializer of ''{0}'' onto this " +
                "declaration, add `private set`, and delete ''{0}'' together with this getter.",
            CommonRenderers.STRING,
        )
    }
}
