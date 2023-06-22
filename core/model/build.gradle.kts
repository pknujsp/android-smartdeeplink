plugins {
  id("plugin.library")
}

android {
  namespace = "io.github.pknujsp.testbed.core.model"
}

hilt {
  enableAggregatingTask = true
}

kapt {
  correctErrorTypes = true
}

dependencies {
  //implementation(project(":deeplink"))
  //implementation(project(":annotation"))
  //"kapt"(project(":annotation"))

  implementation(libs.smartdeeplink.annotation)
  implementation(libs.smartdeeplink.core)
}
