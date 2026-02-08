plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.jaxxnitt.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jaxxnitt.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Twilio credentials from local.properties
        buildConfigField("String", "TWILIO_ACCOUNT_SID", "\"${localProperties.getProperty("TWILIO_ACCOUNT_SID", "")}\"")
        buildConfigField("String", "TWILIO_AUTH_TOKEN", "\"${localProperties.getProperty("TWILIO_AUTH_TOKEN", "")}\"")
        buildConfigField("String", "TWILIO_FROM_PHONE_NUMBER", "\"${localProperties.getProperty("TWILIO_FROM_PHONE_NUMBER", "")}\"")
        buildConfigField("String", "TWILIO_WHATSAPP_FROM_NUMBER", "\"${localProperties.getProperty("TWILIO_WHATSAPP_FROM_NUMBER", "")}\"")

        // Razorpay
        buildConfigField("String", "RAZORPAY_KEY_ID", "\"${localProperties.getProperty("RAZORPAY_KEY_ID", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Retrofit + OkHttp
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    // Coil for image loading
    implementation(libs.coil.compose)

    // Lottie for animations
    implementation(libs.lottie.compose)

    // Razorpay
    implementation(libs.razorpay)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Google Sign-In with Credential Manager
    implementation(libs.play.services.auth)
    implementation(libs.credential.manager)
    implementation(libs.credential.manager.play.services)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}