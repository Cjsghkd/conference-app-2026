package io.github.droidkaigi.confsched.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class NavKeySerializersProviderGenerator(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val navKeyType = resolver
            .getClassDeclarationByName(resolver.getKSNameFromString(NAV_KEY_FQ_NAME))
            ?.asStarProjectedType()
            ?: return emptyList()

        val navKeys = resolver
            .getSymbolsWithAnnotation(SERIALIZABLE_FQ_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .filter { navKeyType.isAssignableFrom(it.asStarProjectedType()) }
            .toList()

        if (navKeys.isEmpty()) return emptyList()

        val packageName = commonPackagePrefix(navKeys.map { it.packageName.asString() })
        val className = packageName.substringAfterLast('.')
            .replaceFirstChar { it.uppercaseChar() } + "NavKeySerializersProvider"

        val navKeyClass = ClassName.bestGuess(NAV_KEY_FQ_NAME)
        val serializersModule = ClassName(SERIALIZATION_MODULES_PACKAGE, "SerializersModule")
        val serializersModuleFn = MemberName(SERIALIZATION_MODULES_PACKAGE, "SerializersModule")
        val polymorphic = MemberName(SERIALIZATION_MODULES_PACKAGE, "polymorphic")
        val subclass = MemberName(SERIALIZATION_MODULES_PACKAGE, "subclass")

        val body = CodeBlock.builder()
            .beginControlFlow("return %M", serializersModuleFn)
            .beginControlFlow("%M(%T::class)", polymorphic, navKeyClass)
            .apply {
                navKeys.forEach { navKey ->
                    val type = navKey.toClassName()
                    addStatement("%M(%T::class, %T.serializer())", subclass, type, type)
                }
            }
            .endControlFlow()
            .endControlFlow()
            .build()

        val providerType = TypeSpec.classBuilder(className)
            .addAnnotation(
                AnnotationSpec.builder(ClassName(METRO_PACKAGE, "ContributesIntoSet"))
                    .addMember("%T::class", ClassName(METRO_PACKAGE, "AppScope"))
                    .build(),
            )
            .addAnnotation(ClassName(METRO_PACKAGE, "Inject"))
            .addSuperinterface(ClassName(CORE_COMMON_PACKAGE, "NavKeySerializersProvider"))
            .addProperty(
                PropertySpec.builder("serializersModule", serializersModule)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(FunSpec.getterBuilder().addCode(body).build())
                    .build(),
            )
            .build()

        FileSpec.builder(packageName, className)
            .addType(providerType)
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
                originatingKSFiles = navKeys.mapNotNull { it.containingFile }.distinct(),
            )

        return emptyList()
    }

    private fun commonPackagePrefix(packages: List<String>): String {
        val segmentLists = packages.map { it.split('.') }
        val first = segmentLists.first()
        var prefixLength = first.size
        for (segments in segmentLists) {
            var index = 0
            while (index < prefixLength && index < segments.size && segments[index] == first[index]) index++
            prefixLength = index
        }
        return first.take(prefixLength).joinToString(".")
    }

    private companion object {
        const val NAV_KEY_FQ_NAME = "androidx.navigation3.runtime.NavKey"
        const val SERIALIZABLE_FQ_NAME = "kotlinx.serialization.Serializable"
        const val SERIALIZATION_MODULES_PACKAGE = "kotlinx.serialization.modules"
        const val METRO_PACKAGE = "dev.zacsweers.metro"
        const val CORE_COMMON_PACKAGE = "io.github.droidkaigi.confsched.core.common"
    }
}
