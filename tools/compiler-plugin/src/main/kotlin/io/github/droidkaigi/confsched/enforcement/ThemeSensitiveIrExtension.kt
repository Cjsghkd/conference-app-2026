@file:OptIn(org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI::class)

package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.parentClassOrNull

class ThemeSensitiveMetadataIrExtension : SensitivityMetadataIrExtension(ThemeSensitiveNames.THEME_SENSITIVE_CLASS_ID) {

    override fun isInherentRead(element: IrElement): Boolean =
        element is IrCall && element.symbol.owner.isMaterialThemeGetter()

    private fun IrSimpleFunction.isMaterialThemeGetter(): Boolean {
        val property = correspondingPropertySymbol?.owner ?: return false
        if (property.name !in ThemeSensitiveNames.MATERIAL_THEME_GETTER_NAMES) return false
        return parentClassOrNull?.classId == ThemeSensitiveNames.MATERIAL_THEME_CLASS_ID
    }
}
