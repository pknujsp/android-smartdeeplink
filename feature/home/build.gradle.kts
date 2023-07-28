plugins {
  id("plugin.view")
}

android {
  namespace = "io.github.pknujsp.testbed.feature.home"

  hilt {
    enableAggregatingTask = true
  }
}

kapt {
  correctErrorTypes = true
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:ui"))
  implementation(project(":feature:compose"))
  implementation(libs.smartdeeplink.core)
}
