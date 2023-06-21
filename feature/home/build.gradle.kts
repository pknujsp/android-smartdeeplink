plugins {
  id("plugin.view")
}

android {
    namespace = "io.pknujsp.testbed.feature.home"
}

dependencies {
    implementation(project(":core:model"))
}
