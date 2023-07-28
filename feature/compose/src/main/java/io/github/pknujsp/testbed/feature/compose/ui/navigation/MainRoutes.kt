package io.github.pknujsp.testbed.feature.compose.ui.navigation

sealed class MainRoutes(override val route: String) : Routes(route) {
  object Areas : MainRoutes("areas")
  object Home : MainRoutes("home")

  object Settings : MainRoutes("settings")
}
