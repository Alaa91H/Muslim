plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "org.example.islamicapp.core.prayer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Pure-Kotlin astronomical engine (java.time based); no Android deps.
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
