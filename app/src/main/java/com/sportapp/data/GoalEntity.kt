package com.sportapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户运动目标
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String = "monthly_run",   // 目标标识
    val type: String = "running",     // 运动类型
    val title: String = "本月跑步目标",
    val targetValue: Float = 100f,    // 目标值（公里）
    val currentValue: Float = 0f,     // 当前进度
    val unit: String = "km",
    val periodStartMs: Long = 0,      // 周期开始（月首）
    val periodEndMs: Long = 0         // 周期结束（月末）
)
