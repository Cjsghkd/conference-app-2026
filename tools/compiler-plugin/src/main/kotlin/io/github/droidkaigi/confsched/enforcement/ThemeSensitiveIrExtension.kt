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
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

// Writes @ThemeSensitive into the metadata of every function that calls a theme-sensitive
// declaration, so downstream modules' FIR sees the transitive marker on resolved symbols.
class ThemeSensitiveMetadataIrExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // Absent when core:common is not on the classpath (e.g. the plugin's own build); nothing to mark then.
        val annotationConstructor = pluginContext.finderForBuiltins()
            .findConstructors(ThemeSensitiveNames.THEME_SENSITIVE_CLASS_ID)
            .firstOrNull() ?: return

        val functions = mutableListOf<IrSimpleFunction>()
        moduleFragment.files.forEach { collectFunctions(it.declarations, functions) }

        // Roots: functions explicitly annotated, or that directly read a MaterialTheme member.
        val sensitive = functions.filterTo(mutableSetOf()) {
            it.hasAnnotation(ThemeSensitiveNames.THEME_SENSITIVE_FQN) ||
                it.body?.readsMaterialTheme() == true
        }
        // Fixed point over the module's own call graph; cross-module callees already carry the
        // marker in their deserialized annotations, so one pass over each body sees them directly.
        do {
            var changed = false
            for (function in functions) {
                if (function in sensitive) continue
                if (function.body?.callsThemeSensitive(sensitive) != true) continue
                sensitive.add(function)
                changed = true
            }
        } while (changed)

        for (function in sensitive) {
            // Source-annotated functions reach metadata through normal binary-retention serialization.
            if (function.hasAnnotation(ThemeSensitiveNames.THEME_SENSITIVE_FQN)) continue
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

    private fun IrElement.readsMaterialTheme(): Boolean {
        var found = false
        acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (found) return
                if (element is IrCall && element.symbol.owner.isMaterialThemeGetter()) {
                    found = true
                    return
                }
                element.acceptChildrenVoid(this)
            }
        })
        return found
    }

    private fun IrSimpleFunction.isMaterialThemeGetter(): Boolean {
        val property = correspondingPropertySymbol?.owner ?: return false
        if (property.name !in ThemeSensitiveNames.MATERIAL_THEME_GETTER_NAMES) return false
        return parentClassOrNull?.classId == ThemeSensitiveNames.MATERIAL_THEME_CLASS_ID
    }

    private fun IrElement.callsThemeSensitive(sensitive: Set<IrSimpleFunction>): Boolean {
        var found = false
        acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) {
                if (found) return
                if (element is IrCall) {
                    val callee = element.symbol.owner
                    if (callee in sensitive || callee.hasAnnotation(ThemeSensitiveNames.THEME_SENSITIVE_FQN)) {
                        found = true
                        return
                    }
                }
                element.acceptChildrenVoid(this)
            }
        })
        return found
    }
}
