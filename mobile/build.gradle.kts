import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val credentialProps = Properties().apply {
    val credentialsFile = rootProject.file("credentials.properties")
    if (credentialsFile.exists()) {
        credentialsFile.inputStream().use { load(it) }
    }
}
val apiBaseUrl: String = credentialProps.getProperty("BASE_URL") ?: ""

android {
    namespace = "com.engineerfred.beststreamsug.mobile"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.engineerfred.beststreamsug.mobile"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    testImplementation(libs.junit)
}
