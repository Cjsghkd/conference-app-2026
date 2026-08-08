package io.github.droidkaigi.confsched.enforcement

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.references.toResolvedPropertySymbol
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val STATE_CLASS_ID = ClassId(FqName("androidx.compose.runtime"), Name.identifier("State"))
private val VALUE_NAME = Name.identifier("value")

internal object StateMustBeDelegatedChecker : FirPropertyChecker(MppCheckerKind.Platform) {

    @OptIn(SymbolInternals::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirProperty) {
        val session = context.session
        if (!declaration.holdsDelegatableState(session)) return
        val source = declaration.source ?: return
        // A local variable and a private property are both confined to the file that declares
        // them, so the file holds every reference the rule has to account for.
        val file = context.containingFileSymbol?.fir ?: return

        val uses = StateUses(declaration.symbol, session)
        file.accept(uses)
        if (uses.reads.isEmpty() || !uses.valueReceivers.containsAll(uses.reads)) return

        reporter.reportOn(source, StateDelegationErrors.STATE_MUST_BE_DELEGATED, context)
    }
}

// `by` needs a delegate expression on the declaration and does not recompute per read, so a
// property the declaration does not initialize and a property with a custom getter stay out.
private fun FirProperty.holdsDelegatableState(session: FirSession): Boolean {
    if (isVar || delegate != null) return false
    if (initializer == null || getter?.body != null) return false
    if (!isLocal && visibility != Visibilities.Private) return false
    return symbol.resolvedReturnType.toRegularClassSymbol(session)?.isComposeState(session) == true
}

private class StateUses(
    private val target: FirPropertySymbol,
    private val session: FirSession,
) : FirVisitorVoid() {
    // FIR elements carry no structural equality, so these sets hold occurrences: one read of the
    // target stays distinct from another.
    val reads = mutableSetOf<FirElement>()
    val valueReceivers = mutableSetOf<FirElement>()

    override fun visitElement(element: FirElement) {
        if (element is FirQualifiedAccessExpression) {
            if (element.calleeReference.toResolvedPropertySymbol() === target) {
                reads += element
            }
            if (element is FirPropertyAccessExpression && element.readsStateValue(session)) {
                element.explicitReceiver?.let { valueReceivers += it }
            }
        }
        element.acceptChildren(this)
    }
}

private fun FirPropertyAccessExpression.readsStateValue(session: FirSession): Boolean {
    val symbol = calleeReference.toResolvedPropertySymbol() ?: return false
    if (symbol.name != VALUE_NAME) return false
    val owner = symbol.getContainingClassSymbol() as? FirRegularClassSymbol ?: return false
    return owner.isComposeState(session)
}

private fun FirRegularClassSymbol.isComposeState(session: FirSession): Boolean =
    classId == STATE_CLASS_ID ||
        lookupSuperTypes(this, lookupInterfaces = true, deep = true, useSiteSession = session)
            .any { it.classId == STATE_CLASS_ID }

object StateDelegationErrors : KtDiagnosticsContainer() {
    val STATE_MUST_BE_DELEGATED by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_NAME)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = StateDelegationErrorMessages
}

object StateDelegationErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("StateDelegation") { map ->
        map.put(
            StateDelegationErrors.STATE_MUST_BE_DELEGATED,
            "A state read only through .value must be delegated: declare it with 'by' and drop " +
                ".value at every use, importing androidx.compose.runtime.getValue, and setValue " +
                "as well where the state is written. A state whose object is passed, returned or " +
                "destructured cannot be delegated and is unaffected.",
        )
    }
}
