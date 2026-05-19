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

data class RankingUser(
    val rank: Int,
    val name: String,
    val duration: String,
    val distance: Float,
    val isMe: Boolean = false,
    val avatarColor: Long
)

data class AchievementBadge(
    val name: String,
    val subtitle: String,
    val icon: String,
    val earned: Boolean
)

data class FriendFeed(
    val name: String,
    val time: String,
    val text: String,
    val stats: List<Pair<String, String>>,
    val likes: Int,
    val comments: Int,
    val avatarColor: Long,
    val liked: Boolean = true
)

data class MapStats(
    val label: String,
    val value: String
)
