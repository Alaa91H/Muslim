plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "org.muslim.app.core.cast"
    compileSdk { version = release(37) }
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.google.play.services.cast)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
