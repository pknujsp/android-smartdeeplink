plugins {
  id("plugin.view")
  alias(libs.plugins.dokka)
}

android {
  namespace = "io.github.pknujsp.simpledialog"
  compileSdk = libs.versions.compile.sdk.get().toInt()

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
}

tasks.withType(GenerateModuleMetadata::class).configureEach {
  dependsOn("androidSourcesJar")
}

hilt {
  enableAggregatingTask = true
}

kapt {
  correctErrorTypes = true
}

dependencies {

}

apply {
  from("${rootProject.projectDir}/scripts/publish-module.gradle")
}
