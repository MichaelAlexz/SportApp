package com.sportapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportapp.service.ReminderManager
import com.sportapp.ui.screens.HomeScreen
import com.sportapp.ui.theme.SportAppTheme
import com.sportapp.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: HomeViewModel

    // 权限请求
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化通知渠道
        ReminderManager.createNotificationChannels(this)

        // 调度每日运动提醒
        ReminderManager.scheduleDailyReminder(this)

        // 请求权限
        requestNeededPermissions()

        enableEdgeToEdge()

        setContent {
            SportAppTheme {
                // 使用 viewModel() 自动绑定生命周期
                val vm: HomeViewModel = viewModel()
                viewModel = vm
                HomeScreen(viewModel = vm)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun requestNeededPermissions() {
        val permissions = mutableListOf<String>()

        // 活动识别（计步器）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // 位置权限（GPS追踪）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // 通知权限（Android 13+）
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
