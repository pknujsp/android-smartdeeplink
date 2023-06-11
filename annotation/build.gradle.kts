plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")
    api("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
}