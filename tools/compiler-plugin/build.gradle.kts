plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.droidkaigiPrimitiveSpotless)
    `java-test-fixtures`
}

kotlin {
    jvmToolchain(21)
}

// The official compiler test framework's layout: hand-written runners in test-fixtures, JUnit 5
// classes generated from testData into test-gen, and the stubs testData resolves its imports against.
sourceSets {
    test {
        java.setSrcDirs(listOf("test-gen"))
    }
    testFixtures {
        java.setSrcDirs(listOf("test-fixtures"))
    }
    create("testStubs") {
        java.setSrcDirs(listOf("test-stubs"))
    }
}

val testStubsJar by tasks.registering(Jar::class) {
    archiveClassifier.set("test-stubs")
    from(sourceSets["testStubs"].output)
}

// Jars the test framework locates by absolute path through the system properties set below.
val testArtifacts: Configuration by configurations.creating

dependencies {
    // compileOnly (never bundled) and the version must match the consumer's Kotlin, else the plugin fails to load with NoSuchMethodError/LinkageError.
    compileOnly(libs.kotlinCompiler)

    testFixturesApi(libs.kotlinCompiler)
    testFixturesApi(libs.kotlinCompilerInternalTestFramework)
    testFixturesApi(libs.kotlinTestJunit5)
    testFixturesApi(libs.kotlinComposeCompilerPlugin)
    // The framework's assertion utilities still run on JUnit 4 at test runtime.
    testFixturesRuntimeOnly(libs.junit)

    testArtifacts(libs.kotlinStdlib)
    testArtifacts(libs.kotlinStdlibJdk8)
    testArtifacts(libs.kotlinReflect)
    testArtifacts(libs.kotlinTest)
    testArtifacts(libs.kotlinScriptRuntime)
    testArtifacts(libs.kotlinAnnotationsJvm)
}

val generateTests by tasks.registering(JavaExec::class) {
    inputs.dir(layout.projectDirectory.dir("testData"))
    outputs.dir(layout.projectDirectory.dir("test-gen"))
    classpath = sourceSets.testFixtures.get().runtimeClasspath
    mainClass.set("io.github.droidkaigi.confsched.enforcement.GenerateTestsKt")
    workingDir = rootDir
}

// The generator emits JUnit 5 classes as .java, so both compile tasks consume test-gen.
tasks.compileTestKotlin { dependsOn(generateTests) }
tasks.compileTestJava { dependsOn(generateTests) }

tasks.test {
    dependsOn(testArtifacts, testStubsJar)
    useJUnitPlatform()
    workingDir = rootDir
    // The generated test classes only name the fixtures; editing one changes no declared input,
    // so without this the task stays up to date and silently keeps the previous verdict.
    inputs.dir(layout.projectDirectory.dir("testData"))

    systemProperty("idea.home.path", rootDir)
    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("enforcement.test.stubs.jar", testStubsJar.get().archiveFile.get().asFile.absolutePath)
    // -Penforcement.updateTestData=true rewrites every testData file with the diagnostics actually
    // reported; see TestDataUpdater.
    systemProperty(
        "enforcement.test.updateTestData",
        providers.gradleProperty("enforcement.updateTestData").getOrElse("false"),
    )

    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = testArtifacts.files
        .find { """$jarName-\d.*""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: error("testArtifacts is missing $jarName — add the matching `testArtifacts(...)` coordinate")
    systemProperty(propName, path)
}

// The Kotlin Gradle plugin loads compiler plugins into kotlin-compiler-embeddable, whose IntelliJ
// classes live under org.jetbrains.kotlin.com.intellij, while the plugin compiles against the
// un-shaded kotlin-compiler so it can share a classpath with the compiler test framework. Every
// consuming module therefore takes this relocated artifact, never the plain jar.
tasks.shadowJar {
    // A classifier of its own: an unclassified name is the `jar` task's output path, and whichever
    // task ran last would win it — leaving the app build with the un-relocated classes half the time.
    archiveClassifier.set("relocated")
    relocate("com.intellij", "org.jetbrains.kotlin.com.intellij")
    // Relocation is the only reason this task exists; the compiler supplies the stdlib, so bundling
    // any dependency would put a second copy of it on the compiler's classpath.
    configurations.set(emptyList())
}

// Widens the convention plugin's `src/**` target; test-gen and testData stay out — one is
// generated, the other is deliberately malformed Kotlin carrying diagnostic markers.
spotless {
    kotlin {
        target("src/**/*.kt", "test-fixtures/**/*.kt", "test-stubs/**/*.kt")
    }
}
