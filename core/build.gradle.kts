plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.harold.my_stats"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        buildConfigField("String", "ENDPOINT_URL", "\"https://sparqm.com/web/gabb/debug/debug_report_post.php\"")
        buildConfigField("int", "APP_INSTALL_ID", "1")
        buildConfigField("int", "DEVICE_ID", "1")
        buildConfigField("String", "GIT_BRANCH", "\"priv/my_stats-baseline\"")
    }

    buildFeatures { buildConfig = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-process:2.9.3")
    api("androidx.room:room-runtime:2.8.4")
    api("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}
