package com.pknujsp.core.model

import com.pknujsp.annotation.RequireDeepNavArgs

@RequireDeepNavArgs
data class PersonInfoArgs(
    val name: String,
    val age: Int,
    val height: Float,
    val weight: Long,
    val weights: Long,
    val isMale: Boolean,
)