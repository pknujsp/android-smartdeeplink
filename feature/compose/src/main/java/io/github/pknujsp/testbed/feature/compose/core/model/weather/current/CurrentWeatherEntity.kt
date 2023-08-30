package io.github.pknujsp.testbed.feature.compose.core.model.weather.current

import androidx.annotation.DrawableRes
import io.github.pknujsp.testbed.feature.compose.core.model.weather.common.WeatherValueType

data class CurrentWeatherEntity(
  val weatherCondition: String,
  @DrawableRes val weatherIcon: Int,
  val tempeature: WeatherValueType,
  val feelsLikeTemperature: WeatherValueType,
  val humidity: WeatherValueType,
  val windSpeed: WeatherValueType,
  val windDirection: WeatherValueType,
  val airQuality: WeatherValueType,
)
