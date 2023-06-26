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
  set("PUBLISH_ARTIFACT_ID", "blur")
  set("PUBLISH_VERSION", "1.0.0")
  set("PUBLISH_DESCRIPTION", "Android Blur Processing Library")
  set("PUBLISH_URL", "https://github.com/pknujsp/android-blur")
  set("PUBLISH_SCM_CONNECTION", "scm:git:github.com/pknujsp/android-blur")
  set("PUBLISH_SCM_DEVELOPER_CONNECTION", "scm:git:ssh://github.com/pknujsp/android-blur.git")
  set("PUBLISH_SCM_URL", "https://github.com/pknujsp/android-blur.git")
}

tasks.withType(GenerateModuleMetadata::class).configureEach {
  dependsOn("androidSourcesJar")
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.lifecycle.runtime.ktx)
}

apply {
  from("${rootProject.projectDir}/scripts/publish-module.gradle")
}
