plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kapt)
  alias(libs.plugins.dokka)
}

rootProject.extra.apply {
  set("PUBLISH_ARTIFACT_ID", "smartdeeplink-annotation")
  set("PUBLISH_VERSION", "1.0.0-rc05")
  set("PUBLISH_DESCRIPTION", "annotation processor of SmartDeepLink Library")
  set("PUBLISH_URL", "https://github.com/pknujsp/android-smartdeeplink")
  set("PUBLISH_SCM_CONNECTION", "scm:git:github.com/pknujsp/android-smartdeeplink.git")
  set("PUBLISH_SCM_DEVELOPER_CONNECTION", "scm:git:ssh://github.com/pknujsp/android-smartdeeplink.git")
  set("PUBLISH_SCM_URL", "https://github.com/pknujsp/android-smartdeeplink.git")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

apply {
  from("${rootProject.projectDir}/scripts/publish-jvm-module.gradle")
}

dependencies {
  implementation("com.squareup:kotlinpoet:1.13.2")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
}
