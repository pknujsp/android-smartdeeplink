package com.pknujsp.feature.holographic

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import kotlin.math.abs

class ShadowManager(
    context: Context, lifecycle: Lifecycle, private
    val diffDistanceListener: DiffDistanceListener
) : GravitySensorHelper.GravitySensorListener {

    fun interface DiffDistanceListener {
        fun onDiffDistance(x: Float, y: Float, rotX: Float, rotY: Float)
    }

    private val gravitySensorHelper = GravitySensorHelper(context, lifecycle, this)

    private var firstGravity: Triple<Float, Float, Float>? = null

    override fun onSensorChanged(x: Float, y: Float, z: Float) {
        if (firstGravity == null)
            firstGravity = Triple(x, y, z)
        else {
            /*
            x > 0 -> 화면이 왼쪽 방향으로 가도록 기울임
            x < 0 -> 화면이 오른쪽 방향으로 가도록 기울임

            y > 0 -> 화면이 직진 방향으로 가도록 기울임
            y < 0 -> 화면이 내 쪽 방향으로 가도록 기울임
             */

            val diffX = (firstGravity!!.first + x) % 2f
            val diffY = (firstGravity!!.second + y) % 2f

            Log.d("Sensor",
                "origin : (${firstGravity!!.first}, ${firstGravity!!.second}) -> " +
                        "current: ($x, $y) -> diff: ($diffX, $diffY)")
            diffDistanceListener.onDiffDistance(diffY, diffX, diffY, diffX)
        }
    }


}