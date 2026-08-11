@file:OptIn(org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI::class)

package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.impl.IrAnnotationImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.ClassId

/**
 * Writes [markerClassId] into the metadata of every function that transitively performs an inherent
 * read, so downstream modules' FIR sees the marker on resolved symbols. [SensitivityAnalysis] is the
 * matching front-end half.
 */
abstract class SensitivityMetadataIrExtension(private val markerClassId: ClassId) : IrGenerationExtension {

    private val markerFqName = markerClassId.asSingleFqName()

    /** Whether [element] on its own makes the enclosing function sensitive. */
    protected abstract fun isInherentRead(element: IrElement): Boolean

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // Absent when core:common is not on the classpath (e.g. the plugin's own build); nothing to mark then.
        val annotationConstructor = pluginContext.finderForBuiltins()
            .findConstructors(markerClassId)
            .firstOrNull() ?: return

        val functions = mutableListOf<IrSimpleFunction>()
        moduleFragment.files.forEach { collectFunctions(it.declarations, functions) }

        // Roots: functions explicitly annotated, or that perform an inherent read directly.
        val sensitive = functions.filterTo(mutableSetOf()) {
            it.hasAnnotation(markerFqName) || it.body?.containsInherentRead() == true
        }
        // Fixed point over the module's own call graph; cross-module callees already carry the
        // marker in their deserialized annotations, so one pass over each body sees them directly.
        do {
            var changed = false
            for (function in functions) {
                if (function in sensitive) continue
                if (function.body?.callsSensitive(sensitive) != true) continue
                sensitive.add(function)
                changed = true
            }
        } while (changed)

        for (function in sensitive) {
            // Source-annotated functions reach metadata through normal binary-retention serialization.
            if (function.hasAnnotation(markerFqName)) continue
            if (function.isFakeOverride) continue
            val annotation = IrAnnotationImpl.fromSymbolOwner(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                annotationConstructor.owner.returnType,
                annotationConstructor,
            )
            pluginContext.metadataDeclarationRegistrar
                .addMetadataVisibleAnnotationsToElement(function, listOf(annotation))
        }
    }

    private fun collectFunctions(declarations: List<IrDeclaration>, out: MutableList<IrSimpleFunction>) {
        for (declaration in declarations) {
            when (declaration) {
                is IrSimpleFunction -> out += declaration
                is IrClass -> collectFunctions(declaration.declarations, out)
                else -> {}
            }
        }
    }

    private fun IrElement.containsInherentRead(): Boolean = containsElement { isInherentRead(it) }

    private fun IrElement.callsSensitive(sensitive: Set<IrSimpleFunction>): Boolean = containsElement { element ->
        if (element !is IrCall) return@containsElement false
        val callee = element.symbol.owner
        callee in sensitive || callee.hasAnnotation(markerFqName)
    }

    private fun IrElement.containsElement(predicate: (IrElement) -> Boolean): Boolean {
        var found = false
        acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (found) return
                if (predicate(element)) {
                    found = true
                    return
                }
                element.acceptChildrenVoid(this)
            }
        })
        return found
    }
}
