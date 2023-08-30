plugins {
  id("plugin.release.android.library")
  alias(libs.plugins.dokka)
  alias(libs.plugins.kapt)
}

android {
  namespace = "io.github.pknujsp.simpledialog"

  defaultConfig {
    renderscriptTargetApi = 24
    renderscriptNdkModeEnabled = false
    renderscriptSupportModeEnabled = true
  }

  buildTypes {
    release {
      isMinifyEnabled = false
    }
  }

  externalNativeBuild {
    cmake {
      path("src/main/cpp/CMakeLists.txt")
      version = "3.22.1"
    }
  }

}

rootProject.extra.apply {
  set("PUBLISH_ARTIFACT_ID", "simpledialog")
  set("PUBLISH_VERSION", "1.0.2")
  set("PUBLISH_DESCRIPTION", "Android Simple Dialog Library")
  set("PUBLISH_URL", "https://github.com/pknujsp/android-simpledialog")
  set("PUBLISH_SCM_CONNECTION", "scm:git:github.com/pknujsp/android-simpledialog")
  set("PUBLISH_SCM_DEVELOPER_CONNECTION", "scm:git:ssh://github.com/pknujsp/android-simpledialog.git")
  set("PUBLISH_SCM_URL", "https://github.com/pknujsp/android-simpledialog.git")
}

tasks.withType(GenerateModuleMetadata::class).configureEach {
  dependsOn("androidSourcesJar")
}

apply {
  from("${rootProject.projectDir}/scripts/publish-android-module.gradle")
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
}
