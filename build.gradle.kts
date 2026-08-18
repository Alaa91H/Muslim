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
