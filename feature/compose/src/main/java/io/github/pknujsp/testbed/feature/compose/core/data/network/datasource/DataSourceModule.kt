package io.github.pknujsp.testbed.feature.compose.core.data.network.datasource

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.pknujsp.testbed.feature.compose.core.data.network.api.kma.KmaNetworkApi
import io.github.pknujsp.testbed.feature.compose.core.data.network.datasource.kma.KmaDataSource
import io.github.pknujsp.testbed.feature.compose.core.data.network.datasource.kma.KmaDataSourceImpl
import io.github.pknujsp.testbed.feature.compose.core.data.network.datasource.kma.parser.KmaHtmlParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

  @Provides
  @Singleton
  fun providesKmaDataSource(
    kmaNetworkApi: KmaNetworkApi,
    kmaHtmlParser: KmaHtmlParser,
  ): KmaDataSource = KmaDataSourceImpl(kmaNetworkApi, kmaHtmlParser)
}
