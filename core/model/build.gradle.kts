plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
}

android {
    namespace = "com.pknujsp.core.model"
    compileSdk = libs.versions.compile.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.androidx.navigation.fragment.ktx)
    //implementation(project(":deeplink"))
    //implementation(project(":annotation"))
    //"kapt"(project(":annotation"))

    implementation("io.github.pknujsp:smartdeeplink.core:1.0.0-rc02")
    implementation("io.github.pknujsp:smartdeeplink.annotation:1.0.0-rc02")
}