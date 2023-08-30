plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.nav.safeargs) apply false
  alias(libs.plugins.kapt) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.hilt) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.shadow) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.ktlint)
  alias(libs.plugins.detekt)
  alias(libs.plugins.nexus.publish)
  alias(libs.plugins.dokka)
}

apply {
  from("${rootProject.projectDir}/scripts/publish-root.gradle")
  from("publish.gradle")
}

tasks.register("cleanAll", type = Delete::class) {
  allprojects.map(Project::getBuildDir).forEach(::delete)
}

subprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  apply(plugin = "io.gitlab.arturbosch.detekt")

  configurations {
    ktlint
    detekt
  }

  repositories {
    mavenCentral()
  }

  detekt {
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt-config.yml"))
  }

  ktlint {
    debug.set(true)
    verbose.set(true)
  }
}
