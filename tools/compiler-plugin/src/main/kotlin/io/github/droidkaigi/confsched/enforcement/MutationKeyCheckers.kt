package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirResolvable
import org.jetbrains.kotlin.fir.expressions.resolvedArgumentMapping
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object MutationKeyNames {
    val MUTATION_KEY_ID = ClassId(FqName("soil.query"), Name.identifier("MutationKey"))
    val MUTATION_TAG_ID = ClassId(
        FqName("io.github.droidkaigi.confsched.core.model"),
        Name.identifier("MutationTag"),
    )

    const val BUILDER_PREFIX = "build"
    const val BUILDER_SUFFIX = "MutationKey"
    val ID_PARAMETER = Name.identifier("id")
}

internal object MutationKeyMustCarryTagChecker : FirClassChecker(MppCheckerKind.Platform) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val session = context.session
        val implementsMutationKey =
            lookupSuperTypes(declaration.symbol, lookupInterfaces = true, deep = true, useSiteSession = session)
                .any { it.classId == MutationKeyNames.MUTATION_KEY_ID }
        if (!implementsMutationKey) return

        val tagParameters = declaration.primaryConstructorIfAny(session)
            ?.valueParameterSymbols
            .orEmpty()
            .filter { it.resolvedReturnTypeRef.coneType.classId == MutationKeyNames.MUTATION_TAG_ID }
        if (tagParameters.isEmpty()) {
            reporter.reportOn(declaration.source, MutationKeyErrors.MUTATION_KEY_WITHOUT_TAG, context)
            return
        }

        val idArguments = mutableListOf<FirExpression>()
        declaration.accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                element.acceptChildren(this)
            }

            override fun visitFunctionCall(functionCall: FirFunctionCall) {
                val calleeName = functionCall.toResolvedCallableSymbol()?.name?.asString().orEmpty()
                if (calleeName.startsWith(MutationKeyNames.BUILDER_PREFIX) &&
                    calleeName.endsWith(MutationKeyNames.BUILDER_SUFFIX)
                ) {
                    functionCall.resolvedArgumentMapping?.forEach { (argument, parameter) ->
                        if (parameter.name == MutationKeyNames.ID_PARAMETER) idArguments += argument
                    }
                }
                functionCall.acceptChildren(this)
            }
        })

        val tagFlowsIntoId = idArguments.any { it.referencesMutationTag() }
        if (!tagFlowsIntoId) {
            reporter.reportOn(
                tagParameters.first().source ?: declaration.source,
                MutationKeyErrors.MUTATION_TAG_NOT_IN_ID,
                context,
            )
        }
    }

    private fun FirExpression.referencesMutationTag(): Boolean {
        var found = false
        accept(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                if (found) return
                if (element is FirResolvable) {
                    val symbol = element.toResolvedCallableSymbol()
                    val isTagReference = symbol != null &&
                        symbol.resolvedReturnTypeRef.coneType.classId == MutationKeyNames.MUTATION_TAG_ID
                    if (isTagReference) {
                        found = true
                        return
                    }
                }
                element.acceptChildren(this)
            }
        })
        return found
    }
}
