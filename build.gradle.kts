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
    // Static analysis (Detekt): applied at the root and pointed at the whole
    // source tree in one pass. This avoids per-module plugin wiring, which
    // AGP 9's built-in Kotlin (no org.jetbrains.kotlin.android plugin id)
    // makes awkward. config/detekt/detekt.yml holds the rule set and
    // config/detekt/detekt-baseline.xml pins current findings so the gate
    // fails only on NEW violations.
    alias(libs.plugins.detekt)
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/detekt-baseline.xml")
    parallel = true
    // Analyze every Kotlin source set (main + test) across all modules in a
    // single pass. Build outputs and the Freebuff worktree are excluded.
    source.setFrom(
        files(
            fileTree(rootDir) {
                include("**/src/main/**/*.kt", "**/src/test/**/*.kt")
                exclude("**/build/**", "**/.freebuff/**")
            },
        ),
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
