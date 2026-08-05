@file:OptIn(org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi::class)

package io.github.droidkaigi.confsched.enforcement.services

import androidx.compose.compiler.plugins.kotlin.k2.ComposeFirExtensionRegistrar
import io.github.droidkaigi.confsched.enforcement.EnforcementCompilerPluginRegistrar
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File

private const val STUBS_JAR_PROPERTY = "enforcement.test.stubs.jar"

fun TestConfigurationBuilder.configureEnforcementPlugin() {
    useConfigurators(::EnforcementEnvironmentConfigurator)
}

internal class EnforcementEnvironmentConfigurator(
    testServices: TestServices,
) : EnvironmentConfigurator(testServices) {

    // testData resolves the app's and its libraries' types against this jar; a runtime-classpath
    // provider would come too late, since the checkers match on types the test compiler resolves.
    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        val stubsJar = System.getProperty(STUBS_JAR_PROPERTY)
            ?: error("System property $STUBS_JAR_PROPERTY is not set — check the tasks.test wiring")
        configuration.addJvmClasspathRoot(File(stubsJar))
    }

    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        // Supplies the @Composable function-type kind; without it such lambdas resolve to the plain
        // kotlin.FunctionN kind and rules branching on FunctionTypeKind diverge from the app build.
        FirExtensionRegistrarAdapter.registerExtension(ComposeFirExtensionRegistrar())
        with(EnforcementCompilerPluginRegistrar()) { registerExtensions(configuration) }
    }
}
