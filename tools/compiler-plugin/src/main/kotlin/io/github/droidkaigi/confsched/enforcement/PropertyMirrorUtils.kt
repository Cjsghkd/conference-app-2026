package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.utils.fromPrimaryConstructor
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.references.toResolvedPropertySymbol
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

internal fun FirProperty.getterResult(): FirExpression? {
    val body = getter?.body ?: return null
    return (body.statements.singleOrNull() as? FirReturnExpression)?.result
}

internal fun FirExpression.propertyReadOnThis(): FirPropertySymbol? {
    val access = this as? FirPropertyAccessExpression ?: return null
    val receiver = access.explicitReceiver
    if (receiver != null && receiver !is FirThisReceiverExpression) return null
    return access.calleeReference.toResolvedPropertySymbol()
}

// A computed or constructor property is excluded: neither has an initializer of its own to fold
// into the exposed property.
internal fun FirPropertySymbol.isPrivateStoredMemberOf(owner: FirClassLikeSymbol<*>): Boolean =
    resolvedStatus.visibility == Visibilities.Private &&
        getContainingClassSymbol() == owner &&
        hasInitializer &&
        !fromPrimaryConstructor
