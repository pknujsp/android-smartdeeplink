plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

rootProject.extra.apply {
    set("PUBLISH_ARTIFACT_ID", "smartdeeplink.core")
    set("PUBLISH_DESCRIPTION", "core of SmartDeepLink Library")
}

apply {
    from("${rootProject.projectDir}/scripts/publish-module.gradle")
}

tasks.withType(GenerateModuleMetadata::class) {
    mustRunAfter(":deeplink:androidSourcesJar")
}

kapt {
    includeCompileClasspath = false
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
    implementation("androidx.navigation:navigation-runtime-ktx:2.6.0")
    implementation("androidx.fragment:fragment-ktx:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
}