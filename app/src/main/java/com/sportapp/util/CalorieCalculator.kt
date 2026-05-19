package com.sportapp.util

import kotlin.math.roundToInt

/**
 * 卡路里计算器
 * 基于 MET (Metabolic Equivalent of Task) 代谢当量
 *
 * MET 值参考:
 *   跑步 8km/h    = 8.0 MET
 *   跑步 10km/h   = 10.0 MET
 *   骑行 15km/h   = 6.0 MET
 *   健身/力量训练  = 5.0 MET
 *   步行 5km/h    = 3.5 MET
 */
object CalorieCalculator {

    /**
     * 计算运动消耗的卡路里
     * 公式: kcal = MET × 体重(kg) × 时长(h)
     */
    fun calculate(
        workoutType: String,
        durationSeconds: Long,
        weightKg: Float = 65f,        // 默认体重65kg
        avgHeartRate: Int = 130
    ): Float {
        val hours = durationSeconds / 3600f
        if (hours <= 0f) return 0f

        val met = when (workoutType) {
            "running" -> {
                // 用心率估算强度: MET ≈ (HR / 40) (粗略估算)
                (avgHeartRate / 40f).coerceIn(6f, 16f)
            }
            "cycling" -> 6.0f
            "fitness" -> 5.0f
            "walking" -> 3.5f
            else -> 5.0f
        }

        return (met * weightKg * hours * 1.05f)  // 1.05为校正系数
    }

    /**
     * 根据步数估算卡路里
     * 约 1000步 ≈ 35kcal
     */
    fun calculateFromSteps(steps: Int): Float {
        return (steps * 0.035f)
    }

    /**
     * 根据步数估算距离（米）
     * 平均步幅 ≈ 身高cm × 0.45
     */
    fun estimateDistanceFromSteps(steps: Int, heightCm: Int = 170): Float {
        val stride = heightCm * 0.45f / 100f  // 米
        return steps * stride
    }

    /**
     * 格式化卡路里为整数显示
     */
    fun formatCalories(calories: Float): String {
        return calories.roundToInt().toString()
    }

    /**
     * 格式化距离
     */
    fun formatDistance(meters: Float): String {
        return if (meters >= 1000) {
            String.format("%.1f", meters / 1000f) + "km"
        } else {
            "${meters.roundToInt()}m"
        }
    }

    /**
     * 格式化配速 (秒/公里 → "4'30"")
     */
    fun formatPace(paceSeconds: Int): String {
        if (paceSeconds <= 0) return "--"
        val min = paceSeconds / 60
        val sec = paceSeconds % 60
        return "${min}'${String.format("%02d", sec)}\""
    }
}
