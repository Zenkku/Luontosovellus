plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)  // Tarvitaan google-services.json:lle
}

android {
    namespace = "com.example.luontopeli"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.luontopeli"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.volley)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth.ktx)

    // Compose BOM — hallitsee Compose-kirjastojen versiot automaattisesti
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt (riippuvuusinjektio)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // OpenStreetMap — kartat ilman API-avainta (lisätään viikolla 3)
    implementation(libs.osmdroid.android)

    // CameraX (lisätään viikolla 4)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)

    // Coil — kuvan lataus (lisätään viikolla 4)
    implementation(libs.coil.compose)

    // ML Kit — kasvintunnistus (lisätään viikolla 5)
    implementation(libs.mlkit.image.labeling)

    // Splash Screen (lisätään viikolla 7)
    implementation(libs.androidx.core.splashscreen)

    // Accompanist Permissions — ajonaikaiset luvat (lisätään viikolla 2–3)
    implementation(libs.accompanist.permissions)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // Firebase BOM – hallitsee kaikkien Firebase-kirjastojen versiot automaattisesti
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)       // Authentication – käyttäjätunnistus
    implementation(libs.firebase.firestore)  // Firestore – löytöjen metadata pilveen

    // Guava – ratkaisee Firebase + CameraX ListenableFuture -ristiriidan
    implementation("com.google.guava:guava:32.1.3-android")
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}