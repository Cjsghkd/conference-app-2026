package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isUnit

private val SCREEN_LEVEL_SUFFIXES = listOf("Screen", "ScreenRoot")

internal object ScreenIsSoleComponentInFileChecker : FirFileChecker(MppCheckerKind.Platform) {

    @OptIn(DirectDeclarationsAccess::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFile) {
        val session = context.session
        val components = declaration.declarations
            .filterIsInstance<FirNamedFunction>()
            .filter { it.symbol.hasAnnotation(PreviewWrapperNames.COMPOSABLE_ID, session) }
            .filter { it.symbol.resolvedReturnTypeRef.coneType.isUnit }
            .filterNot { it.symbol.previewAnnotations(session).isPreview }
        if (components.size < 2) return

        val screens = components.filter { component ->
            SCREEN_LEVEL_SUFFIXES.any { component.name.asString().endsWith(it) }
        }
        if (screens.isEmpty()) return

        val fileBaseName = declaration.name.substringBeforeLast('.')
        val anchor = screens.firstOrNull { it.name.asString() == fileBaseName } ?: screens.first()

        for (component in components) {
            if (component === anchor) continue
            val source = component.source ?: continue
            reporter.reportOn(
                source,
                ScreenFileErrors.SCREEN_FILE_DECLARES_EXTRA_COMPONENT,
                anchor.name.asString(),
                context,
            )
        }
    }
}

object ScreenFileErrors : KtDiagnosticsContainer() {
    val SCREEN_FILE_DECLARES_EXTRA_COMPONENT by error1<PsiElement, String>(
        SourceElementPositioningStrategies.DECLARATION_NAME,
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = ScreenFileErrorMessages
}

object ScreenFileErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("ScreenFile") { map ->
        map.put(
            ScreenFileErrors.SCREEN_FILE_DECLARES_EXTRA_COMPONENT,
            "A file declaring the screen-level @Composable ''{0}'' must not declare another UI " +
                "component. Move this component to its own file named after it. @Preview " +
                "functions are exempt.",
            CommonRenderers.STRING,
        )
    }
}
