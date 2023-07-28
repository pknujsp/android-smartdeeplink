package io.github.pknujsp.testbed.feature.compose.util

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

fun List<AStyle>.toAnnotated() = buildAnnotatedString {
  forEach { aStyle ->
    aStyle.span?.let {
      withStyle(it) { append(aStyle.text) }
    } ?: aStyle.paragraph?.let {
      withStyle(it) { append(aStyle.text) }
    } ?: append(aStyle.text)
  }
}


data class AStyle(
  val text: String,
  val paragraph: ParagraphStyle? = null,
  val span: SpanStyle? = null,
)
