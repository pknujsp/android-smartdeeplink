import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
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
        suppressWarnings = false
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

rootProject.extra.apply {
    set("PUBLISH_ARTIFACT_ID", "smartdeeplink.core")
    set("PUBLISH_DESCRIPTION", "Core of SmartDeepLink Library")
}

tasks.withType(GenerateModuleMetadata::class).configureEach {
    dependsOn("androidSourcesJar")
}

dependencies {
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.kotlin.reflection)
    api(project(":annotation"))
}

apply {
    from("${rootProject.projectDir}/scripts/publish-module.gradle")
}