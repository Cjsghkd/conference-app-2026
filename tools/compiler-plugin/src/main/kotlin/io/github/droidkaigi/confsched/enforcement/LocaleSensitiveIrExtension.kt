@file:OptIn(org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI::class)

package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.classId

class LocaleSensitiveMetadataIrExtension : SensitivityMetadataIrExtension(LocaleSensitiveNames.LOCALE_SENSITIVE_CLASS_ID) {

    // Matching on the expression type rather than on the call covers both a generated `Res.string.…`
    // accessor and a resource handed to a component as a parameter.
    override fun isInherentRead(element: IrElement): Boolean {
        if (element !is IrExpression) return false
        val classId = element.type.classOrNull?.owner?.classId ?: return false
        return classId in LocaleSensitiveNames.LOCALIZED_RESOURCE_CLASS_IDS
    }
}
