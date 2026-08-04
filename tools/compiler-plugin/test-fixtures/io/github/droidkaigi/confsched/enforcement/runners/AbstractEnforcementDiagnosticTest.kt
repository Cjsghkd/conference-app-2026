package io.github.droidkaigi.confsched.enforcement.runners

import io.github.droidkaigi.confsched.enforcement.services.TestDataUpdater
import io.github.droidkaigi.confsched.enforcement.services.configureEnforcementPlugin
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.configuration.baseFirDiagnosticTestConfiguration
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.directives.configureFirParser
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

// LightTree is what the Kotlin Gradle plugin feeds the compiler, so the tests exercise the parser
// the enforcement rules actually run against in the app build.
abstract class AbstractEnforcementDiagnosticTest : AbstractKotlinCompilerTest() {
    // The default provider resolves jars out of a Kotlin monorepo checkout; this one reads the
    // org.jetbrains.kotlin.test.* system properties the test task sets.
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider =
        EnvironmentBasedStandardLibrariesPathProvider

    override fun configure(builder: TestConfigurationBuilder) {
        with(builder) {
            baseFirDiagnosticTestConfiguration()
            configureFirParser(FirParser.LightTree)
            defaultDirectives {
                +ConfigurationDirectives.WITH_STDLIB
                // The mock JDK the framework defaults to omits supertypes of String and Enum,
                // which turns ordinary stdlib calls in testData into MISSING_DEPENDENCY_SUPERCLASS.
                +JvmEnvironmentConfigurationDirectives.FULL_JDK
                // Matches the target the test-stubs jar is compiled for, so inlining a stub
                // function does not raise INLINE_FROM_HIGHER_PLATFORM.
                JvmEnvironmentConfigurationDirectives.JVM_TARGET with JvmTarget.JVM_21
            }
            configureEnforcementPlugin()
            useAfterAnalysisCheckers(::TestDataUpdater)
        }
    }
}
