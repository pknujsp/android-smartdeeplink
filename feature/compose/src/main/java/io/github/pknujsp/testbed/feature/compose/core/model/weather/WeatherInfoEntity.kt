package io.github.pknujsp.testbed.feature.compose.core.model.weather

import io.github.pknujsp.testbed.feature.compose.core.model.weather.current.CurrentWeatherEntity

data class WeatherInfoEntity(
  val currentWeatherEntity: CurrentWeatherEntity,
)
