package com.pknujsp.core.model

import com.pknujsp.annotation.WapNavArgs

@WapNavArgs
data class PersonInfoArgs(
    val name: String,
    val age: Int,
    val height: Float,
    val isMale: Boolean,
)