import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.pknujsp.feature.compose"
    compileSdk = libs.versions.compile.sdk.get().toInt()

    @Suppress("UnstableApiUsage")
    buildFeatures {
        compose = true
    }

    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.kotlin.compiler.get()
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
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)

    implementation(libs.material)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)
    kapt(libs.androidx.lifecycle.compilerkapt)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.bundles.composes)
    implementation(libs.bundles.hilt)

    implementation(project(":core:model"))
    implementation(project(":core:ui"))
}