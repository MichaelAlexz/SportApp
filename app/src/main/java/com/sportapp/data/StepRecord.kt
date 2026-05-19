package com.sportapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日步数记录
 */
@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey
    val date: String,           // "2026-05-19"
    val totalSteps: Int = 0,
    val goalSteps: Int = 10000,
    val activeMinutes: Float = 0f,
    val distanceMeters: Float = 0f,
    val caloriesBurned: Float = 0f,
    val lastUpdatedMs: Long = System.currentTimeMillis()
)
