package io.github.droidkaigi.confsched.enforcement

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers

object PersistedKeyErrors : KtDiagnosticsContainer() {
    val PERSISTED_KEY_TYPE_NOT_SERIALIZABLE by error1<PsiElement, String>(
        SourceElementPositioningStrategies.DEFAULT,
    )

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = PersistedKeyErrorMessages
}

object PersistedKeyErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("PersistedKey") { map ->
        map.put(
            PersistedKeyErrors.PERSISTED_KEY_TYPE_NOT_SERIALIZABLE,
            "The type argument for a @MustBeSerializable type parameter must be serializable: {0} has " +
                "no serializer, and the reified serializer<T>() lookup is not compile-checked, so " +
                "persistence would fail only at runtime",
            CommonRenderers.STRING,
        )
    }
}
