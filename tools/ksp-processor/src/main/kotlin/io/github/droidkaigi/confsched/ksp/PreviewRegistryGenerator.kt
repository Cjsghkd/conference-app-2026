package io.github.droidkaigi.confsched.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
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
private const val PREVIEW_WRAPPER = "androidx.compose.ui.tooling.preview.PreviewWrapper"
private const val PREVIEW_PARAMETER = "androidx.compose.ui.tooling.preview.PreviewParameter"
private const val CORE_COMMON_PACKAGE = "io.github.droidkaigi.confsched.core.common"
private val PREVIEW_ANNOTATIONS = setOf(ANDROIDX_PREVIEW, JETBRAINS_PREVIEW)

/**
 * Generates a per-module `PreviewRegistry` object enumerating the module's previews as composable
 * lambdas. Non-JVM screenshot tests (iOS) cannot discover previews by classpath scanning, so the
 * registry provides the compile-time equivalent: it applies each preview's `@PreviewWrapper` and
 * expands its `@PreviewParameter` across the provider's values, matching what the tooling and the
 * JVM scanner do, so the screenshot ids agree across platforms.
 */
class PreviewRegistryGenerator(
    private val codeGenerator: CodeGenerator,
    private val registryPackage: String?,
) : SymbolProcessor {
    private var emitted = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val packageName = registryPackage ?: return emptyList()
        if (emitted) return emptyList()

        // Not getSymbolsWithAnnotation: it does not follow meta-annotations, so a preview declared
        // through a multi-preview annotation would be missed.
        val previews = resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { function -> function.effectiveAnnotations().any { it.fqName() in PREVIEW_ANNOTATIONS } }
            .distinctBy { it.qualifiedName!!.asString() }
            .sortedBy { it.qualifiedName!!.asString() }
            .toList()
        if (previews.isEmpty()) return emptyList()
        emitted = true

        val registeredPreview = ClassName(CORE_COMMON_PACKAGE, "RegisteredPreview")

        val initializer = CodeBlock.builder().beginControlFlow("buildList")
        previews.forEach { preview ->
            val name = preview.qualifiedName!!.asString()
            val member = MemberName(preview.packageName.asString(), preview.simpleName.asString())
            val wrapper = preview.previewWrapper()
            val provider = preview.previewParameterProvider()
            fun render(argument: String): CodeBlock {
                val call = CodeBlock.of("%M($argument)", member)
                return if (wrapper == null) call else CodeBlock.of("%T().Wrap { %L }", wrapper, call)
            }
            when {
                provider != null -> {
                    initializer.add("%T().values.forEachIndexed { index, value ->\n", provider)
                    initializer.indent()
                    initializer.addStatement(
                        "add(%T(%S + \"_\" + index) { %L })",
                        registeredPreview,
                        name,
                        render("value"),
                    )
                    initializer.unindent()
                    initializer.add("}\n")
                }

                preview.parameters.isEmpty() -> initializer.addStatement(
                    "add(%T(%S) { %L })",
                    registeredPreview,
                    name,
                    render(""),
                )
            }
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
                    *previews.mapNotNull { it.containingFile }.toTypedArray(),
                ),
            )
        return emptyList()
    }

    private fun KSFunctionDeclaration.previewWrapper(): ClassName? =
        effectiveAnnotations().firstOrNull { it.fqName() == PREVIEW_WRAPPER }?.classArgument("wrapper")

    private fun KSFunctionDeclaration.previewParameterProvider(): ClassName? = parameters
        .singleOrNull()
        ?.annotations
        ?.firstOrNull { it.fqName() == PREVIEW_PARAMETER }
        ?.classArgument("provider")

    private fun KSAnnotation.classArgument(name: String): ClassName? {
        val type = arguments.firstOrNull { it.name?.asString() == name }?.value as? KSType ?: return null
        val declaration = type.declaration
        return ClassName(declaration.packageName.asString(), declaration.simpleName.asString())
    }

    // The declaration's own annotations plus those of each annotation's class, so a meta-annotation
    // carrying `@Preview` / `@PreviewWrapper` counts as if the declaration carried them directly.
    private fun KSAnnotated.effectiveAnnotations(): Sequence<KSAnnotation> =
        annotations.flatMap { annotation ->
            sequenceOf(annotation) + annotation.annotationType.resolve().declaration.annotations
        }

    private fun KSAnnotation.fqName(): String? =
        annotationType.resolve().declaration.qualifiedName?.asString()
}

class PreviewRegistrySymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        PreviewRegistryGenerator(
            codeGenerator = environment.codeGenerator,
            registryPackage = environment.options[REGISTRY_PACKAGE_OPTION],
        )
}
