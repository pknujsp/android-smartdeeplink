plugins {
  id("plugin.release.android.library")
  alias(libs.plugins.dokka)
  alias(libs.plugins.kapt)
}

android {
  namespace = "io.github.pknujsp.blur"

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
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(project(":coroutineext"))
}

apply {
  from("${rootProject.projectDir}/scripts/publish-android-module.gradle")
}
