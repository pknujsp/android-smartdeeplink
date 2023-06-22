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
  implementation("io.github.pknujsp:smartdeeplink.core:1.0.0-rc02")
  implementation("io.github.pknujsp:smartdeeplink.annotation:1.0.0-rc02")
}
