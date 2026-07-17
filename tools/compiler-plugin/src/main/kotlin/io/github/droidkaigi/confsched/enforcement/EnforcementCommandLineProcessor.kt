@file:OptIn(org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi::class)

package io.github.droidkaigi.confsched.enforcement

import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

class EnforcementCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = PluginNames.PLUGIN_ID
    override val pluginOptions: Collection<CliOption> = emptyList()
}
