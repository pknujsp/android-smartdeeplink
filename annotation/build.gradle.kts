plugins {
    id("org.jetbrains.kotlin.jvm")
}

/*
rootProject.extra.apply {
    set("PUBLISH_ARTIFACT_ID", "smartdeeplink.annotation")
    set("PUBLISH_DESCRIPTION", "annotation processor of SmartDeepLink Library")
}

apply {
    from("${rootProject.projectDir}/scripts/publish-module.gradle")
}

 */

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.13.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
}