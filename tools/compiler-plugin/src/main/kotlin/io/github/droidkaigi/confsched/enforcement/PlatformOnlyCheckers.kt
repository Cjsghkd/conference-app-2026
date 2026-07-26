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
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val PLATFORM_ONLY_CLASS_ID = ClassId(
    FqName("io.github.droidkaigi.confsched.core.common"),
    Name.identifier("PlatformOnly"),
)
private val PLATFORM_PREFIXES = listOf("Android", "Ios", "Desktop", "Web")

// A common declaration that only has an effect on one platform must say so in its name, and a
// platform-prefixed name must be backed by @PlatformOnly so the prefix cannot lie or go stale.
internal object PlatformOnlyNamingChecker : FirBasicDeclarationChecker(MppCheckerKind.Platform) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirDeclaration) {
        val name = when (declaration) {
            is FirNamedFunction -> declaration.name.asString()
            is FirRegularClass -> declaration.name.asString()
            is FirProperty -> declaration.name.asString()
            else -> return
        }

        val annotation = declaration.getAnnotationByClassId(PLATFORM_ONLY_CLASS_ID, context.session)
        val declaredPlatform = annotation?.argumentMapping?.mapping?.get(Name.identifier("platform"))
            ?.let { it as? FirPropertyAccessExpression }
            ?.calleeReference?.toResolvedCallableSymbol()?.name?.asString()

        if (annotation != null) {
            if (declaredPlatform != null && !name.startsWith(declaredPlatform)) {
                reporter.reportOn(
                    declaration.source,
                    PlatformOnlyErrors.PLATFORM_ONLY_NAME_MISMATCH,
                    declaredPlatform,
                    context,
                )
            }
            return
        }

        // Reverse direction only applies to common code: platform source sets legitimately use
        // platform-prefixed names (IosAppGraph and the like) without the annotation.
        val filePath = context.containingFileSymbol?.sourceFile?.path ?: return
        if (!filePath.contains("/commonMain/")) return
        if (context.containingDeclarations.any { it is org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol<*> }) return
        val prefix = PLATFORM_PREFIXES.firstOrNull { name.startsWith(it) && name.length > it.length } ?: return
        reporter.reportOn(
            declaration.source,
            PlatformOnlyErrors.PLATFORM_PREFIX_REQUIRES_ANNOTATION,
            prefix,
            context,
        )
    }
}

object PlatformOnlyErrors : KtDiagnosticsContainer() {
    val PLATFORM_ONLY_NAME_MISMATCH by error1<PsiElement, String>(SourceElementPositioningStrategies.DEFAULT)
    val PLATFORM_PREFIX_REQUIRES_ANNOTATION by error1<PsiElement, String>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = PlatformOnlyErrorMessages
}

object PlatformOnlyErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("PlatformOnly") { map ->
        map.put(
            PlatformOnlyErrors.PLATFORM_ONLY_NAME_MISMATCH,
            "This declaration is @PlatformOnly({0}) but its name does not start with ''{0}''. " +
                "Platform-confined behavior must be visible in the name.",
            CommonRenderers.STRING,
        )
        map.put(
            PlatformOnlyErrors.PLATFORM_PREFIX_REQUIRES_ANNOTATION,
            "The name starts with the platform prefix ''{0}'' but the declaration is not " +
                "annotated @PlatformOnly. Annotate it (or rename it) so the prefix cannot lie.",
            CommonRenderers.STRING,
        )
    }
}
