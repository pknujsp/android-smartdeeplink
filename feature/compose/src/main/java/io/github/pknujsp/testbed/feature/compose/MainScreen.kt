package io.github.pknujsp.testbed.feature.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.pknujsp.testbed.feature.compose.ui.areas.AreasScreen
import io.github.pknujsp.testbed.feature.compose.ui.home.HomeScreen
import io.github.pknujsp.testbed.feature.compose.ui.navigation.MainRoutes
import io.github.pknujsp.testbed.feature.compose.ui.settings.SettingsScreen


@Preview
@Composable
fun MainScreen() {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = MainRoutes.Home.route) {
    composable(MainRoutes.Home.route) {
      HomeScreen()
    }
    composable(MainRoutes.Areas.route) {
      AreasScreen()
    }
    composable(MainRoutes.Settings.route) {
      SettingsScreen()
    }
  }
}
