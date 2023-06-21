import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class ApplicationPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      with(pluginManager) {
        apply(libs.findPlugin("android.application").get().get().pluginId)
        apply(libs.findPlugin("kotlin.android").get().get().pluginId)
      }
      extensions.configure<ApplicationExtension> {
        configureKotlinAndroid(this)
        defaultConfig.targetSdk = libs.findVersion("target_sdk").get().toString().toInt()
      }

    }

  }
}
