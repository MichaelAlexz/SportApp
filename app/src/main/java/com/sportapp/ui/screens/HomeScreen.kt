package com.sportapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportapp.*
import com.sportapp.data.WorkoutRecord
import com.sportapp.ui.components.*
import com.sportapp.ui.theme.*
import com.sportapp.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current

    val todaySteps by viewModel.todaySteps.collectAsState()
    val stepProgress by viewModel.todayStepProgress.collectAsState()
    val todayWorkoutDuration by viewModel.todayWorkoutDuration.collectAsState()
    val todayCalories by viewModel.todayCalories.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val activeMin by viewModel.activeMinutes.collectAsState()
    val stepCalories by viewModel.todayCaloriesFromSteps.collectAsState()
    val stepDistance by viewModel.todayDistanceFromSteps.collectAsState()

    val isTracking by viewModel.isTracking.collectAsState()
    val isTrackingPaused by viewModel.isTrackingPaused.collectAsState()
    val trackingType by viewModel.trackingType.collectAsState()
    val trackingDistance by viewModel.trackingDistance.collectAsState()
    val trackingDuration by viewModel.trackingDuration.collectAsState()
    val trackingCalories by viewModel.trackingCalories.collectAsState()
    val trackingPace by viewModel.trackingPace.collectAsState()

    val monthlyProgress by viewModel.monthlyGoalProgress.collectAsState()
    val monthlyTarget by viewModel.monthlyGoalTarget.collectAsState()
    val monthlyValue by viewModel.monthlyGoalValue.collectAsState()

    val recentWorkouts by viewModel.recentWorkouts.collectAsState()

    var showReminder by remember { mutableStateOf(true) }
    var showWorkoutComplete by remember { mutableStateOf(false) }

    val dashboardItems = remember(todaySteps, todayWorkoutDuration, todayCalories) {
        listOf(
            DashBoardItem("👣", formatNumber(todaySteps), "步",
                "距目标还差 ${(10000 - todaySteps).coerceAtLeast(0)} 步",
                stepProgress, 0xFF5BC0B8),
            DashBoardItem("⏱️", (todayWorkoutDuration / 60).toString(), "分钟",
                "今日运动时长", (todayWorkoutDuration / 3600f).coerceAtMost(1f), 0xFFFF7E5F),
            DashBoardItem("🔥", viewModel.formatCalories(todayCalories + stepCalories), "千卡",
                "消耗卡路里", ((todayCalories + stepCalories) / 500f).coerceAtMost(1f), 0xFF5B8DEF)
        )
    }

    // 5种运动类别
    val workoutCategories = listOf(
        WorkoutCategory("户外跑步", "GPS追踪", "🏃", "--", "--",
            listOf(0xFFE8F6F3, 0xFFD0F0EC)),
        WorkoutCategory("走路", "日常步行", "🚶", "--", "--",
            listOf(0xFFE8FFE8, 0xFFD0F0D0)),
        WorkoutCategory("骑行", "户外骑行", "🚴", "--", "--",
            listOf(0xFFEBF4FF, 0xFFD6E8FF)),
        WorkoutCategory("爬楼梯", "燃脂有氧", "🪜", "--", "--",
            listOf(0xFFFFF0EB, 0xFFF5E0D0)),
        WorkoutCategory("健身", "力量训练", "💪", "--", "--",
            listOf(0xFFFFF0EB, 0xFFFFE0D6))
    )

    val typeMap = mapOf(
        "户外跑步" to "running", "走路" to "walking",
        "骑行" to "cycling", "爬楼梯" to "stairs", "健身" to "fitness"
    )

    val badges = listOf(
        AchievementBadge("跑者入门", "累计10km", "🏃", todaySteps > 10000),
        AchievementBadge("燃脂达人", "消耗5000kcal", "🔥", (todayCalories + stepCalories) > 500),
        AchievementBadge("连续7天", "坚持运动", "⭐", streakDays >= 7),
        AchievementBadge("月度之星", "月跑100km", "🏆", false),
        AchievementBadge("全力以赴", "单次10km", "💯", false)
    )

    if (showReminder) {
        ReminderDialog(
            onDismiss = { showReminder = false },
            onStart = { showReminder = false; viewModel.startTracking("running", context) }
        )
    }
    if (showWorkoutComplete) {
        WorkoutCompleteDialog(
            distanceKm = trackingDistance / 1000f,
            durationMin = (trackingDuration / 60).toInt(),
            calories = trackingCalories,
            onDismiss = { showWorkoutComplete = false }
        )
    }

    Scaffold(
        containerColor = Background,
        bottomBar = { SimpleBottomBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            HeaderSection()
            SectionTitle("今日概况")
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(dashboardItems) { DashCard(it) }
            }
            SectionTitle("今日打卡", "连续打卡 $streakDays 天")
            CheckInCardRealTime(steps = todaySteps, distance = stepDistance, activeMinutes = activeMin, calories = stepCalories)

            if (isTracking) {
                TrackingPanel(
                    type = trackingType, distance = trackingDistance, duration = trackingDuration,
                    calories = trackingCalories, pace = trackingPace,
                    isPaused = isTrackingPaused,
                    onTogglePause = {
                        if (isTrackingPaused) viewModel.resumeTracking(context)
                        else viewModel.pauseTracking(context)
                    },
                    onStop = { viewModel.stopTracking(context); showWorkoutComplete = true },
                    formatDistance = { viewModel.formatDistance(it) },
                    formatDuration = { viewModel.formatDuration(it) },
                    formatCalories = { viewModel.formatCalories(it) },
                    formatPace = { viewModel.formatPace(it) }
                )
            } else {
                SectionTitle("开始运动")
                LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(workoutCategories) { cat ->
                        WorkoutCard(cat, onClick = {
                            viewModel.startTracking(typeMap[cat.name] ?: "running", context)
                        })
                    }
                }
            }

            SectionTitle("运动轨迹", if (todayWorkoutDuration > 0) "今日运动 ${viewModel.formatDuration(todayWorkoutDuration)}" else "暂无记录")
            RouteMapCard()
            SectionTitle("🎯 本月目标")
            GoalCardRealTime(progress = monthlyProgress, current = monthlyValue, target = monthlyTarget)
            SectionTitle("🏅 成就徽章", "已获得 ${badges.count { it.earned }}/${badges.size}")
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(badges) { BadgeItem(it) }
            }

            // ─── 新增：运动历史记录 ───
            SectionTitle("📋 运动记录", "共 ${recentWorkouts.size} 条")
            if (recentWorkouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("还没有运动记录，开始第一次运动吧 🏃", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                recentWorkouts.take(10).forEach { record ->
                    WorkoutHistoryItem(record, viewModel)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─── 运动历史记录卡片 ───
@Composable
fun WorkoutHistoryItem(record: WorkoutRecord, viewModel: HomeViewModel) {
    val typeEmoji = when (record.type) {
        "running" -> "🏃"; "walking" -> "🚶"; "cycling" -> "🚴"
        "stairs" -> "🪜"; "fitness" -> "💪"; else -> "🏃"
    }
    val typeName = when (record.type) {
        "running" -> "户外跑步"; "walking" -> "走路"; "cycling" -> "骑行"
        "stairs" -> "爬楼梯"; "fitness" -> "健身"; else -> record.type
    }
    val dateStr = remember(record.startTimeMs) {
        val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.CHINA)
        sdf.format(Date(record.startTimeMs))
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                .background(PrimaryLight), contentAlignment = Alignment.Center) {
                Text(typeEmoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(typeName, fontWeight = FontWeight.W700, fontSize = 14.sp, color = OnBackground)
                Text(dateStr, color = TextTertiary, fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
            if (record.distanceMeters > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                    Text(viewModel.formatDistance(record.distanceMeters), fontWeight = FontWeight.W800, fontSize = 14.sp, color = Primary)
                    Text("距离", color = TextTertiary, fontSize = 9.sp)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Text("${record.durationSeconds / 60}", fontWeight = FontWeight.W800, fontSize = 14.sp, color = OnBackground)
                Text("分钟", color = TextTertiary, fontSize = 9.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Text("${record.caloriesBurned.toInt()}", fontWeight = FontWeight.W800, fontSize = 14.sp, color = OrangeAccent)
                Text("千卡", color = TextTertiary, fontSize = 9.sp)
            }
        }
    }
}

// ─── HEADER ───
@Composable
fun HeaderSection() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("上午好 👋", color = TextSecondary, fontSize = 14.sp)
            Text("开始今天的运动", color = OnBackground, fontSize = 20.sp, fontWeight = FontWeight.W700)
        }
        Box(modifier = Modifier.size(40.dp).clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(Primary, PrimaryDark))),
            contentAlignment = Alignment.Center) {
            Text("李", color = Color.White, fontWeight = FontWeight.W700, fontSize = 16.sp)
        }
    }
}

