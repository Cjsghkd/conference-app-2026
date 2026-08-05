package io.github.droidkaigi.confsched.enforcement

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory

object RoleContextErrors : KtDiagnosticsContainer() {
    val PRESENTER_DECLARES_SCREEN_CONTEXT by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    val SCREEN_CONTEXT_IS_PRESENTER_CONTEXT by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    val PRESENTER_EFFECT_IN_SCREEN_ROOT by error0<PsiElement>(SourceElementPositioningStrategies.DEFAULT)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = RoleContextErrorMessages
}

object RoleContextErrorMessages : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("RoleContext") { map ->
        map.put(
            RoleContextErrors.PRESENTER_DECLARES_SCREEN_CONTEXT,
            "A presenter must take ONLY a PresenterContext. Declaring a ScreenContext-derived type " +
                "as a context parameter would let the presenter consume Root-role dependencies. " +
                "Pass the held PresenterContext instead.",
        )
        map.put(
            RoleContextErrors.SCREEN_CONTEXT_IS_PRESENTER_CONTEXT,
            "A type must not implement BOTH ScreenContext and PresenterContext (is-a leak). " +
                "Use composition: hold a PresenterContext as a property " +
                "(e.g. `class FooScreenContext(val presenterContext: FooPresenterContext) : ScreenContext`).",
        )
        map.put(
            RoleContextErrors.PRESENTER_EFFECT_IN_SCREEN_ROOT,
            "A screen root must not perform presenter-role effects. This call requires a " +
                "PresenterContext context-parameter (e.g. ActionEffect / ScreenChannel.emit), which " +
                "is presenter-only. The Root may narrow-open a PresenterContext scope ONLY to invoke " +
                "the `*Presenter` function; move this effect into the presenter.",
        )
    }
}
