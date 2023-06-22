plugins {
  id("plugin.application")
}

android {
  namespace = "io.github.pknujsp.testbed"

  defaultConfig {
    minSdk = libs.versions.min.sdk.get().toInt()
    applicationId = "io.pknujsp.testbed"
    versionCode = 1
    versionName = "1.0.0"
  }
}

hilt {
  enableAggregatingTask = true
}

kapt {
  correctErrorTypes = true
}

dependencies {
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")

  implementation(project(":feature:compose"))
  implementation(project(":feature:home"))
  implementation(project(":feature:result"))
  implementation(project(":feature:search"))
  implementation(project(":feature:holographic"))
  implementation(project(":core:ui"))
}
