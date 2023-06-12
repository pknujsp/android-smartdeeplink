plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.13.2")
    kapt("com.squareup:kotlinpoet:1.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")
    //compileOnly(project(":annotation"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
}