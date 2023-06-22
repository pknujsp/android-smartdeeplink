import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

internal fun Project.configureReleaseLibraryKotlinAndroid(
  commonExtension: CommonExtension<*, *, *, *>,
) {

  val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

  commonExtension.apply {
    compileSdk = libs.findVersion("compile_sdk").get().toString().toInt()

    @Suppress("UnstableApiUsage")
    buildFeatures {
      buildConfig = true
    }

    defaultConfig {
      minSdk = libs.findVersion("min_sdk").get().toString().toInt()
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
  }
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
