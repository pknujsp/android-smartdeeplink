package io.github.pknujsp.testbed.feature.compose.core.conveter

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherDataConverter @Inject constructor(@ApplicationContext private val context: Context) {

  fun convertTemperature(temperature: Int) = "$temperature°C"

  fun convertHumidity(humidity: Int) = "$humidity%"

  fun convertWindSpeed(windSpeed: Int) = "$windSpeed m/s"

  fun convertWindDirection(windDirection: Int) = "$windDirection°"

  fun convertAirQuality(airQuality: Int) = "$airQuality%"

  fun convertWeatherDescription(weatherCondition: String) = weatherCondition

  fun convertWeatherIcon(weatherIcon: Int) = weatherIcon

  fun convertFeelingTemperature(feelsLikeTemperature: Int) = "$feelsLikeTemperature°C"
}
