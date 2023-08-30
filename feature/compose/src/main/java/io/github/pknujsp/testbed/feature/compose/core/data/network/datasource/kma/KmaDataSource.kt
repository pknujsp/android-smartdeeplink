package io.github.pknujsp.testbed.feature.compose.core.data.network.datasource.kma

import io.github.pknujsp.testbed.feature.compose.core.data.network.datasource.kma.parser.ParsedKmaCurrentCondition
import io.github.pknujsp.testbed.feature.compose.core.data.network.datasource.kma.parser.ParsedKmaDailyForecast
import io.github.pknujsp.testbed.feature.compose.core.data.network.datasource.kma.parser.ParsedKmaHourlyForecast

interface KmaDataSource {
  suspend fun getKmaCurrentConditions(code: String): Result<ParsedKmaCurrentCondition>

  suspend fun getKmaHourlyAndDailyForecast(code: String): Result<Pair<List<ParsedKmaHourlyForecast>, List<ParsedKmaDailyForecast>>>
}
