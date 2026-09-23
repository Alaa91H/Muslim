import java.io.File
import java.util.Base64
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory

private fun readAndroidStrings(file: File): Map<String, String> {
    if (!file.isFile) return emptyMap()
    val document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(file)
    val strings = linkedMapOf<String, String>()
    val children = document.documentElement.childNodes
    for (index in 0 until children.length) {
        val node = children.item(index)
        if (node is org.w3c.dom.Element && node.tagName == "string") {
            val name = node.getAttribute("name")
            if (name.isNotBlank()) strings[name] = node.textContent
        }
    }
    return strings
}

private fun escapeWearXml(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

val muslimApplicationId = providers.gradleProperty("muslim.applicationId").get()

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
        applicationId = muslimApplicationId
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
        // Wear strings are generated from the same locale corpus as the phone.
        // Keep the lint block available for module-specific checks without
        // suppressing missing translations.
    }
}

val wearLocaleCatalogRes = rootProject.file("feature/feature-settings/src/main/res")
val wearPrayerRes = rootProject.file("feature/feature-prayer-times/src/main/res")
val wearTasbihRes = rootProject.file("feature/feature-tasbih/src/main/res")
val generatedWearLocaleRes = layout.buildDirectory.dir("generated/wear-locale-res")

/**
 * Reuses the exact translations already shipped by the phone app instead of
 * maintaining a second translation corpus for Wear OS. The settings module is
 * the locale catalog because its language picker is built from the merged APK
 * locale set. Prayer/tasbih strings use their localized value when present and
 * the same Arabic default fallback Android uses in the phone app otherwise.
 */
val generateWearLocaleResources = tasks.register("generateWearLocaleResources") {
    inputs.files(
        fileTree(wearLocaleCatalogRes) { include("values*/strings.xml") },
        fileTree(wearPrayerRes) { include("values*/strings.xml") },
        fileTree(wearTasbihRes) { include("values*/strings.xml") },
    )
    outputs.dir(generatedWearLocaleRes)

    doLast {
        val outputRoot = generatedWearLocaleRes.get().asFile
        outputRoot.deleteRecursively()

        val localeDirs = wearLocaleCatalogRes.listFiles()
            .orEmpty()
            .filter { directory ->
                directory.isDirectory &&
                    (directory.name == "values" || directory.name.matches(Regex("""values-[a-z]{2,3}""")))
            }
            .sortedBy { it.name }

        val prayerDefault = readAndroidStrings(File(wearPrayerRes, "values/strings.xml"))
        val tasbihDefault = readAndroidStrings(File(wearTasbihRes, "values/strings.xml"))

        fun valueFor(
            localized: Map<String, String>,
            fallback: Map<String, String>,
            key: String,
        ): String = localized[key] ?: fallback[key]
            ?: error("Missing required Wear translation source: $key")

        localeDirs.forEach { localeDirectory ->
            val qualifier = localeDirectory.name
            val prayer = readAndroidStrings(File(wearPrayerRes, "$qualifier/strings.xml"))
            val tasbih = readAndroidStrings(File(wearTasbihRes, "$qualifier/strings.xml"))

            val values = linkedMapOf(
                "wear_app_name" to "Muslim",
                "wear_next_prayer" to valueFor(prayer, prayerDefault, "home_next_prayer"),
                "wear_no_prayer" to valueFor(prayer, prayerDefault, "next_adhan_no_location"),
                "wear_countdown" to valueFor(prayer, prayerDefault, "next_adhan_remaining"),
                "wear_tasbih" to valueFor(tasbih, tasbihDefault, "tasbih_title"),
                "wear_tasbih_of_target" to valueFor(tasbih, tasbihDefault, "tasbih_of_target"),
                "wear_increment" to valueFor(tasbih, tasbihDefault, "misbaha_widget_tap_hint"),
                "wear_vibration" to valueFor(prayer, prayerDefault, "settings_vibrate"),
                "wear_prayer_fajr" to valueFor(prayer, prayerDefault, "prayer_fajr"),
                "wear_prayer_sunrise" to valueFor(prayer, prayerDefault, "prayer_sunrise"),
                "wear_prayer_dhuhr" to valueFor(prayer, prayerDefault, "prayer_dhuhr"),
                "wear_prayer_asr" to valueFor(prayer, prayerDefault, "prayer_asr"),
                "wear_prayer_maghrib" to valueFor(prayer, prayerDefault, "prayer_maghrib"),
                "wear_prayer_isha" to valueFor(prayer, prayerDefault, "prayer_isha"),
            )

            val outputFile = File(outputRoot, "$qualifier/strings.xml")
            outputFile.parentFile.mkdirs()
            outputFile.writeText(
                buildString {
                    appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
                    appendLine("<resources>")
                    values.forEach { (name, value) ->
                        appendLine("""    <string name="$name">${escapeWearXml(value)}</string>""")
                    }
                    appendLine("</resources>")
                },
                Charsets.UTF_8,
            )
        }

        check(localeDirs.size >= 150) {
            "Wear locale generation unexpectedly found only ${localeDirs.size} app locale directories"
        }
    }
}

android.sourceSets.getByName("main").res.directories.add(generatedWearLocaleRes.get().asFile.absolutePath)
tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(generateWearLocaleResources)
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.google.play.services.wearable)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
