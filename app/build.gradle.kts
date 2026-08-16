import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.example.islamicapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.example.islamicapp"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // Release signing is read from the machine-local keystore.properties
        // (see keystore.properties.example). When absent, the release build
        // falls back to the debug key so `assembleRelease` works out of the box.
        create("release") {
            val props = Properties().apply {
                val file = rootProject.file("keystore.properties")
                if (file.exists()) file.inputStream().use { load(it) }
            }
            val storeFilePath = props.getProperty("storeFile")
            if (storeFilePath != null) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        // False positive: AAPT2 only accepts <adaptive-icon> resources inside a
        // version-qualified `mipmap-anydpi-v26` folder even though minSdk is 26
        // (the plain `mipmap-anydpi` folder fails resource linking).
        disable += "ObsoleteSdkInt"
        // The "newer version available" hints for KSP / the Compose compiler
        // plugin require a Kotlin newer than AGP 9.3's built-in 2.2.10 — the
        // versions in the catalog are pinned to the built-in Kotlin on purpose.
        disable += "GradleDependency"
        disable += "NewerVersionAvailable"
    }
}

dependencies {
    // Project modules
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-datastore"))
    implementation(project(":core:core-notifications"))
    implementation(project(":core:core-location"))
    implementation(project(":feature:feature-prayer-times"))
    implementation(project(":feature:feature-qibla"))
    implementation(project(":feature:feature-quran"))
    implementation(project(":feature:feature-hadith"))
    implementation(project(":feature:feature-adhkar"))
    implementation(project(":feature:feature-tasbih"))
    implementation(project(":feature:feature-ramadan"))
    implementation(project(":feature:feature-zakat"))
    implementation(project(":feature:feature-learn"))
    implementation(project(":feature:feature-settings"))

    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Compose (versions from BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.adaptive)

    // DI
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Debug tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)

    // Instrumented tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
