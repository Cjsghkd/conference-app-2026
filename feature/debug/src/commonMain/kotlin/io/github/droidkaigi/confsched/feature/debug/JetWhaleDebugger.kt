package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.kitakkun.jetwhale.agent.runtime.startJetWhale
import com.kitakkun.jetwhale.plugins.nav3.agent.JetWhaleNav3AgentPlugin
import com.kitakkun.jetwhale.plugins.nav3.agent.Nav3KeyCodec
import com.kitakkun.jetwhale.plugins.nav3.agent.TrackNavBackStack
import com.kitakkun.jetwhale.plugins.network.agent.JetWhaleNetworkAgentPlugin
import com.kitakkun.jetwhale.plugins.network.agent.ktor.ktorSendInterceptor
import com.kitakkun.jetwhale.plugins.semantics.agent.JetWhaleSemanticsAgentPlugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.droidkaigi.confsched.core.common.BackStackDebuggingEffect
import io.github.droidkaigi.confsched.core.common.MergedNavKeySerializersProvider
import io.github.droidkaigi.confsched.core.common.NoopBackStackDebuggingEffect
import io.github.droidkaigi.confsched.core.common.NoopSemanticsDebuggingEffect
import io.github.droidkaigi.confsched.core.common.SemanticsDebuggingEffect
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin

// The JetWhale host's default port. Android and the iOS simulator reach it on localhost; the host
// sets up the `adb reverse` mapping for Android itself.
private const val JETWHALE_HOST = "localhost"
private const val JETWHALE_PORT = 5080

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding<BackStackDebuggingEffect>(), replaces = [NoopBackStackDebuggingEffect::class])
@ContributesBinding(AppScope::class, binding<SemanticsDebuggingEffect>(), replaces = [NoopSemanticsDebuggingEffect::class])
class JetWhaleDebugger(
    httpClient: HttpClient,
    navKeySerializersProvider: MergedNavKeySerializersProvider,
) : BackStackDebuggingEffect,
    SemanticsDebuggingEffect {
    private val networkPlugin = JetWhaleNetworkAgentPlugin()

    // The app registers its NavKeys open-polymorphically, which is also how the host decodes the
    // keys it pushes — see docs/navigation-navkey-serializers.md.
    private val nav3Plugin = JetWhaleNav3AgentPlugin(
        Nav3KeyCodec.openPolymorphic(navKeySerializersProvider.serializersModule),
    )

    init {
        // HttpSend attaches to an already-built client, leaving core:data's provider untouched. It
        // has no way to unregister and does not reject duplicates, so this must run exactly once —
        // which the AppScope singleton guarantees.
        httpClient.plugin(HttpSend).intercept(networkPlugin.ktorSendInterceptor(httpClient))

        startJetWhale {
            connection {
                host = JETWHALE_HOST
                port = JETWHALE_PORT
            }
            plugins {
                register(networkPlugin)
                register(nav3Plugin)
                register(JetWhaleSemanticsAgentPlugin())
            }
        }
    }

    @Composable
    override fun invoke(backStack: NavBackStack<NavKey>) {
        nav3Plugin.TrackNavBackStack(backStack)
    }

    @Composable
    override fun invoke() {
        SemanticsProbe()
    }
}
