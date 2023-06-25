package io.github.pknujsp.testbed.feature.holographic

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

  private val sensorHelper = SensorHelper(context, lifecycle, supervisor)
  private val displayDensity = Resources.getSystem().displayMetrics.density

  private var firstSensorValue: SensorHelper.SensorValue? = null
  private var lastValue: ChangedValue? = null

  private val bufferCapacity = 4
  private val buffers = ArrayDeque<SensorHelper.SensorValue>(bufferCapacity)

  private val shadowShiftMinDp = -10.0 * displayDensity
  private val shadowShiftMaxDp = 10.0 * displayDensity

  private val imageShiftMaxDp = 2.0 * displayDensity

  val sensorValueChannel = channelFlow {
    sensorHelper.sensorValueChannel.receiveAsFlow().collect { newSensorValue ->
      buffers.add(newSensorValue)
      if (buffers.size < bufferCapacity) return@collect

      // average
      val averageSensorValue = SensorHelper.SensorValue(
        buffers.sumOf { it.roll } / buffers.size,
        buffers.sumOf { it.pitch } / buffers.size,
      )

      buffers.clear()

      val diffRoll = averageSensorValue.roll.floor() / 1.5
      val diffPitch = averageSensorValue.pitch.floor() / 1.5

      val shiftX = normalize(-diffRoll)
      val shiftY = normalize(diffPitch)

      val newValue = ChangedValue(
        diffPitch, diffRoll, shiftX.toInt(), shiftY.toInt(),
        (shiftX / shadowShiftMaxDp * imageShiftMaxDp).toInt(), (shiftY / shadowShiftMaxDp * imageShiftMaxDp).toInt(),
      )

      if (lastValue != newValue) {
        lastValue = newValue
        Log.d("newValue", "$newValue")
        trySend(newValue)
      }

    }
  }.flowOn(Dispatchers.Default)

  private fun normalize(value: Double): Double {
    val dp = value * displayDensity
    return if (dp >= shadowShiftMaxDp) shadowShiftMaxDp
    else if (dp <= shadowShiftMinDp) shadowShiftMinDp
    else {
      dp
    }
  }

  data class ChangedValue(
    val rotationVerticalDegree: Double,
    val rotationHorizontalDegree: Double,
    val shadowShiftX: Int = 0,
    val shadowShiftY: Int = 0,
    val imageShiftX: Int = 0,
    val imageShiftY: Int = 0,
  ) {
    override fun equals(other: Any?): Boolean =
      if (other is ChangedValue) (abs(other.rotationVerticalDegree - rotationVerticalDegree) <= 0.3) && (abs(other.rotationHorizontalDegree - rotationHorizontalDegree) <= 0.3)
      else false


    override fun hashCode(): Int {
      var result = rotationVerticalDegree.hashCode()
      result = 31 * result + rotationHorizontalDegree.hashCode()
      result = 31 * result + shadowShiftX
      result = 31 * result + shadowShiftY
      return result
    }
  }
}

private fun Double.floor() = floor(this * 100.0) / 100.0
