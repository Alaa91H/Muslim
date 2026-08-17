plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "org.muslim.app.core.location"
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
    implementation(project(":core:core-common"))
    implementation(libs.kotlinx.coroutines.android)

    // GPS via FusedLocationProvider (Google Play services). The app degrades
    // gracefully to manual coordinates / the offline city list when the
    // permission is denied or Play services are unavailable.
    implementation(libs.google.play.services.location)
}
