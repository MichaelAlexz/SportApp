package com.sportapp.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*

/**
 * 计步器前台服务
 * 使用 TYPE_STEP_COUNTER（累计步数）或 TYPE_STEP_DETECTOR（逐歩检测）传感器
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var baseSteps: Int = -1
    private var currentTotalSteps: Int = 0

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastBroadcastTime = 0L

    companion object {
        const val TAG = "StepCounterService"
        const val ACTION_STEPS_UPDATED = "com.sportapp.STEPS_UPDATED"
        const val EXTRA_STEPS = "extra_steps"
        const val EXTRA_TOTAL_STEPS = "extra_total_steps"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            // 降级到 TYPE_STEP_DETECTOR
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            Log.w(TAG, "TYPE_STEP_COUNTER not available, falling back to STEP_DETECTOR")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d(TAG, "Step counter sensor registered")
        } ?: Log.e(TAG, "No step sensor available on this device")

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        val steps = event.values[0].toInt()

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (baseSteps < 0) {
                baseSteps = steps
            }
            currentTotalSteps = steps - baseSteps
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            currentTotalSteps++
        }

        // 限频广播（每1秒广播一次）
        val now = System.currentTimeMillis()
        if (now - lastBroadcastTime >= 1000) {
            lastBroadcastTime = now
            broadcastSteps(currentTotalSteps)
        }
    }

    private fun broadcastSteps(steps: Int) {
        val intent = Intent(ACTION_STEPS_UPDATED).apply {
            putExtra(EXTRA_STEPS, steps)
            putExtra(EXTRA_TOTAL_STEPS, currentTotalSteps)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        scope.cancel()
    }
}
