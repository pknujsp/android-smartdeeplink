plugins {
  id("plugin.view")
}

android {
    namespace = "com.pknujsp.testbed.feature.result"
}

dependencies {
    implementation(project(":core:model"))
    //implementation(project(":deeplink"))
    implementation("io.github.pknujsp:smartdeeplink.core:1.0.0-rc02")
    //implementation("io.github.pknujsp:smartdeeplink.annotation:1.0.0-rc02")

}