// ─── CHECK-IN ───
@Composable
fun CheckInCardRealTime(steps: Int, distance: Float, activeMinutes: Float, calories: Float) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(modifier = Modifier.fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Primary, PrimaryDark)), RoundedCornerShape(20.dp))
            .padding(20.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("今日步数", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    Text("实时更新", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(formatNumber(steps), fontSize = 36.sp, fontWeight = FontWeight.W900, color = Color.White)
                        Text(" / 10,000", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Surface(shape = RoundedCornerShape(25.dp), color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))) {
                        Text(if (steps >= 10000) "🎉 目标达成" else "✓ 已打卡",
                            color = Color.White, fontWeight = FontWeight.W600,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontSize = 13.sp)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    CStat("🏃 距离", String.format("%.1fkm", distance / 1000f))
                    CStat("⏱ 活跃", String.format("%.1fh", activeMinutes / 60f))
                    CStat("🔥 卡路里", "${calories.toInt()}kcal")
                }
            }
        }
    }
}
@Composable private fun CStat(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        Text(" $value", color = Color.White, fontWeight = FontWeight.W700, fontSize = 14.sp)
    }
}

// ─── TRACKING PANEL（修复暂停/继续切换） ───
@Composable
fun TrackingPanel(
    type: String, distance: Float, duration: Long, calories: Float, pace: Int,
    isPaused: Boolean, onTogglePause: () -> Unit, onStop: () -> Unit,
    formatDistance: (Float) -> String, formatDuration: (Long) -> String,
    formatCalories: (Float) -> String, formatPace: (Int) -> String
) {
    val typeEmoji = when (type) { "running" -> "🏃"; "walking" -> "🚶"; "cycling" -> "🚴"; "stairs" -> "🪜"; else -> "💪" }
    val statusText = if (isPaused) "已暂停" else "运动中"
    val statusColor = if (isPaused) OrangeAccent else ErrorRed

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$typeEmoji $statusText", fontWeight = FontWeight.W700, fontSize = 16.sp, color = if (isPaused) OrangeAccent else Primary)
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
            }
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatDistance(distance), fontWeight = FontWeight.W900, fontSize = 28.sp, color = OnBackground)
                    Text("距离", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatDuration(duration), fontWeight = FontWeight.W900, fontSize = 28.sp, color = OnBackground)
                    Text("时长", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatCalories(calories), fontWeight = FontWeight.W700, fontSize = 18.sp, color = OnBackground)
                    Text("千卡", color = TextSecondary, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatPace(pace), fontWeight = FontWeight.W700, fontSize = 18.sp, color = OnBackground)
                    Text("配速", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onTogglePause, modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)) {
                    Text(if (isPaused) "继续 ▶" else "暂停 ⏸", fontWeight = FontWeight.W700)
                }
                Button(onClick = onStop, modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                    Text("结束", fontWeight = FontWeight.W700, color = Color.White)
                }
            }
        }
    }
}

