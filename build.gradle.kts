plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.dokka)
}

apply {
    from("${rootProject.projectDir}/scripts/publish-root.gradle")
    from("publish.gradle")
}

tasks.withType(Delete::class.java) {
    delete(rootProject.buildDir)
}