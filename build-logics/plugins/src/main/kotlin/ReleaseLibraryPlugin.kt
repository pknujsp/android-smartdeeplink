import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class ReleaseLibraryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      pluginManager.apply {
        apply(libs.findPlugin("android.library").get().get().pluginId)
        apply(libs.findPlugin("kotlin.android").get().get().pluginId)
      }

      extensions.configure<LibraryExtension> {
        configureReleaseLibraryKotlinAndroid(this)
      }

      dependencies {}
    }
  }
}
