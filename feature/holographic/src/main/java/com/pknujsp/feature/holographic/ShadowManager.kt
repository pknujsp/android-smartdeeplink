package com.pknujsp.feature.holographic

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.math.abs
import kotlin.math.floor

internal class ShadowManager(
    context: Context, lifecycle: Lifecycle,
    supervisor: Job,
) {


    private val gravitySensorHelper = GravitySensorHelper(context, lifecycle, supervisor)
    private val displayDensity = Resources.getSystem().displayMetrics.density

    private var firstGravity: GravitySensorHelper.Gravity? = null
    private var lastGravity: ChangedGravity? = null

    val gravityChannel = channelFlow {
        gravitySensorHelper.gravityChannel.receiveAsFlow().collect { gravity ->
            firstGravity?.also { firstGravity ->
                val diffX = ((firstGravity.x + gravity.x.times(1000f)) % 3f).floor()
                val diffY = ((firstGravity.y + gravity.y.times(1000f)) % 3f).floor()

                val shiftX = diffX * displayDensity * 1.4f
                val shiftY = -diffY * displayDensity * 1.4f

                val newGravity = ChangedGravity(diffY, diffX, shiftX.toInt(), shiftY.toInt())

                // compare with last gravity
                if (lastGravity != newGravity) {
                    Log.d("Sensor", "onSensorChanged: $newGravity")
                    lastGravity = newGravity
                    trySend(newGravity)
                } else {
                    //Log.d("Sensor", "onSensorChanged SAME: $newGravity = $lastGravity")
                }
            } ?: run {
                firstGravity = gravity.copy(x = gravity.x.times(1000f).floor(), y = gravity.y.times(1000f).floor())
            }
        }
    }.flowOn(Dispatchers.Default)

    data class ChangedGravity(
        val rotationVerticalDegree: Float,
        val rotationHorizontalDegree: Float,
        val shiftX: Int = 0,
        val shiftY: Int = 0,
    ) {
        override fun equals(other: Any?): Boolean {
            return if (other is ChangedGravity) {
                abs(other.rotationVerticalDegree - rotationVerticalDegree) <= 0.2 ||
                        abs(other.rotationHorizontalDegree - rotationHorizontalDegree) <= 0.2
            } else {
                false
            }
        }
    }
}

private fun Float.floor() = floor(this * 10) / 10