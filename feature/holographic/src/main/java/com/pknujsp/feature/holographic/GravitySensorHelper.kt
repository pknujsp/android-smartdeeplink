package com.pknujsp.feature.holographic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class GravitySensorHelper(
    context: Context,
    lifeCycle: Lifecycle,
    private val gravitySensorListener: GravitySensorListener
) : LifecycleEventObserver {

    private val gravitySensor: Sensor
    private val sensorManager: SensorManager


    init {
        lifeCycle.addObserver(this)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.apply {
                val x = values[0]
                val y = values[1]
                val z = values[2]

                gravitySensorListener.onSensorChanged(x, y, z)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                sensorManager.registerListener(sensorListener, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL)
            }

            Lifecycle.Event.ON_PAUSE -> {
                sensorManager.unregisterListener(sensorListener)
            }

            else -> {}
        }
    }

    fun interface GravitySensorListener {
        fun onSensorChanged(x: Float, y: Float, z: Float)
    }

}