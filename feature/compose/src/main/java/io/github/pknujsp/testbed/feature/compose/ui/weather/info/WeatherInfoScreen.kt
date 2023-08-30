package io.github.pknujsp.testbed.feature.compose.ui.weather.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.pknujsp.testbed.feature.compose.ui.state.onError
import io.github.pknujsp.testbed.feature.compose.ui.state.onLoading
import io.github.pknujsp.testbed.feature.compose.ui.state.onSuccess
import io.github.pknujsp.testbed.feature.compose.ui.weather.info.currentweather.CurrentWeatherScreen
import io.github.pknujsp.testbed.feature.compose.ui.weather.info.dailyforecast.DailyForecastScreen
import io.github.pknujsp.testbed.feature.compose.ui.weather.info.headinfo.HeadInfoScreen
import io.github.pknujsp.testbed.feature.compose.ui.weather.info.hourlyforecast.HourlyForecastScreen

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun WeatherInfoScreen() {
  val weatherInfoViewModel: WeatherInfoViewModel = hiltViewModel()
  val weatherInfo = weatherInfoViewModel.weatherInfo.collectAsState()

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .padding(horizontal = 16.dp),
  ) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      weatherInfo.value.onLoading {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "날씨 정보를 불러오는 중입니다", style = TextStyle(color = Color.Black))
      }.onSuccess { weatherInfo ->
        HeadInfoScreen(weatherInfoViewModel)
        ItemSpacer(60.dp)
        CurrentWeatherScreen(weatherInfoViewModel)
        ItemSpacer()
        HourlyForecastScreen(weatherInfoViewModel)
        ItemSpacer()
        DailyForecastScreen(weatherInfoViewModel)
        ItemSpacer()
      }.onError { throwable ->
        Text(text = throwable.message ?: "Error")
      }
    }
  }
}

@Composable
private fun ItemSpacer(height: Dp = 12.dp) {
  Spacer(modifier = Modifier.height(height))
}
