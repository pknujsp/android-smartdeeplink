plugins {
  id("plugin.view")
}

android {
  namespace = "io.github.pknujsp.testbed.core.ui"
  compileSdk = libs.versions.compile.sdk.get().toInt()
}

hilt {
  enableAggregatingTask = true
}

kapt {
  correctErrorTypes = true
}

dependencies {
  //implementation(project(":blur"))
}
