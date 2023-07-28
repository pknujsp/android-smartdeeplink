package io.github.pknujsp.testbed.feature.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.pknujsp.testbed.feature.compose.theme.MyTheme
import io.github.pknujsp.testbed.feature.compose.theme.myColorScheme

@AndroidEntryPoint
class ComposeActivity : ComponentActivity() {

  private val viewModel: ActivityViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      MyTheme {
        val navController = rememberNavController()
        Surface(modifier = Modifier.fillMaxSize(), color = myColorScheme.background) {
          MainScreen()
        }
      }
    }
  }
}
