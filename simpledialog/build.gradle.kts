plugins {
  id("plugin.release.android.library")
  alias(libs.plugins.dokka)
  alias(libs.plugins.kapt)
}

android {
  namespace = "io.github.pknujsp.simpledialog"

  buildTypes {
    release {
      isMinifyEnabled = false
    }
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }
}

rootProject.extra.apply {
  set("PUBLISH_ARTIFACT_ID", "simpledialog")
  set("PUBLISH_DESCRIPTION", "Simple dialog library")
  set("PUBLISH_VERSION", "1.0.0")
}

tasks.withType(GenerateModuleMetadata::class).configureEach {
  dependsOn("androidSourcesJar")
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
}

apply {
  from("${rootProject.projectDir}/scripts/publish-module.gradle")
}
