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
  set("PUBLISH_VERSION", "1.0.0")
  set("PUBLISH_DESCRIPTION", "Android Simple Dialog Library")
  set("PUBLISH_URL", "https://github.com/pknujsp/android-simpledialog")
  set("PUBLISH_SCM_CONNECTION", "scm:git:github.com/pknujsp/android-simpledialog")
  set("PUBLISH_SCM_DEVELOPER_CONNECTION", "scm:git:ssh://github.com/pknujsp/android-simpledialog.git")
  set("PUBLISH_SCM_URL", "https://github.com/pknujsp/android-simpledialog.git")
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
