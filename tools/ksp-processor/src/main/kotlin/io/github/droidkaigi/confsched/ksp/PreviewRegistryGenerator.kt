package io.github.droidkaigi.confsched.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

private const val REGISTRY_PACKAGE_OPTION = "droidkaigi.previewRegistryPackage"
private const val JETBRAINS_PREVIEW = "org.jetbrains.compose.ui.tooling.preview.Preview"
private const val ANDROIDX_PREVIEW = "androidx.compose.ui.tooling.preview.Preview"
private const val CORE_COMMON_PACKAGE = "io.github.droidkaigi.confsched.core.common"
private const val CORE_PREVIEW_PACKAGE = "io.github.droidkaigi.confsched.core.preview"
private const val MULTI_THEMED_PREVIEW = "io.github.droidkaigi.confsched.core.preview.MultiThemedPreview"

/**
 * Generates a per-module `PreviewRegistry` object enumerating the module's previews as
 * composable lambdas. Non-JVM screenshot tests (iOS) cannot discover previews by classpath
 * scanning, so the registry provides the compile-time equivalent. Zero-parameter `@Preview`
 * functions are registered directly; `@MultiThemedPreview` functions are expanded across
 * `KaigiSchemeProvider`, mirroring the `@PreviewParameter` expansion the scanner performs.
 */
class PreviewRegistryGenerator(
    private val codeGenerator: CodeGenerator,
    private val registryPackage: String?,
) : SymbolProcessor {
    private var emitted = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val packageName = registryPackage ?: return emptyList()
        if (emitted) return emptyList()

        val plainPreviews = (
            resolver.getSymbolsWithAnnotation(ANDROIDX_PREVIEW) +
                resolver.getSymbolsWithAnnotation(JETBRAINS_PREVIEW)
            )
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.parameters.isEmpty() }
            .distinctBy { it.qualifiedName!!.asString() }
            .sortedBy { it.qualifiedName!!.asString() }
            .toList()
        val multiThemedPreviews = resolver.getSymbolsWithAnnotation(MULTI_THEMED_PREVIEW)
            .filterIsInstance<KSFunctionDeclaration>()
            .sortedBy { it.qualifiedName!!.asString() }
            .toList()
        if (plainPreviews.isEmpty() && multiThemedPreviews.isEmpty()) return emptyList()
        emitted = true

        val registeredPreview = ClassName(CORE_COMMON_PACKAGE, "RegisteredPreview")
        val schemeProvider = ClassName(CORE_PREVIEW_PACKAGE, "KaigiSchemeProvider")

        val initializer = CodeBlock.builder().beginControlFlow("buildList")
        plainPreviews.forEach { preview ->
            val member = MemberName(preview.packageName.asString(), preview.simpleName.asString())
            initializer.addStatement(
                "add(%T(%S) { %M() })",
                registeredPreview,
                preview.qualifiedName!!.asString(),
                member,
            )
        }
        multiThemedPreviews.forEach { preview ->
            // Matches the name of the KSP-generated @PreviewParameter preview and the screenshot
            // id the JVM scanner derives from it, so goldens are comparable across platforms.
            val generatedName = "${preview.simpleName.asString()}MultiThemed"
            val member = MemberName(preview.packageName.asString(), generatedName)
            initializer.add("%T().values.forEachIndexed { index, scheme ->\n", schemeProvider)
            initializer.indent()
            initializer.addStatement(
                "add(%T(%S + \"_\" + index) { %M(scheme) })",
                registeredPreview,
                "${preview.packageName.asString()}.$generatedName",
                member,
            )
            initializer.unindent()
            initializer.add("}\n")
        }
        initializer.endControlFlow()

        val registry = TypeSpec.objectBuilder("PreviewRegistry")
            .addProperty(
                PropertySpec.builder(
                    "previews",
                    LIST.parameterizedBy(registeredPreview),
                ).initializer(initializer.build()).build(),
            )
            .build()

        FileSpec.builder(packageName, "PreviewRegistry")
            .addType(registry)
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                dependencies = Dependencies(
                    aggregating = true,
                    *(plainPreviews + multiThemedPreviews).mapNotNull { it.containingFile }.toTypedArray(),
                ),
            )
        return emptyList()
    }
}

class PreviewRegistrySymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        PreviewRegistryGenerator(
            codeGenerator = environment.codeGenerator,
            registryPackage = environment.options[REGISTRY_PACKAGE_OPTION],
        )
}
