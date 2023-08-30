package io.github.pknujsp.testbed.feature.compose.ui.weather.info.currentweather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import io.github.pknujsp.testbed.feature.compose.R
import io.github.pknujsp.testbed.feature.compose.core.model.weather.common.WeatherDataCategory
import io.github.pknujsp.testbed.feature.compose.ui.state.UiState
import io.github.pknujsp.testbed.feature.compose.ui.weather.info.WeatherInfoViewModel
import io.github.pknujsp.testbed.feature.compose.ui.weather.info.WeatherItemSurface
import io.github.pknujsp.testbed.feature.compose.util.AStyle
import io.github.pknujsp.testbed.feature.compose.util.toAnnotated


@Composable
fun CurrentWeatherScreen(weatherInfoViewModel: WeatherInfoViewModel) {
  val weatherInfo = weatherInfoViewModel.weatherInfo.collectAsState()

  when (weatherInfo.value) {
    is UiState.Success -> {
      val currentWeather = (weatherInfo.value as UiState.Success).data.currentWeather

      WeatherItemSurface {
        ConstraintLayout {
          val (
            weatherIcon, airQuality, temperature, weatherCondition, wind, humidity, comparedToYesterday,
            feelsLikeTemperature,
          ) = createRefs()

          Text(
            text = listOf(
              AStyle(currentWeather.tempeature.value.toInt().toString()),
              AStyle(currentWeather.tempeature.unit.symbol, span = SpanStyle(fontSize = TextUnit(24f, TextUnitType.Sp))),
            ).toAnnotated(),
            style = TextStyle(fontSize = TextUnit(50f, TextUnitType.Sp)),
            modifier = Modifier.constrainAs(temperature) {
              bottom.linkTo(parent.bottom)
              absoluteLeft.linkTo(parent.absoluteLeft)
            },
          )

          Image(
            imageVector = ImageVector.vectorResource(id = currentWeather.weatherCondition.weatherIcon),
            contentDescription = stringResource(
              id = R.string.weather_icon_description,
            ),
            modifier = Modifier
              .size(24.dp)
              .constrainAs(weatherIcon) {
                bottom.linkTo(temperature.top, margin = 8.dp)
                absoluteLeft.linkTo(parent.absoluteLeft)
              },
          )

          Text(
            currentWeather.weatherCondition.weatherCondition,
            modifier = Modifier
              .absolutePadding(left = 8.dp)
              .constrainAs(weatherCondition) {
                baseline.linkTo(weatherIcon.baseline)
                absoluteLeft.linkTo(weatherIcon.absoluteRight)
              },
            style = TextStyle(fontSize = TextUnit(20f, TextUnitType.Sp)),
          )

          Text(
            text = listOf(
              AStyle(stringResource(id = WeatherDataCategory.FEELS_LIKE_TEMPERATURE.stringId)),
              AStyle(currentWeather.feelsLikeTemperature.value.toInt().toString(), span = SpanStyle(fontSize = TextUnit(30f, TextUnitType.Sp))),
              AStyle(currentWeather.feelsLikeTemperature.unit.symbol, span = SpanStyle(fontSize = TextUnit(20f, TextUnitType.Sp))),
            ).toAnnotated(),
            modifier = Modifier.constrainAs(feelsLikeTemperature) {
              bottom.linkTo(parent.bottom)
              absoluteRight.linkTo(parent.absoluteRight)
            },
          )

          Text(
            text = "${stringResource(id = WeatherDataCategory.AIR_QUALITY_INDEX.stringId)} currentWeather.airQuality.formattedIntValue()",
            modifier = Modifier.constrainAs(airQuality) {
              bottom.linkTo(feelsLikeTemperature.top, margin = 8.dp)
              absoluteRight.linkTo(parent.absoluteRight)
            },
          )
        }
      }
    }

    is UiState.Error -> {}
    is UiState.Loading -> {}
  }
}
