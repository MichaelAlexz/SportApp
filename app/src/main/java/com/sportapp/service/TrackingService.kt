package com.sportapp.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.sportapp.util.CalorieCalculator
import kotlinx.coroutines.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * GPS 运动追踪服务
 * 跑步/骑行时追踪实时位置、计算距离和配速
 */
class TrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val locations = mutableListOf<Location>()
    private var totalDistanceMeters = 0f
    private var startTimeMs = 0L
    private var workoutType = "running"
    private var isPaused = false
    private var pausedDistance = 0f
    private var lastLocation: Location? = null

    companion object {
        const val TAG = "TrackingService"
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1002

        const val ACTION_START = "com.sportapp.TRACKING_START"
        const val ACTION_PAUSE = "com.sportapp.TRACKING_PAUSE"
        const val ACTION_RESUME = "com.sportapp.TRACKING_RESUME"
        const val ACTION_STOP = "com.sportapp.TRACKING_STOP"
        const val EXTRA_WORKOUT_TYPE = "extra_workout_type"

        const val BROADCAST_LOCATION = "com.sportapp.LOCATION_UPDATE"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_CALORIES = "extra_calories"
        const val EXTRA_PACE = "extra_pace"
        const val EXTRA_LOCATIONS_JSON = "extra_locations"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L  // 2秒间隔
        ).apply {
            setMinUpdateIntervalMillis(1000L)
            setMaxUpdateDelayMillis(5000L)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (isPaused) return
                for (location in result.locations) {
                    processLocation(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                workoutType = intent.getStringExtra(EXTRA_WORKOUT_TYPE) ?: "running"
                startTimeMs = System.currentTimeMillis()
                totalDistanceMeters = 0f
                locations.clear()
                lastLocation = null
                isPaused = false
                startForeground(NOTIFICATION_ID, createNotification("运动追踪中..."))
                startLocationUpdates()
            }
            ACTION_PAUSE -> {
                isPaused = true
                stopLocationUpdates()
                updateNotification("运动已暂停")
            }
            ACTION_RESUME -> {
                isPaused = false
                startLocationUpdates()
                updateNotification("运动追踪中...")
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.d(TAG, "Location updates started")
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location updates stopped")
    }

    private fun processLocation(location: Location) {
        locations.add(location)

        if (lastLocation != null && location.accuracy < 50f) {  // 过滤低精度点
            val delta = location.distanceTo(lastLocation)
            if (delta > 0 && delta < 200f) {  // 过滤异常跳点
                totalDistanceMeters += delta
            }
        }
        if (location.accuracy < 50f) {
            lastLocation = location
        }

        // 计算实时数据
        val durationSeconds = (System.currentTimeMillis() - startTimeMs) / 1000
        val avgPace = if (totalDistanceMeters > 0) {
            (durationSeconds / (totalDistanceMeters / 1000f)).roundToInt()
        } else 0
        val calories = CalorieCalculator.calculate(workoutType, durationSeconds)

        // 广播更新
        val jsonArray = JSONArray()
        locations.takeLast(50).forEach { loc ->
            val point = org.json.JSONObject().apply {
                put("lat", loc.latitude)
                put("lng", loc.longitude)
            }
            jsonArray.put(point)
        }

        val broadcastIntent = Intent(BROADCAST_LOCATION).apply {
            putExtra(EXTRA_DISTANCE, totalDistanceMeters)
            putExtra(EXTRA_DURATION, durationSeconds)
            putExtra(EXTRA_CALORIES, calories)
            putExtra(EXTRA_PACE, avgPace)
            putExtra(EXTRA_LOCATIONS_JSON, jsonArray.toString())
        }
        sendBroadcast(broadcastIntent)

        // 更新通知
        val distKm = totalDistanceMeters / 1000f
        updateNotification(String.format("%.2f km | %s", distKm, formatDuration(durationSeconds)))
    }

    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "运动追踪",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示运动追踪状态"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("轻动")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        scope.cancel()
        super.onDestroy()
    }
}
