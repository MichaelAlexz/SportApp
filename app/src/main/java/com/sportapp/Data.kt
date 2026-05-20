package com.sportapp

data class DashBoardItem(
    val icon: String,
    val value: String,
    val unit: String,
    val label: String,
    val progress: Float,
    val accentColor: Long
)

data class WorkoutCategory(
    val name: String,
    val desc: String,
    val icon: String,
    val calories: String,
    val duration: String,
    val bgColors: List<Long>
)

data class AchievementBadge(
    val name: String,
    val subtitle: String,
    val icon: String,
    val earned: Boolean
)
