plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.nav.safeargs.kotlin) apply false
    alias(libs.plugins.kapt) apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.20" apply false
}