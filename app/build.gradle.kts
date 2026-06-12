plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // Thêm plugin Gradle của dịch vụ Google
    id("com.google.gms.google-services")
}

// Khai báo cấu phần phiên bản chính (Major)
val phienBanMajor = 1
// Khai báo cấu phần phiên bản phụ (Minor)
val phienBanMinor = 0
// Khai báo cấu phần phiên bản vá lỗi (Patch)
val phienBanPatch = 0

android {
    namespace = "com.example.learnflash"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.learnflash"
        minSdk = 24
        targetSdk = 36
        // Tính toán mã phiên bản tự động theo thuật toán Major * 10000 + Minor * 100 + Patch
        versionCode = phienBanMajor * 10000 + phienBanMinor * 100 + phienBanPatch
        // Gán chuỗi tên phiên bản theo định dạng chuẩn Major.Minor.Patch
        versionName = "$phienBanMajor.$phienBanMinor.$phienBanPatch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
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

    // Thêm bộ quản lý thư viện Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.14.0"))
    // Firestore SDK — dùng artifact chính thay vì -ktx đã deprecated từ BOM 33+
    implementation("com.google.firebase:firebase-firestore")
    // Coroutines Play Services — cung cấp hàm .await() để gọi Firebase trong Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.0")

    // Khai báo thư viện mở rộng chứa toàn bộ Icon cho Compose
    implementation("androidx.compose.material:material-icons-extended")

    // Room Database
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // Retrofit & Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.2")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ViewModel & Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}