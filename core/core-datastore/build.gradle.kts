plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "org.example.islamicapp.core.datastore"
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
    // `api` so feature modules can use the shared DataStore API directly.
    api(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
}
