import java.io.File
import java.util.Base64
import java.util.Properties

val gitVersionTag = providers.exec {
    commandLine("git", "describe", "--tags", "--match", "v*", "--always")
    workingDir = rootProject.projectDir
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim() }

fun deriveVersion(describe: String, envTag: String): Pair<Int, String> {
    val match = Regex("v?(\\d+)\\.(\\d+)\\.(\\d+)").find(envTag.ifBlank { describe })
    val major = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
    val minor = match?.groupValues?.get(2)?.toIntOrNull() ?: 0
    val patch = match?.groupValues?.get(3)?.toIntOrNull() ?: 0
    return major * 10_000 + minor * 100 + patch to if (match == null) "1.0.0-dev" else "$major.$minor.$patch"
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.muslim.app.wear"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Data Layer communication is restricted to the same application id
        // and signing certificate on the paired phone and watch.
        applicationId = "org.muslim.app"
        minSdk = 30
        targetSdk = 37
        val (code, name) = deriveVersion(gitVersionTag.get(), System.getenv("VERSION_TAG").orEmpty())
        versionCode = code
        versionName = name
    }

    signingConfigs {
        create("release") {
            val properties = Properties().apply {
                val file = rootProject.file("keystore.properties")
                if (file.exists()) file.inputStream().use(::load)
            }
            val storeFilePath = properties.getProperty("storeFile")
            if (storeFilePath != null) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = properties.getProperty("storePassword")
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPassword")
            } else {
                val encodedKeystore = System.getenv("SIGNING_KEYSTORE")
                if (!encodedKeystore.isNullOrBlank()) {
                    val decoded = Base64.getDecoder().decode(encodedKeystore)
                    val keystore = File(System.getProperty("java.io.tmpdir"), "muslim-release-keystore.jks")
                    keystore.writeBytes(decoded)
                    storeFile = keystore
                    storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                    keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                    keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
                }
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
        buildConfig = true
    }

    lint {
        // The watch UI is professionally maintained in Arabic and English; other
        // app locales fall back safely instead of carrying copied translations.
        disable += "MissingTranslation"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.google.play.services.wearable)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
