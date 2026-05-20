package com.sportapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 运动记录实体
 * 记录每次运动会话的完整数据
 */
@Entity(tableName = "workout_records")
data class WorkoutRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,           // "running", "cycling", "fitness"
    val startTimeMs: Long,       // 开始时间戳
    val endTimeMs: Long? = null, // 结束时间戳
    val durationSeconds: Long = 0,  // 运动时长（秒）
    val distanceMeters: Float = 0f, // 距离（米）
    val caloriesBurned: Float = 0f, // 消耗卡路里
    val avgHeartRate: Int = 0,      // 平均心率
    val maxHeartRate: Int = 0,      // 最高心率
    val avgPaceSeconds: Int = 0,    // 平均配速（秒/公里）
    val routePoints: String = "",   // GPS轨迹点（JSON序列化）
    val isCompleted: Boolean = true
) {
    companion object {
        const val TYPE_RUNNING = "running"
        const val TYPE_CYCLING = "cycling"
        const val TYPE_FITNESS = "fitness"
        const val TYPE_STAIRS = "stairs"
        const val TYPE_WALKING = "walking"
    }
}
