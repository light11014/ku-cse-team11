plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.ku_cse_team11_mobileapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ku_cse_team11_mobileapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // 디버그: 기본은 Fake 사용
            buildConfigField("boolean", "USE_FAKE_COMMUNITY", "true")
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        release {
            isMinifyEnabled = true
            buildConfigField("boolean", "USE_FAKE_COMMUNITY", "false")
            buildConfigField("String", "BASE_URL", "\"https://api.yourdomain.com/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.benchmark.common)
    implementation(libs.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.runtime)
    implementation(libs.ui)
    implementation(platform(libs.androidx.compose.bom.v20240800))
    implementation(libs.androidx.animation.core.lint)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}