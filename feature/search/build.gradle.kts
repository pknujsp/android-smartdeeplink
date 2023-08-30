plugins {
  id("plugin.view")
}

android {
  namespace = "io.github.pknujsp.testbed.feature.search"
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
//  implementation(project(":deeplink"))
}
