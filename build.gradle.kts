plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.nav.safeargs.kotlin) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

buildscript {
    dependencies {
        this.classpath("io.github.gradle-nexus.publish-plugin:publish-plugin:1.1.0")
    }
}

apply {
    from("publish-root.gradle.kts")
}