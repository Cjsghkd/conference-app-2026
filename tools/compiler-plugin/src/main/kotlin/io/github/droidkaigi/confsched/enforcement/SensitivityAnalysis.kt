package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.ClassId

/**
 * Decides whether a body transitively reaches a sensitive read. A callee counts when it (1) carries
 * [markerClassId] from source or from the metadata [SensitivityMetadataIrExtension] synthesizes for
 * cross-module callees, or (2) is an inherent read itself. Same-module intermediate callers are not
 * yet annotated, since IR runs after the checkers, so their bodies are walked here via a memoized
 * reachability search.
 */
internal class SensitivityAnalysis(
    private val markerClassId: ClassId,
    private val isInherentRead: (FirCallableSymbol<*>) -> Boolean,
) {
    context(context: CheckerContext)
    fun isReachedFrom(body: FirElement): Boolean =
        body.referencesSensitive(cache = HashMap(), visiting = HashSet())

    // Walks a body (already BODY_RESOLVE-resolved for same-module declarations) collecting every
    // resolved callee, delegating the sensitivity decision to isSensitive.
    context(context: CheckerContext)
    private fun FirElement.referencesSensitive(
        cache: MutableMap<FirCallableSymbol<*>, Boolean>,
        visiting: MutableSet<FirCallableSymbol<*>>,
    ): Boolean {
        var found = false
        accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                if (found) return
                if (element is FirQualifiedAccessExpression) {
                    val callee = element.toResolvedCallableSymbol()
                    if (callee != null && isSensitive(callee, cache, visiting)) {
                        found = true
                        return
                    }
                }
                element.acceptChildren(this)
            }
        })
        return found
    }

    // symbol.fir reaches another declaration's body; safe here because the checkers run after
    // BODY_RESOLVE, so same-module callees already have resolved bodies.
    @OptIn(org.jetbrains.kotlin.fir.symbols.SymbolInternals::class)
    context(context: CheckerContext)
    private fun isSensitive(
        symbol: FirCallableSymbol<*>,
        cache: MutableMap<FirCallableSymbol<*>, Boolean>,
        visiting: MutableSet<FirCallableSymbol<*>>,
    ): Boolean {
        cache[symbol]?.let { return it }
        if (isInherentRead(symbol)) {
            cache[symbol] = true
            return true
        }
        if (symbol is FirNamedFunctionSymbol && symbol.hasAnnotation(markerClassId, context.session)) {
            cache[symbol] = true
            return true
        }
        // A callee already on the current path forms a cycle; treat it as not-yet-sensitive here
        // without caching, so the enclosing frame still decides the callee's real value.
        if (symbol in visiting) return false
        // Cross-module callees deserialize without a body; their transitive marker, if any, was
        // already observed above through the metadata annotation.
        val body = (symbol.fir as? FirFunction)?.body ?: run {
            cache[symbol] = false
            return false
        }
        visiting.add(symbol)
        val result = body.referencesSensitive(cache, visiting)
        visiting.remove(symbol)
        cache[symbol] = result
        return result
    }
}