// ─── GOAL CARD ───
@Composable
fun GoalCardRealTime(progress: Float, current: Float, target: Float) {
    val pct = (progress * 100).toInt()
    val sweepAngle = (progress * 360f).coerceAtMost(360f)
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val sw = 7.dp.toPx(); val r = (size.minDimension - sw) / 2
                    val tl = androidx.compose.ui.geometry.Offset((size.width - r * 2) / 2, (size.height - r * 2) / 2)
                    val ar = androidx.compose.ui.geometry.Size(r * 2, r * 2)
                    drawArc(Divider, -90f, 360f, false, tl, ar, style = Stroke(sw, cap = StrokeCap.Round))
                    drawArc(Brush.horizontalGradient(listOf(Primary, Primary.copy(0.7f))), -90f, sweepAngle, false, tl, ar, style = Stroke(sw, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$pct%", fontWeight = FontWeight.W900, fontSize = 18.sp, color = Primary)
                    Text("完成", color = TextSecondary, fontSize = 8.sp)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("本月跑步目标 ${target.toInt()}km", fontWeight = FontWeight.W700, fontSize = 15.sp, color = OnBackground)
                Text("已完成 ${String.format("%.1f", current)}km，还差 ${String.format("%.1f", (target - current).coerceAtLeast(0f))}km",
                    color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)).background(Divider)) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress.coerceAtMost(1f))
                        .clip(RoundedCornerShape(6.dp)).background(Brush.horizontalGradient(listOf(Primary, Primary.copy(0.7f)))))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0km", color = TextTertiary, fontSize = 10.sp)
                    Text("${String.format("%.1f", current)}km", color = Primary, fontWeight = FontWeight.W600, fontSize = 10.sp)
                    Text("${target.toInt()}km", color = TextTertiary, fontSize = 10.sp)
                }
            }
        }
    }
}

// ─── WORKOUT COMPLETE DIALOG ───
@Composable
fun WorkoutCompleteDialog(distanceKm: Float, durationMin: Int, calories: Float, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(28.dp), containerColor = Surface, title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(PrimaryLight), contentAlignment = Alignment.Center) { Text("🎉", fontSize = 36.sp) }
                Spacer(Modifier.height(12.dp))
                Text("运动完成！", fontWeight = FontWeight.W800, fontSize = 20.sp, color = OnBackground)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(String.format("%.1f", distanceKm), fontWeight = FontWeight.W900, fontSize = 20.sp, color = OnBackground)
                        Text("公里", color = TextSecondary, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$durationMin", fontWeight = FontWeight.W900, fontSize = 20.sp, color = OnBackground)
                        Text("分钟", color = TextSecondary, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${calories.toInt()}", fontWeight = FontWeight.W900, fontSize = 20.sp, color = OnBackground)
                        Text("千卡", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = onDismiss, shape = RoundedCornerShape(25.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth().height(48.dp), elevation = ButtonDefaults.buttonElevation(4.dp)) {
                    Text("完成", fontWeight = FontWeight.W700, fontSize = 15.sp)
                }
            }
        }, confirmButton = {}, dismissButton = {})
}

// ─── SIMPLE BOTTOM BAR ───
@Composable
fun SimpleBottomBar() {
    Surface(modifier = Modifier.fillMaxWidth().height(70.dp), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Surface.copy(alpha = 0.92f), shadowElevation = 8.dp) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("🏃 轻动 · 你的私人运动记录", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

private fun formatNumber(n: Int): String {
    return if (n >= 10000) String.format("%.1f", n / 10000f) + "万" else String.format("%,d", n)
}
