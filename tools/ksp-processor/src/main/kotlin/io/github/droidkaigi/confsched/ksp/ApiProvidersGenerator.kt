package io.github.droidkaigi.confsched.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class ApiProvidersGenerator(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(PROVIDED_API_FQ_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .forEach(::generate)

        return emptyList()
    }

    private fun generate(api: KSClassDeclaration) {
        val apiType = api.toClassName()
        val packageName = apiType.packageName
        val simpleName = apiType.simpleName
        val providerType = ClassName(packageName, "${simpleName}Provider")
        val defaultProviderType = ClassName(packageName, "Default${simpleName}Provider")
        val providersName = "${simpleName}Providers"
        val createExtension = MemberName(packageName, "create$simpleName")

        val ktorfit = ClassName(KTORFIT_PACKAGE, "Ktorfit")
        val appScope = ClassName(METRO_PACKAGE, "AppScope")
        val provides = ClassName(METRO_PACKAGE, "Provides")
        val singleIn = ClassName(METRO_PACKAGE, "SingleIn")
        val inject = ClassName(METRO_PACKAGE, "Inject")
        val contributesBinding = ClassName(METRO_PACKAGE, "ContributesBinding")
        val contributesTo = ClassName(METRO_PACKAGE, "ContributesTo")

        val singleInAnnotation = AnnotationSpec.builder(singleIn)
            .addMember("%T::class", appScope)
            .build()

        val providerInterface = TypeSpec.interfaceBuilder(providerType)
            .addProperty(
                PropertySpec.builder("api", apiType)
                    .addModifiers(KModifier.ABSTRACT)
                    .build(),
            )
            .build()

        val defaultProvider = TypeSpec.classBuilder(defaultProviderType)
            .addAnnotation(inject)
            .addAnnotation(singleInAnnotation)
            .addAnnotation(
                AnnotationSpec.builder(contributesBinding)
                    .addMember("%T::class", appScope)
                    .build(),
            )
            .addSuperinterface(providerType)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("ktorfit", ktorfit)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("api", apiType, KModifier.OVERRIDE)
                    .initializer("ktorfit.%M()", createExtension)
                    .build(),
            )
            .build()

        val provideApi = FunSpec.builder("provide$simpleName")
            .addAnnotation(provides)
            .addAnnotation(singleInAnnotation)
            .addParameter("provider", providerType)
            .returns(apiType)
            .addStatement("return provider.api")
            .build()

        val providers = TypeSpec.interfaceBuilder(providersName)
            .addAnnotation(
                AnnotationSpec.builder(contributesTo)
                    .addMember("%T::class", appScope)
                    .build(),
            )
            .addFunction(provideApi)
            .build()

        FileSpec.builder(packageName, providersName)
            .addType(providerInterface)
            .addType(defaultProvider)
            .addType(providers)
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                aggregating = false,
                originatingKSFiles = listOfNotNull(api.containingFile),
            )
    }

    private companion object {
        const val PROVIDED_API_FQ_NAME = "io.github.droidkaigi.confsched.core.data.ProvidedApi"
        const val KTORFIT_PACKAGE = "de.jensklingenberg.ktorfit"
        const val METRO_PACKAGE = "dev.zacsweers.metro"
    }
}
