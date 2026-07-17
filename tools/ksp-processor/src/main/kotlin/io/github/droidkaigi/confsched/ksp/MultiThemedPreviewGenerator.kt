package io.github.droidkaigi.confsched.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.writeTo

class MultiThemedPreviewGenerator(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val functions = resolver
            .getSymbolsWithAnnotation(MULTI_THEMED_PREVIEW_FQ_NAME)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        functions.forEach { function ->
            val packageName = function.packageName.asString()
            val originalName = function.simpleName.asString()
            val generatedName = "${originalName}MultiThemed"

            val preview = ClassName(COMPOSE_PREVIEW_PACKAGE, "Preview")
            val previewParameter = ClassName(COMPOSE_PREVIEW_PACKAGE, "PreviewParameter")
            val composable = ClassName(COMPOSE_RUNTIME_PACKAGE, "Composable")
            val colorScheme = ClassName(CORE_MODEL_PACKAGE, "KaigiColorScheme")
            val schemeProvider = ClassName(CORE_PREVIEW_PACKAGE, "KaigiSchemeProvider")
            val themeWrapper = MemberName(CORE_PREVIEW_PACKAGE, "MultiThemedPreviewTheme")
            val originalCall = MemberName(packageName, originalName)

            val colorSchemeParam = ParameterSpec.builder("colorScheme", colorScheme)
                .addAnnotation(
                    AnnotationSpec.builder(previewParameter)
                        .addMember("provider = %T::class", schemeProvider)
                        .build(),
                )
                .build()

            val funSpec = FunSpec.builder(generatedName)
                .addAnnotation(preview)
                .addAnnotation(composable)
                .addParameter(colorSchemeParam)
                .beginControlFlow("%M(colorScheme)", themeWrapper)
                .addStatement("%M()", originalCall)
                .endControlFlow()
                .build()

            FileSpec.builder(packageName, generatedName)
                .indent("    ")
                .addFunction(funSpec)
                .build()
                .writeTo(
                    codeGenerator = codeGenerator,
                    aggregating = false,
                    originatingKSFiles = listOfNotNull(function.containingFile),
                )
        }

        return emptyList()
    }

    private companion object {
        const val MULTI_THEMED_PREVIEW_FQ_NAME =
            "io.github.droidkaigi.confsched.core.preview.MultiThemedPreview"
        // androidx.* is the current common Preview home; the org.jetbrains.compose.* one is deprecated.
        const val COMPOSE_PREVIEW_PACKAGE = "androidx.compose.ui.tooling.preview"
        const val COMPOSE_RUNTIME_PACKAGE = "androidx.compose.runtime"
        const val CORE_MODEL_PACKAGE = "io.github.droidkaigi.confsched.core.model"
        const val CORE_PREVIEW_PACKAGE = "io.github.droidkaigi.confsched.core.preview"
    }
}
