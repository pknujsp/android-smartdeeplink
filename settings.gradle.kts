pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") }
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
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "DeepLinkLibraryTest"
include(":app")
include(":feature")
include(":core")
include(":core:model")
include(":feature:home")
include(":feature:search")
include(":feature:result")
include(":deeplink")
include(":annotationprocessor")
include(":annotation")
include(":keys")