package com.sportapp.service

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*

/**
 * 每日运动提醒管理器
 * 使用 WorkManager 调度定期提醒
 */
object ReminderManager {

    private const val WORK_NAME = "daily_reminder_work"
    private const val REMINDER_HOUR = 10  // 上午10点提醒
    private const val REMINDER_MINUTE = 0
    private const val CHECK_TAG = "reminder_check"

    private const val CHANNEL_ID = "reminder_channel"
    private const val NOTIFICATION_ID = 1001
    private const val WORKOUT_CHANNEL_ID = "workout_reminder_channel"

    /**
     * 创建通知渠道
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "运动提醒",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "提醒您按时运动"
                enableVibration(true)
            }
            val manager = context.getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            // 追踪通知渠道
            val trackingChannel = android.app.NotificationChannel(
                WORKOUT_CHANNEL_ID,
                "运动追踪状态",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示运动追踪状态"
                setShowBadge(false)
            }
            manager.createNotificationChannel(trackingChannel)
        }
    }

    /**
     * 调度每日运动提醒
     */
    fun scheduleDailyReminder(context: Context) {
        val dailyRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag(CHECK_TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
            )
    }

    /**
     * 取消提醒
     */
    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * 显示立即运动通知
     */
    fun showReminderNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🏃 该运动啦！")
            .setContentText("你已经坐了很久了，起来活动一下身体吧")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

/**
 * WorkManager Worker：定时检查是否需要发送运动提醒
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        // 检查今天是否已经运动过
        // 简单实现：直接发送提醒
        ReminderManager.showReminderNotification(applicationContext)
        return Result.success()
    }
}
