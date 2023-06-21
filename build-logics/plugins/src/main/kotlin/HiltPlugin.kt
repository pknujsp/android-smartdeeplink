import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class HiltPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      with(pluginManager) {
        apply(libs.findPlugin("kapt").get().get().pluginId)
        apply(libs.findPlugin("hilt").get().get().pluginId)
      }

      dependencies {
        "kapt"(libs.findLibrary("androidx.hilt.compiler").get())
        "implementation"(libs.findBundle("hilt").get())
      }

      (this as org.gradle.api.plugins.ExtensionAware).extensions.configure<Any>("hilt") {
        this.javaClass.getDeclaredMethod("enableAggregatingTask", Boolean::class.java)
          .invoke(this, true)
      }
    }
  }

}
