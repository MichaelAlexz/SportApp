package com.sportapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sportapp.util.CalorieCalculator
import kotlinx.coroutines.*
import kotlin.math.roundToInt

/**
 * GPS 运动追踪服务（使用 Android 原生 LocationManager，不依赖 Google Play Services）
 */
class TrackingService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var totalDistanceMeters = 0f
    private var startTimeMs = 0L
    private var workoutType = "running"
    private var isPaused = false
    private var lastLocation: Location? = null
    private var timerJob: Job? = null
    private var elapsedSeconds = 0L
    // 实时轨迹点（归一化坐标，用于UI绘制）
    private val routePoints = mutableListOf<Pair<Float, Float>>()
    private var routeBounds = floatArrayOf(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE) // minLat, minLng, maxLat, maxLng

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
        const val EXTRA_ROUTE = "extra_route"
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                workoutType = intent.getStringExtra(EXTRA_WORKOUT_TYPE) ?: "running"
                startTimeMs = System.currentTimeMillis()
                totalDistanceMeters = 0f
                elapsedSeconds = 0L
                lastLocation = null
                isPaused = false
                startForeground(NOTIFICATION_ID, createNotification("运动中..."))
                startLocationUpdates()
                startTimer()
            }
            ACTION_PAUSE -> {
                isPaused = true
                stopLocationUpdates()
                timerJob?.cancel()
                updateNotification("已暂停")
            }
            ACTION_RESUME -> {
                isPaused = false
                startLocationUpdates()
                startTimer()
                updateNotification("运动中...")
            }
            ACTION_STOP -> {
                stopLocationUpdates()
                timerJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startLocationUpdates() {
        try {
            // 优先使用 GPS，其次网络定位
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER
            )

            for (provider in providers) {
                try {
                    locationManager.requestLocationUpdates(
                        provider,
                        2000L,  // 2秒更新一次
                        1f,     // 最小距离变化1米
                        this
                    )
                    Log.d(TAG, "已注册定位: $provider")
                } catch (e: SecurityException) {
                    Log.w(TAG, "无定位权限: $provider")
                } catch (e: Exception) {
                    Log.w(TAG, "定位不可用: $provider - ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "启动定位失败", e)
        }
    }

    private fun stopLocationUpdates() {
        try {
            locationManager.removeUpdates(this)
        } catch (_: Exception) {}
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                if (!isPaused) {
                    elapsedSeconds++
                    broadcastUpdate()
                }
            }
        }
    }

    // ─── LocationListener 回调 ───

    override fun onLocationChanged(location: Location) {
        if (isPaused) return

        // 过滤低精度定位
        if (location.accuracy > 100f) return

        val prev = lastLocation
        if (prev != null) {
            val delta = location.distanceTo(prev)
            if (delta > 0 && delta < 300f) {  // 过滤异常跳点
                totalDistanceMeters += delta
            }
        }
        lastLocation = location

        // 记录轨迹点（归一化到0-1范围）
        val lat = location.latitude.toFloat()
        val lng = location.longitude.toFloat()
        if (lat < routeBounds[0]) routeBounds[0] = lat
        if (lng < routeBounds[1]) routeBounds[1] = lng
        if (lat > routeBounds[2]) routeBounds[2] = lat
        if (lng > routeBounds[3]) routeBounds[3] = lng
        // 归一化坐标加入列表
        val normLat = (lat - routeBounds[0]) / (routeBounds[2] - routeBounds[0] + 0.0001f)
        val normLng = (lng - routeBounds[1]) / (routeBounds[3] - routeBounds[1] + 0.0001f)
        routePoints.add(Pair(normLat, normLng))
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun broadcastUpdate() {
        val avgPace = if (totalDistanceMeters > 0) {
            (elapsedSeconds / (totalDistanceMeters / 1000f)).roundToInt()
        } else 0
        val calories = CalorieCalculator.calculate(workoutType, elapsedSeconds)

        // 序列化轨迹点：lat1,lng1|lat2,lng2|...
        val routeStr = routePoints.joinToString("|") { "${it.first},${it.second}" }

        val intent = Intent(BROADCAST_LOCATION).apply {
            putExtra(EXTRA_DISTANCE, totalDistanceMeters)
            putExtra(EXTRA_DURATION, elapsedSeconds)
            putExtra(EXTRA_CALORIES, calories)
            putExtra(EXTRA_PACE, avgPace)
            putExtra(EXTRA_ROUTE, routeStr)
        }
        sendBroadcast(intent)
    }

    // ─── 通知 ───

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "运动追踪",
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
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
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(content))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        timerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}
