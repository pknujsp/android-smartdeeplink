plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.dokka)
}

rootProject.extra.apply {
    set("PUBLISH_ARTIFACT_ID", "smartdeeplink.annotation")
    set("PUBLISH_DESCRIPTION", "annotation processor of SmartDeepLink Library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

apply {
    from("${rootProject.projectDir}/scripts/publish-module.gradle")
}

dependencies {
    api("com.squareup:kotlinpoet:1.13.2")
    api("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
}