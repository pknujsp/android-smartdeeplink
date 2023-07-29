plugins {
  id("plugin.application")
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "io.github.pknujsp.testbed"

  defaultConfig {
    minSdk = libs.versions.min.sdk.get().toInt()
    applicationId = "io.pknujsp.testbed"
    versionCode = 1
    versionName = "1.0.0"

    vectorDrawables {
      useSupportLibrary = true
    }
  }

  @Suppress("UnstableApiUsage") buildFeatures {
    compose = true
  }

  composeOptions {
    useLiveLiterals = true
    kotlinCompilerExtensionVersion = libs.versions.androidx.compose.kotlin.compiler.get().toString()
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  hilt {
    enableAggregatingTask = true
  }

  packaging.resources.excludes.run {
    add("/META-INF/{AL2.0,LGPL2.1}")

  }
}

kapt {
  correctErrorTypes = true
}

dependencies {
  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.bundles.compose.hilt)
  implementation(libs.bundles.compose.navigation)
  implementation(libs.bundles.compose.runtime)
  implementation(libs.bundles.compose.activity)
  implementation(libs.bundles.compose.core)
  implementation(libs.bundles.compose.viewmodel)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.ui.graphics)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.ui.test.junit4)
  debugImplementation(libs.bundles.compose.debug)

  implementation(project(":feature:compose"))
  implementation(project(":feature:home"))
  implementation(project(":feature:result"))
  implementation(project(":feature:search"))
  implementation(project(":feature:holographic"))
  implementation(project(":feature:dialog"))
  implementation(project(":core:ui"))
  debugImplementation(libs.ui.test.manifest)
}
