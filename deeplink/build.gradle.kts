plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kapt)
  alias(libs.plugins.dokka)
}

android {
  namespace = "io.github.pknujsp.smartdeeplink.core"
  compileSdk = 33

  defaultConfig {
    minSdk = 23
  }

  buildTypes {
    release {
      isMinifyEnabled = false
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }

  publishing {
    singleVariant("release") {
      //withSourcesJar()
      //withJavadocJar()
    }
  }
}

rootProject.extra.apply {
  set("PUBLISH_ARTIFACT_ID", "smartdeeplink-core")
  set("PUBLISH_VERSION", "1.0.0-rc06")
  set("PUBLISH_DESCRIPTION", "core of SmartDeepLink Library")
  set("PUBLISH_URL", "https://github.com/pknujsp/android-smartdeeplink")
  set("PUBLISH_SCM_CONNECTION", "scm:git:github.com/pknujsp/android-smartdeeplink.git")
  set("PUBLISH_SCM_DEVELOPER_CONNECTION", "scm:git:ssh://github.com/pknujsp/android-smartdeeplink.git")
  set("PUBLISH_SCM_URL", "https://github.com/pknujsp/android-smartdeeplink.git")
}

tasks.withType(GenerateModuleMetadata::class).configureEach {
  dependsOn("androidSourcesJar")
}

apply {
  from("${rootProject.projectDir}/scripts/publish-android-module.gradle")
}

dependencies {
  implementation(libs.androidx.navigation.fragment.ktx)
  implementation(libs.androidx.fragment)
  implementation(libs.kotlin.reflection)
  implementation(libs.smartdeeplink.annotation)
}
