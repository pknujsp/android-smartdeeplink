plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.nav.safeargs.kotlin)
    alias(libs.plugins.kapt)
}

android {
    namespace = "com.pknujsp.feature.search"

    @Suppress("UnstableApiUsage")
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)

    implementation(libs.kotlin.poet)
    implementation(libs.kotlin.reflection)
    implementation(libs.google.autocommon)
    implementation(libs.material)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment)
    kapt(libs.androidx.lifecycle.compilerkapt)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}