pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Muslim"

// ---------------------------------------------------------------------------
// Multi-module structure (see PROJECT_PROMPT.md §3.3)
// Each feature module depends only on core modules — never on another feature.
// ---------------------------------------------------------------------------

// App entry point
include(":app")

// Core modules
include(":core:core-common")
include(":core:core-design-system")
include(":core:core-ui")
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-network")
include(":core:core-notifications")
include(":core:core-location")
include(":core:core-permissions")

// Feature modules (added incrementally per the 8-phase roadmap)
include(":feature:feature-prayer-times")
include(":feature:feature-qibla")
include(":feature:feature-quran")
include(":feature:feature-hadith")
include(":feature:feature-adhkar")
include(":feature:feature-tasbih")
include(":feature:feature-learn")
include(":feature:feature-ramadan")
include(":feature:feature-zakat")
include(":feature:feature-settings")
include(":feature:feature-reference")
