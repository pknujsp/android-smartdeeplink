package com.pknujsp.core.model

import com.pknujsp.annotation.RequireDeepNavArgs
import com.pknujsp.deeplink.DeepNavArgs

@RequireDeepNavArgs
data class PensonInfoArgs(
    val nonNullName: String,
    val nonNullAge: Int,
    val nonNullHeight: Float,
    val nonNullWeight: Long,
    val nonNullIsMale: Boolean,
) : DeepNavArgs()