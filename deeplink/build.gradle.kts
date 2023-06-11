plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    //id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "io.github.pknujsp.smartdeeplink.core"
    compileSdk = 33

    defaultConfig {
        minSdk = 23
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
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
    api(project(":annotation"))
    kapt(project(":annotationprocessor"))
    implementation("androidx.navigation:navigation-common-ktx:2.6.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.6.0")
    implementation("androidx.fragment:fragment-ktx:1.6.0")
}