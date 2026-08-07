package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.kitakkun.jetwhale.agent.runtime.startJetWhale
import com.kitakkun.jetwhale.annotations.ExperimentalJetWhaleApi
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
import io.github.droidkaigi.confsched.core.common.AppInitializer
import io.github.droidkaigi.confsched.core.common.BackStackDebuggingEffect
import io.github.droidkaigi.confsched.core.common.MergedNavKeySerializersProvider
import io.github.droidkaigi.confsched.core.common.NoopAppInitializer
import io.github.droidkaigi.confsched.core.common.NoopBackStackDebuggingEffect
import io.github.droidkaigi.confsched.core.common.NoopSemanticsDebuggingEffect
import io.github.droidkaigi.confsched.core.common.SemanticsDebuggingEffect
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin

// The JetWhale host's default ports. It binds plain ws to loopback and serves wss on the LAN, so a
// device that cannot reach loopback has to take the secure one.
private const val JETWHALE_HOST = "localhost"
private const val JETWHALE_WS_PORT = 5080
private const val JETWHALE_WSS_PORT = 5443

@OptIn(ExperimentalJetWhaleApi::class)
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding<AppInitializer>(), replaces = [NoopAppInitializer::class])
@ContributesBinding(AppScope::class, binding<BackStackDebuggingEffect>(), replaces = [NoopBackStackDebuggingEffect::class])
@ContributesBinding(AppScope::class, binding<SemanticsDebuggingEffect>(), replaces = [NoopSemanticsDebuggingEffect::class])
class JetWhaleDebugger(
    private val httpClient: HttpClient,
    navKeySerializersProvider: MergedNavKeySerializersProvider,
) : AppInitializer,
    BackStackDebuggingEffect,
    SemanticsDebuggingEffect {
    private val networkPlugin = JetWhaleNetworkAgentPlugin()

    // The app registers its NavKeys open-polymorphically, which is also how the host decodes the
    // keys it pushes — see docs/navigation-navkey-serializers.md.
    private val nav3Plugin = JetWhaleNav3AgentPlugin(
        Nav3KeyCodec.openPolymorphic(navKeySerializersProvider.serializersModule),
    )

    override fun initialize() {
        // HttpSend attaches to an already-built client, leaving core:data's provider untouched. It
        // has no way to unregister and does not reject duplicates, so this must run exactly once —
        // which the AppScope singleton plus the single entry-point call guarantees.
        httpClient.plugin(HttpSend).intercept(networkPlugin.ktorSendInterceptor(httpClient))

        startJetWhale {
            connection {
                // Candidates are tried in order. Loopback covers the emulator, the simulator, the
                // desktop app and the browser; the baked-in build machine address covers a physical
                // iPhone, which sees neither loopback nor the host's plain-ws port.
                endpoints {
                    ws(JETWHALE_HOST, JETWHALE_WS_PORT)
                    buildMachineWss(JETWHALE_WSS_PORT)
                }
                ssl { trustServerCertificate() }
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
