plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.muslim.app.feature.prayertimes"
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

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        // KT-73255: apply annotations (e.g. @ApplicationContext) to both the
        // value parameter and the property, matching the future Kotlin default.
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    implementation(project(":core:core-design-system"))
    implementation(project(":core:core-ui"))

    implementation(project(":core:core-common"))
    implementation(project(":core:core-datastore"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-notifications"))
    implementation(project(":core:core-location"))
    implementation(project(":core:core-permissions"))
    // Resolves an IANA zone locally from saved GPS/manual coordinates.
    // The map's Android integration requires the packaged native Zstandard AAR.
    implementation(libs.time.zone.map) {
        exclude(group = "com.github.luben", module = "zstd-jni")
    }
    implementation("com.github.luben:zstd-jni:1.4.9-5@aar")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Home-screen widget (Glance) + periodic background refresh (WorkManager)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.okhttp)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    // Instrumented (Compose UI smoke tests — run on a device/emulator). MockK
    // must be the Android artifact here: the JVM `mockk` fails at runtime on a
    // device with "Failed to load plugin … include io.mockk:mockk-android".
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
