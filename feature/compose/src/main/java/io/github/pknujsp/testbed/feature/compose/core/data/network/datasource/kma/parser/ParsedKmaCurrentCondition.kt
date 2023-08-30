package io.github.pknujsp.testbed.feature.compose.core.data.network.datasource.kma.parser

data class ParsedKmaCurrentCondition(
  val baseDateTimeISO8601: String = "",
  val temp: String = "",
  val yesterdayTemp: String = "",
  val feelsLikeTemp: String = "",
  val humidity: String = "",
  val windDirection: String = "",
  val windSpeed: String = "",
  val precipitationVolume: String = "",
  val pty: String = "",
)
