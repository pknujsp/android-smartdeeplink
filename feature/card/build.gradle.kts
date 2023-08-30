plugins {
  id("plugin.view")
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "io.github.pknujsp.testbed.feature.card"
}

hilt {
  enableAggregatingTask = true
}

kapt {
  correctErrorTypes = true
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:ui"))
}
