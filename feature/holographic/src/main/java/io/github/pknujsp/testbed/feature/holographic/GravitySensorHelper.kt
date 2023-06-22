package io.github.pknujsp.testbed.feature.holographic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class GravitySensorHelper(
    context: Context,
    lifeCycle: Lifecycle,
    private val supervisor: Job,
) : LifecycleEventObserver {

    private val sensor: Sensor
    private val sensorManager: SensorManager

    private var scope: CoroutineScope? = null

    val gravityChannel = Channel<Gravity>(onBufferOverflow = BufferOverflow.DROP_OLDEST, capacity = 20)

    init {
        lifeCycle.addObserver(this)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            scope?.launch {
                event?.apply {
                    gravityChannel.send(Gravity(values[0], values[1], values[2]))
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d("Sensor", "onAccuracyChanged: $accuracy")
        }

    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                scope = CoroutineScope(Dispatchers.Default + Job(supervisor))
                sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
            }

            Lifecycle.Event.ON_PAUSE -> {
                scope?.cancel()
                sensorManager.unregisterListener(sensorListener)
            }

            Lifecycle.Event.ON_DESTROY -> {

            }

            else -> {}
        }
    }

    data class Gravity(val x: Float, val y: Float, val z: Float)
}
