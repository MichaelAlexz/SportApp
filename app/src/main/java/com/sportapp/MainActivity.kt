package com.sportapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportapp.service.ReminderManager
import com.sportapp.service.TrackingService
import com.sportapp.ui.screens.HomeScreen
import com.sportapp.ui.theme.SportAppTheme
import com.sportapp.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var viewModel: HomeViewModel

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        Log.d(TAG, "权限请求结果: $results")
    }

    /**
     * 接收 TrackingService 的 GPS 位置广播
     */
    private val trackingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.onTrackingUpdate(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化通知渠道
        ReminderManager.createNotificationChannels(this)
        ReminderManager.scheduleDailyReminder(this)

        // 请求权限
        requestNeededPermissions()

        enableEdgeToEdge()

        setContent {
            SportAppTheme {
                val vm: HomeViewModel = viewModel()
                viewModel = vm
                HomeScreen(viewModel = vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 注册 GPS 追踪广播（全局广播）
        val filter = IntentFilter(TrackingService.BROADCAST_LOCATION)
        registerReceiver(trackingReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(trackingReceiver)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun requestNeededPermissions() {
        val permissions = mutableListOf<String>()

        // 活动识别（计步器） - Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // 位置权限（GPS 运动追踪）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // 通知权限 - Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissions.launch(permissions.toTypedArray())
        }
    }
}
