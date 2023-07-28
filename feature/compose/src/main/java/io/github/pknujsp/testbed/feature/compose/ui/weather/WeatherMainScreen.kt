package io.github.pknujsp.testbed.feature.compose.ui.weather

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController

@Composable
fun WeatherMainScreen() {
  val mainViewModel: WeatherMainViewModel = hiltViewModel()
  val navController = rememberNavController()
}
