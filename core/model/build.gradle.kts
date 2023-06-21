plugins {
  id("plugin.library")
}

android {
  namespace = "io.pknujsp.testbed.core.model"
}

dependencies {
  //implementation(project(":deeplink"))
  //implementation(project(":annotation"))
  //"kapt"(project(":annotation"))

  implementation("io.github.pknujsp:smartdeeplink.core:1.0.0-rc02")
  implementation("io.github.pknujsp:smartdeeplink.annotation:1.0.0-rc02")
}
