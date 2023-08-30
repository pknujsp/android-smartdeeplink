plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  alias(libs.plugins.dokka)
}

rootProject.extra.apply {
  set("PUBLISH_ARTIFACT_ID", "ksealedbinding-compiler")
  set("PUBLISH_VERSION", "1.0.0")
  set("PUBLISH_DESCRIPTION", "compiler of KSealedBinding Library")
  set("PUBLISH_URL", "https://github.com/pknujsp/KSealedBinding")
  set("PUBLISH_SCM_CONNECTION", "scm:git:github.com/pknujsp/KSealedBinding.git")
  set("PUBLISH_SCM_DEVELOPER_CONNECTION", "scm:git:ssh://github.com/pknujsp/KSealedBinding.git")
  set("PUBLISH_SCM_URL", "https://github.com/pknujsp/KSealedBinding.git")
}

apply {
  from("${rootProject.projectDir}/scripts/publish-jvm-module.gradle")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
  implementation(project(":core:annotation"))
  implementation("com.squareup:kotlinpoet:1.14.2")
  implementation("com.squareup:kotlinpoet-metadata:1.14.2")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.22")
  implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.22")
  implementation(libs.ksp)
}
