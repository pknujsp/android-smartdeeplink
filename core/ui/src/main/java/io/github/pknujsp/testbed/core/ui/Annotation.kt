package io.github.pknujsp.testbed.core.ui

import android.view.ViewGroup
import androidx.annotation.IntDef


@Retention(AnnotationRetention.SOURCE)
@IntDef(value = [ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT])
annotation class SizeMode
