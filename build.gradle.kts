// Top-level build file.
// Plugins are declared here (with `apply false`) so every module uses the
// exact same versions from gradle/libs.versions.toml.
//
// Note (AGP 9): Kotlin support is built into AGP 9.x — there is no
// `org.jetbrains.kotlin.android` plugin. The Compose compiler and
// kotlinx-serialization compiler plugins are applied per-module and are
// versioned with the built-in Kotlin compiler (2.2.10).

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

// Detekt 1.23.8 remains the stable analyzer, but its legacy Gradle plugin calls
// ReportingExtension.file(String), which Gradle 9.5 deprecates for removal in
// Gradle 10. Run the stable CLI directly instead: same rules and baseline,
// without coupling every Gradle invocation to an obsolete reporting API.
val detektCli by configurations.creating

dependencies {
    detektCli("io.gitlab.arturbosch.detekt:detekt-cli:${libs.versions.detekt.get()}")
}

val detektReportsDir = layout.buildDirectory.dir("reports/detekt")

tasks.register<JavaExec>("detekt") {
    group = "verification"
    description = "Runs Detekt over production and unit-test Kotlin sources."

    classpath = detektCli
    mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")

    val configFile = layout.projectDirectory.file("config/detekt/detekt.yml")
    val baselineFile = layout.projectDirectory.file("config/detekt/detekt-baseline.xml")
    val reportsDir = detektReportsDir.get().asFile

    inputs.file(configFile)
    inputs.file(baselineFile)
    inputs.files(
        fileTree(rootDir) {
            include("**/src/main/**/*.kt", "**/src/test/**/*.kt")
            exclude("**/build/**", "**/.freebuff/**")
        },
    )
    outputs.dir(detektReportsDir)

    doFirst {
        reportsDir.mkdirs()
    }

    args(
        "--input", rootDir.absolutePath,
        "--config", configFile.asFile.absolutePath,
        "--baseline", baselineFile.asFile.absolutePath,
        "--includes", "**/src/main/**/*.kt,**/src/test/**/*.kt",
        "--excludes", "**/build/**,**/.freebuff/**",
        "--parallel",
        "--report", "html:${reportsDir.resolve("detekt.html").absolutePath}",
        "--report", "xml:${reportsDir.resolve("detekt.xml").absolutePath}",
        "--report", "sarif:${reportsDir.resolve("detekt.sarif").absolutePath}",
        "--report", "md:${reportsDir.resolve("detekt.md").absolutePath}",
    )
}

// ---------------------------------------------------------------------------
// Global lint configuration.
// Machine-translated content in 184 locales triggers stylistic checks that do
// not apply to translations (ellipsis/dash typography, English typos) and the
// legacy LocaleFolder check, which recommends deprecated Java codes (iw/in/ji)
// instead of the modern BCP-47 codes (he/id/yi) this project uses on purpose.
// ---------------------------------------------------------------------------
subprojects {
    plugins.withId("com.android.application") {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
            lint {
                disable += "LocaleFolder"
                disable += "TypographyEllipsis"
                disable += "TypographyDashes"
                disable += "TypographyOther"
                disable += "Typos"
                disable += "PluralsCandidate"
                disable += "UnusedAttribute"
                disable += "ObsoleteSdkInt"
                disable += "AppBundleLocaleChanges"
                disable += "AndroidGradlePluginVersion"
            }
        }
    }
    plugins.withId("com.android.library") {
        extensions.configure<com.android.build.api.dsl.LibraryExtension> {
            lint {
                disable += "LocaleFolder"
                disable += "TypographyEllipsis"
                disable += "TypographyDashes"
                disable += "TypographyOther"
                disable += "Typos"
                disable += "PluralsCandidate"
                disable += "UnusedAttribute"
                disable += "ObsoleteSdkInt"
                disable += "AppBundleLocaleChanges"
                disable += "AndroidGradlePluginVersion"
            }
        }
    }
}
