plugins {
  alias(libs.plugins.ksp)
  id("plugin.view")
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "io.github.pknujsp.testbed.feature.compose"

  @Suppress("UnstableApiUsage")
  buildFeatures {
    compose = true
  }

  composeOptions {
    useLiveLiterals = true
    kotlinCompilerExtensionVersion = libs.versions.androidx.compose.kotlin.compiler.get().toString()
  }

  hilt {
    enableAggregatingTask = true
  }
}

dependencies {
  ksp(project(":core:compiler"))
  implementation(project(":core:annotation"))
  implementation(libs.bundles.retrofit)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.compose.hilt)
  implementation(libs.bundles.compose.navigation)
  implementation(libs.bundles.compose.runtime)
  implementation(libs.bundles.compose.activity)
  implementation(libs.bundles.compose.core)
  implementation(libs.bundles.compose.viewmodel)
  debugImplementation(libs.bundles.compose.debug)
  implementation(libs.jsoup)
}
