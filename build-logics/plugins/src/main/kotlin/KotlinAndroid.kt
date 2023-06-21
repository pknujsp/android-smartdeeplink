import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

internal fun Project.configureKotlinAndroid(
  commonExtension: CommonExtension<*, *, *, *>,
) {

  val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

  commonExtension.apply {
    compileSdk = libs.findVersion("compile_sdk").get().toString().toInt()

    @Suppress("UnstableApiUsage")
    buildFeatures {
      compose = true
      buildConfig = true
      resValues = false
      shaders = false
    }

    defaultConfig {
      minSdk = libs.findVersion("min_sdk").get().toString().toInt()
    }

    dataBinding.enable = true
    viewBinding.enable = true

    composeOptions {
      useLiveLiterals = true
      kotlinCompilerExtensionVersion = libs.findVersion("androidx_compose_kotlin_compiler").get().toString()
    }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
      isCoreLibraryDesugaringEnabled = true
    }

  }

  configureKotlin()

  dependencies {
    add("coreLibraryDesugaring", libs.findLibrary("desugar-jdk-libs").get())
    add("implementation", libs.findLibrary("kotlin.stdlib").get())
    add("implementation", libs.findBundle("kotlinx").get())
  }
}

fun CommonExtension<*, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
  (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}

/*
alias(libs.plugins.android.application) apply false
alias(libs.plugins.android.library) apply false
alias(libs.plugins.kotlin.android) apply false
alias(libs.plugins.kapt) apply false
alias(libs.plugins.kotlin.jvm) apply false
alias(libs.plugins.hilt) apply false
alias(libs.plugins.ktlint)
alias(libs.plugins.detekt)
alias(libs.plugins.nexus.publish)
alias(libs.plugins.dokka)
 */
