package io.github.pknujsp.testbed.feature.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val myColorScheme = lightColorScheme(
  primary = Color.Blue,
  onSurface = Color.Black,
  background = Color.White,
  surface = Color.White,
)


@Composable
fun MyTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = myColorScheme) {
    content()
  }
}
