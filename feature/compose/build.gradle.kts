import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
  id("plugin.view")
}

android {
    namespace = "com.pknujsp.testbed.feature.compose"
}

dependencies {


    implementation(project(":core:model"))
    implementation(project(":core:ui"))
}
