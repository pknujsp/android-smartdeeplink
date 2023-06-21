plugins {
  id("plugin.view")
}

android {
  namespace = "com.pknujsp.testbed.feature.holographic"
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:ui"))
  implementation("io.github.pknujsp:smartdeeplink.core:1.0.0-rc02")
  implementation("io.github.pknujsp:smartdeeplink.annotation:1.0.0-rc02")
  implementation("androidx.palette:palette:1.0.0")
}
