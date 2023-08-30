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
import kotlinx.coroutines.launch
import kotlin.math.atan2


internal class SensorHelper(
  context: Context,
  lifeCycle: Lifecycle,
  private val supervisor: Job,
) : LifecycleEventObserver {

  private val gyroScopeSensor: Sensor
  private val accelerometerSensor: Sensor
  private val sensorManager: SensorManager

  private var scope: CoroutineScope? = null

  val sensorValueChannel = Channel<SensorValue>(onBufferOverflow = BufferOverflow.DROP_OLDEST, capacity = 600)

  init {
    lifeCycle.addObserver(this)
    sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    gyroScopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  }

  private val sensorListener = object : SensorEventListener {

    /*Sensor variables*/
    private var mGyroValues = FloatArray(3)
    private var mAccValues = FloatArray(3)
    private var mAccPitch = 0.0
    private var mAccRoll = 0.0

    /*for unsing complementary fliter*/
    private val a = 0.2f
    private val NS2S = 1.0f / 1000000000.0f
    private var pitch = 0.0
    private var roll = 0.0
    private var timestamp: Long = 0L
    private var dt = 0.0
    private var temp = 0.0

    private var gyroRunning = false
    private var accRunning = false

    override fun onSensorChanged(event: SensorEvent?) {
      scope?.launch {
        event?.also { event ->
          when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
              mGyroValues = event.values
              if (!gyroRunning) gyroRunning = true
            }

            Sensor.TYPE_ACCELEROMETER -> {
              mAccValues = event.values
              if (!accRunning) accRunning = true
            }
          }

          /**두 센서 새로운 값을 받으면 상보필터 적용 */
          if (gyroRunning && accRunning) {
            filter(event.timestamp)
            //Log.d("SensorChanged", "onSensorChanged: $pitch, $roll")
            sensorValueChannel.trySend(SensorValue(pitch, roll))
          }
        }
      }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
      Log.d("Sensor", "onAccuracyChanged: $accuracy")
    }

    private fun filter(newTimestamp: Long) {
      gyroRunning = false
      accRunning = false

      /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
      if (timestamp == 0L) {
        timestamp = newTimestamp
        return
      }
      dt = ((newTimestamp - timestamp) * NS2S).toDouble() // ns->s 변환
      timestamp = newTimestamp

      /* degree measure for accelerometer */
      mAccPitch = -atan2(mAccValues[0], mAccValues[2]) * 180.0 / Math.PI // Y 축 기준
      mAccRoll = -atan2(mAccValues[1], mAccValues[2]) * 180.0 / Math.PI // X 축 기준

      /**
       * 1st complementary filter.
       *  mGyroValuess : 각속도 성분.
       *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
       */
      temp = (1 / a) * (mAccPitch - pitch) + mGyroValues[1]
      pitch += (temp * dt)

      temp = (1 / a) * (mAccRoll - roll) + mGyroValues[0]
      roll += (temp * dt)
    }
  }


  override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    when (event) {
      Lifecycle.Event.ON_RESUME -> {
        scope = CoroutineScope(Dispatchers.Default + Job(supervisor))
        sensorManager.registerListener(sensorListener, gyroScopeSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
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

  data class SensorValue(val roll: Double, val pitch: Double)
}
