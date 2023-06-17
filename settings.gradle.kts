pluginManagement {
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

rootProject.name = "AndroidLibraryDev"

include(":app")
include(":feature")
include(":core")
include(":core:model")
include(":feature:home")
include(":feature:search")
include(":feature:result")

//include(":deeplink")
//include(":annotation")
include(":core:ui")
include(":feature:holographic")
include(":feature:compose")