plugins {
    id("kotlin")
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.kotlin.poet)
    implementation(libs.kotlin.reflection)
    implementation(libs.google.autocommon)
    implementation(libs.google.autoservice.annotation)
    implementation(libs.google.autoservice.core)
}