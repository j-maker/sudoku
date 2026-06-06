import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

val templateConfig = Properties().apply {
    val file = rootProject.file("app.template.properties")
    check(file.exists()) {
        "Falta app.template.properties en la raíz del proyecto. Ver README.md"
    }
    file.inputStream().use { load(it) }
}

fun templateProp(key: String, default: String = ""): String =
    templateConfig.getProperty(key, default).trim()

val configuredApplicationId =
    templateProp("applicationId", "com.jmaker.sudoku")
val configuredAppSlug = templateProp("appSlug", "app_template")
val configuredAppDisplayName = templateProp("appDisplayName", "App Template")
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.template.android"
    compileSdk = 35

    defaultConfig {
        applicationId = configuredApplicationId
        minSdk = 24
        targetSdk = 35
        versionCode = templateProp("versionCode", "1").toInt()
        versionName = templateProp("versionName", "1.0.0")

        buildConfigField("String", "APP_SLUG", "\"$configuredAppSlug\"")
        buildConfigField("String", "APP_DISPLAY_NAME", "\"$configuredAppDisplayName\"")

        // Mismo valor que appDisplayName → nombre bajo el icono en el launcher
        resValue("string", "app_name", templateProp("appDisplayName", "App Template"))
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            // Mismo applicationId que release → un solo google-services.json (producción).
            // AdMob sigue usando IDs de prueba en debug (AdIds.kt).
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    val signingPropertiesFile = rootProject.file("signing.properties")
    if (signingPropertiesFile.exists()) {
        val signingProperties = Properties().apply {
            signingPropertiesFile.inputStream().use { stream -> load(stream) }
        }
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(signingProperties.getProperty("storeFile"))
                storePassword = signingProperties.getProperty("storePassword")
                keyAlias = signingProperties.getProperty("keyAlias")
                keyPassword = signingProperties.getProperty("keyPassword")
            }
        }
        buildTypes {
            release {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    implementation(libs.play.integrity)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.play.review)
    implementation(libs.play.games)
    implementation(libs.androidx.work)

    debugImplementation(libs.androidx.ui.tooling)
}
