package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object SerializerLookupNames {
    val SERIALIZABLE_ID = ClassId(
        FqName("kotlinx.serialization"),
        Name.identifier("Serializable"),
    )

    val K_SERIALIZER_ID = ClassId(
        FqName("kotlinx.serialization"),
        Name.identifier("KSerializer"),
    )

    // kotlinx.serialization.builtins ships a serializer for each of these, so they resolve without
    // carrying @Serializable.
    val BUILT_IN_SERIALIZABLE = setOf(
        "kotlin.Boolean",
        "kotlin.Byte",
        "kotlin.Short",
        "kotlin.Int",
        "kotlin.Long",
        "kotlin.Float",
        "kotlin.Double",
        "kotlin.Char",
        "kotlin.String",
        "kotlin.Unit",
        "kotlin.Nothing",
        "kotlin.UByte",
        "kotlin.UShort",
        "kotlin.UInt",
        "kotlin.ULong",
        "kotlin.Pair",
        "kotlin.Triple",
        "kotlin.Array",
        "kotlin.BooleanArray",
        "kotlin.ByteArray",
        "kotlin.CharArray",
        "kotlin.ShortArray",
        "kotlin.IntArray",
        "kotlin.LongArray",
        "kotlin.FloatArray",
        "kotlin.DoubleArray",
        "kotlin.UByteArray",
        "kotlin.UShortArray",
        "kotlin.UIntArray",
        "kotlin.ULongArray",
        "kotlin.collections.List",
        "kotlin.collections.MutableList",
        "kotlin.collections.Set",
        "kotlin.collections.MutableSet",
        "kotlin.collections.Map",
        "kotlin.collections.MutableMap",
        "kotlin.collections.Map.Entry",
        "kotlin.time.Duration",
        "kotlin.uuid.Uuid",
    )
}

internal fun ConeKotlinType.hasKotlinxSerializer(session: FirSession): Boolean {
    val expanded = fullyExpandedType(session)
    val classId = expanded.classId ?: return false
    // A star projection resolves to no type at all, so `!= true` rejects it along with an
    // argument that has no serializer.
    if (expanded.typeArguments.any { it.type?.hasKotlinxSerializer(session) != true }) return false
    if (classId.asFqNameString() in SerializerLookupNames.BUILT_IN_SERIALIZABLE) return true

    val classSymbol = expanded.toRegularClassSymbol(session) ?: return false
    return classSymbol.classKind == ClassKind.ENUM_CLASS ||
        classSymbol.hasAnnotation(SerializerLookupNames.SERIALIZABLE_ID, session)
}
