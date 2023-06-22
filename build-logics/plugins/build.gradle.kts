plugins {
  `kotlin-dsl`
}

group = "io.github.pknujsp.testbed.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
}

dependencies {
  compileOnly(libs.android.gradle.plugin)
  compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
  plugins {
    register("AndroidLibrary") {
      id = "plugin.library"
      implementationClass = "LibraryPlugin"
    }
    register("Application") {
      id = "plugin.application"
      implementationClass = "ApplicationPlugin"
    }
    register("AndroidView") {
      id = "plugin.view"
      implementationClass = "ViewPlugin"
    }
    register("Hilt") {
      id = "plugin.hilt"
      implementationClass = "HiltPlugin"
    }
  }
}
