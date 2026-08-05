package io.github.droidkaigi.confsched.enforcement

import io.github.droidkaigi.confsched.enforcement.runners.AbstractEnforcementDiagnosticTest
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5

// Paths are resolved against the generateTests task's workingDir, which is the repository root.
fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(
            testsRoot = "tools/compiler-plugin/test-gen",
            testDataRoot = "tools/compiler-plugin/testData",
        ) {
            testClass<AbstractEnforcementDiagnosticTest> {
                model("diagnostics")
            }
        }
    }
}
