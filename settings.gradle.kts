pluginManagement {
  includeBuild("build-logics")
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url = uri("https://www.jitpack.io") }
  }
}
rootProject.name = "testbed"

include(":app")
include(":feature")
include(":core")

include(":core:model")
include(":core:ui")
include(":core:annotation")
include(":core:compiler")

include(":feature:home")
include(":feature:search")
include(":feature:result")
include(":feature:holographic")
include(":feature:compose")
include(":feature:dialog")
include(":simpledialog")
include(":annotation")
include(":deeplink")
//include(":blur")
include(":coroutineext")
