package io.github.pknujsp.simpledialog

import android.view.ViewGroup
import androidx.annotation.IntDef


@Retention(AnnotationRetention.SOURCE)
@IntDef(value = [ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT])
annotation class SizeMode

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
/**
 * Must be ActivityContext(ActivityContext가 필요합니다)
 */
annotation class ActivityContext
