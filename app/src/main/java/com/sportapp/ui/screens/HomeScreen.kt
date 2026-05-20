package com.sportapp.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportapp.*
import com.sportapp.data.WorkoutRecord
import com.sportapp.data.SportRepository.WeeklyRanking
import com.sportapp.ui.components.*
import com.sportapp.ui.theme.*
import com.sportapp.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current

    // ViewModel 状态
    val todaySteps by viewModel.todaySteps.collectAsState()
    val stepProgress by viewModel.todayStepProgress.collectAsState()
    val todayWorkoutDuration by viewModel.todayWorkoutDuration.collectAsState()
    val todayCalories by viewModel.todayCalories.collectAsState()
    val streakDays by viewModel.streakDays.collectAsState()
    val activeMin by viewModel.activeMinutes.collectAsState()
    val stepCalories by viewModel.todayCaloriesFromSteps.collectAsState()
    val stepDistance by viewModel.todayDistanceFromSteps.collectAsState()

    // 追踪状态
    val isTracking by viewModel.isTracking.collectAsState()
    val trackingType by viewModel.trackingType.collectAsState()
    val trackingDistance by viewModel.trackingDistance.collectAsState()
    val trackingDuration by viewModel.trackingDuration.collectAsState()
    val trackingCalories by viewModel.trackingCalories.collectAsState()
    val trackingPace by viewModel.trackingPace.collectAsState()

    // 排行 & 目标
    val rankings by viewModel.weeklyRankings.collectAsState()
    val monthlyProgress by viewModel.monthlyGoalProgress.collectAsState()
    val monthlyTarget by viewModel.monthlyGoalTarget.collectAsState()
    val monthlyValue by viewModel.monthlyGoalValue.collectAsState()

    // UI 状态
    var showReminder by remember { mutableStateOf(true) }
    var showWorkoutComplete by remember { mutableStateOf(false) }

    // 动态仪表盘数据
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

    val workoutCategories = listOf(
        WorkoutCategory("户外跑步", "开始跑步", "🏃", "--", "--",
            listOf(0xFFE8F6F3, 0xFFD0F0EC)),
        WorkoutCategory("骑行", "燃烧你的卡路里", "🚴", "--", "--",
            listOf(0xFFEBF4FF, 0xFFD6E8FF)),
        WorkoutCategory("健身", "力量训练入门", "💪", "--", "--",
            listOf(0xFFFFF0EB, 0xFFFFE0D6))
    )

    val badges = listOf(
        AchievementBadge("跑者入门", "累计10km", "🏃", todaySteps > 10000),
        AchievementBadge("燃脂达人", "消耗5000kcal", "🔥", (todayCalories + stepCalories) > 500),
        AchievementBadge("连续7天", "坚持运动", "⭐", streakDays >= 7),
        AchievementBadge("月度之星", "月跑100km", "🏆", false),
        AchievementBadge("全力以赴", "单次10km", "💯", false)
    )

    val feeds = listOf(
        FriendFeed(
            "张小雅", "12 分钟前", "晨跑打卡！滨江公园的日出太美了 🌅",
            listOf("8.6" to "公里", "48" to "分钟", "386" to "千卡", "7'12\"" to "配速"),
            28, 6, 0xFFFF9A9E, true
        ),
        FriendFeed(
            "王大勇", "45 分钟前", "今天解锁了新路线！山地骑行真的太爽了 🚴‍♂️",
            listOf("15.2" to "公里", "52" to "分钟", "426" to "千卡", "120" to "心率"),
            15, 3, 0xFFA8EDEA, true
        )
    )

    // Dialog: 提醒
    if (showReminder) {
        ReminderDialog(
            onDismiss = { showReminder = false },
            onStart = {
                showReminder = false
                viewModel.startTracking("running", context)
            }
        )
    }

    // Dialog: 运动完成
    if (showWorkoutComplete) {
        WorkoutCompleteDialog(
            distanceKm = trackingDistance / 1000f,
            durationMin = (trackingDuration / 60).toInt(),
            calories = trackingCalories,
            onDismiss = { showWorkoutComplete = false },
            onShare = { showWorkoutComplete = false }
        )
    }

    Scaffold(
        containerColor = Background,
        bottomBar = { BottomNavBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // ─── HEADER ───
            HeaderSection()

            // ─── DASHBOARD ───
            SectionTitle("今日概况", "查看详情 ›")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dashboardItems) { item ->
                    DashCard(item)
                }
            }

            // ─── CHECK-IN ───
            SectionTitle("今日打卡", "连续打卡 $streakDays 天 ›")
            CheckInCardRealTime(
                steps = todaySteps,
                distance = stepDistance,
                activeMinutes = activeMin,
                calories = stepCalories
            )

            // ─── WORKOUT / TRACKING ───
            if (isTracking) {
                // 运动中：显示追踪面板
                TrackingPanel(
                    type = trackingType,
                    distance = trackingDistance,
                    duration = trackingDuration,
                    calories = trackingCalories,
                    pace = trackingPace,
                    onPause = { viewModel.pauseTracking(context) },
                    onStop = {
                        viewModel.stopTracking(context)
                        showWorkoutComplete = true
                    },
                    formatDistance = { viewModel.formatDistance(it) },
                    formatDuration = { viewModel.formatDuration(it) },
                    formatCalories = { viewModel.formatCalories(it) },
                    formatPace = { viewModel.formatPace(it) }
                )
            } else {
                // 未运动中：显示运动分类入口
                SectionTitle("开始运动", "全部 ›")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(workoutCategories) { cat ->
                        WorkoutCard(cat, onClick = {
                            viewModel.startTracking(
                                when (cat.name) {
                                    "户外跑步" -> "running"
                                    "骑行" -> "cycling"
                                    else -> "fitness"
                                }, context
                            )
                        })
                    }
                }
            }

            // ─── ROUTE MAP ───
            SectionTitle("运动轨迹", if (todayWorkoutDuration > 0) "今日运动 ${viewModel.formatDuration(todayWorkoutDuration)} ›" else "暂无记录 ›")
            RouteMapCard()

            // ─── WEEKLY RANKING ───
            SectionTitle("🏆 周运动排行", "排行榜 ›")
            RankingCard(rankings)

            // ─── GOAL PROGRESS ───
            SectionTitle("🎯 本月目标", "调整目标 ›")
            GoalCardRealTime(
                progress = monthlyProgress,
                current = monthlyValue,
                target = monthlyTarget
            )

            // ─── ACHIEVEMENT BADGES ───
            SectionTitle("🏅 成就徽章", "全部 ${badges.count { it.earned }}/${badges.size} ›")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(badges) { badge ->
                    BadgeItem(badge)
                }
            }

            // ─── FRIEND FEED ───
            SectionTitle("👥 好友动态", "更多 ›")
            feeds.forEach { feed ->
                FeedItem(feed)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─── HEADER ───
@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("上午好 👋", color = TextSecondary, fontSize = 14.sp)
            Text(
                "开始今天的运动",
                color = OnBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.W700
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box {
                Text("\uD83D\uDD14", fontSize = 22.sp)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(ErrorRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text("3", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.W700)
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Primary, PrimaryDark))),
                contentAlignment = Alignment.Center
            ) {
                Text("李", color = Color.White, fontWeight = FontWeight.W700, fontSize = 16.sp)
            }
        }
    }
}

// ─── CHECK-IN (REAL TIME) ───
@Composable
fun CheckInCardRealTime(
    steps: Int,
    distance: Float,
    activeMinutes: Float,
    calories: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Primary, PrimaryDark)),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("今日步数", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    Text("实时更新", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            formatNumber(steps),
                            fontSize = 36.sp, fontWeight = FontWeight.W900, color = Color.White
                        )
                        Text(
                            " / 10,000",
                            fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(25.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Text(
                            if (steps >= 10000) "🎉 目标达成" else "✓ 已打卡",
                            color = Color.White, fontWeight = FontWeight.W600,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            fontSize = 13.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    CheckInStat("🏃 距离", String.format("%.1fkm", distance / 1000f))
                    CheckInStat("⏱ 活跃", String.format("%.1fh", activeMinutes / 60f))
                    CheckInStat("🔥 卡路里", "${calories.toInt()}kcal")
                }
            }
        }
    }
}

@Composable
private fun CheckInStat(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        Text(" $value", color = Color.White, fontWeight = FontWeight.W700, fontSize = 14.sp)
    }
}

// ─── TRACKING PANEL ───
@Composable
fun TrackingPanel(
    type: String,
    distance: Float,
    duration: Long,
    calories: Float,
    pace: Int,
    onPause: () -> Unit,
    onStop: () -> Unit,
    formatDistance: (Float) -> String,
    formatDuration: (Long) -> String,
    formatCalories: (Float) -> String,
    formatPace: (Int) -> String
) {
    val typeEmoji = when (type) {
        "running" -> "🏃"
        "cycling" -> "🚴"
        else -> "💪"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$typeEmoji 运动中", fontWeight = FontWeight.W700, fontSize = 16.sp, color = Primary)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ErrorRed)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Big stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatDistance(distance), fontWeight = FontWeight.W900,
                        fontSize = 28.sp, color = OnBackground)
                    Text("距离", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatDuration(duration), fontWeight = FontWeight.W900,
                        fontSize = 28.sp, color = OnBackground)
                    Text("时长", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatCalories(calories), fontWeight = FontWeight.W700,
                        fontSize = 18.sp, color = OnBackground)
                    Text("千卡", color = TextSecondary, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatPace(pace), fontWeight = FontWeight.W700,
                        fontSize = 18.sp, color = OnBackground)
                    Text("配速", color = TextSecondary, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onPause,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("暂停", fontWeight = FontWeight.W700)
                }
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {
                    Text("结束", fontWeight = FontWeight.W700, color = Color.White)
                }
            }
        }
    }
}

// ─── RANKING CARD (REAL TIME) ───
@Composable
fun RankingCard(rankings: List<WeeklyRanking>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            rankings.forEachIndexed { index, user ->
                RankingItemDynamic(user = user, rank = index + 1)
                if (index < rankings.lastIndex) {
                    Divider(color = Divider, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun RankingItemDynamic(user: WeeklyRanking, rank: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            when (rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "$rank"
            },
            fontWeight = FontWeight.W800, fontSize = 14.sp,
            color = when (rank) {
                1 -> Gold; 2 -> Silver; 3 -> Bronze
                else -> TextSecondary
            },
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .size(36.dp).clip(CircleShape)
                .background(Color(user.avatarColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(user.name.take(1), color = Color.White, fontWeight = FontWeight.W700, fontSize = 13.sp)
        }

        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.name, fontWeight = FontWeight.W600, fontSize = 14.sp, color = OnBackground)
                if (user.isMe) {
                    Text("（你）", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.W500)
                }
            }
            Text("已连续运动 ${user.streakDays} 天", color = TextSecondary, fontSize = 11.sp)
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                String.format("%.1f", user.distanceKm),
                fontWeight = FontWeight.W800, fontSize = 15.sp, color = Primary
            )
            Text(" km", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// ─── GOAL CARD (REAL TIME) ───
@Composable
fun GoalCardRealTime(progress: Float, current: Float, target: Float) {
    val pct = (progress * 100).toInt()
    val sweepAngle = (progress * 360f).coerceAtMost(360f)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 7.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = androidx.compose.ui.geometry.Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    )
                    val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)

                    drawArc(color = Divider, startAngle = -90f, sweepAngle = 360f,
                        useCenter = false, topLeft = topLeft, size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                    drawArc(
                        brush = Brush.horizontalGradient(listOf(Primary, Primary.copy(alpha = 0.7f))),
                        startAngle = -90f, sweepAngle = sweepAngle,
                        useCenter = false, topLeft = topLeft, size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$pct%", fontWeight = FontWeight.W900, fontSize = 18.sp, color = Primary)
                    Text("完成", color = TextSecondary, fontSize = 8.sp)
                }
            }

            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("本月跑步目标 ${target.toInt()}km", fontWeight = FontWeight.W700,
                    fontSize = 15.sp, color = OnBackground)
                Text(
                    "已完成 ${String.format("%.1f", current)}km，还差 ${String.format("%.1f", (target - current).coerceAtLeast(0f))}km",
                    color = TextSecondary, fontSize = 12.sp
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                        .clip(RoundedCornerShape(6.dp)).background(Divider)
                ) {
                    Box(
                        modifier = Modifier.fillMaxHeight()
                            .fillMaxWidth(progress.coerceAtMost(1f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(Primary, Primary.copy(alpha = 0.7f))))
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0km", color = TextTertiary, fontSize = 10.sp)
                    Text(
                        "${String.format("%.1f", current)}km",
                        color = Primary, fontWeight = FontWeight.W600, fontSize = 10.sp
                    )
                    Text("${target.toInt()}km", color = TextTertiary, fontSize = 10.sp)
                }
            }
        }
    }
}

// ─── WORKOUT COMPLETE DIALOG ───
@Composable
fun WorkoutCompleteDialog(
    distanceKm: Float,
    durationMin: Int,
    calories: Float,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Surface,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(PrimaryLight),
                    contentAlignment = Alignment.Center
                ) { Text("🎉", fontSize = 36.sp) }
                Spacer(Modifier.height(12.dp))
                Text("运动完成！", fontWeight = FontWeight.W800, fontSize = 20.sp, color = OnBackground)
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp), color = PrimaryLight
                ) {
                    Text("今日份汗水已达标，继续加油 💪",
                        color = Primary, fontWeight = FontWeight.W600, fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(String.format("%.1f", distanceKm),
                            fontWeight = FontWeight.W900, fontSize = 20.sp, color = OnBackground)
                        Text("公里", color = TextSecondary, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$durationMin", fontWeight = FontWeight.W900,
                            fontSize = 20.sp, color = OnBackground)
                        Text("分钟", color = TextSecondary, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${calories.toInt()}", fontWeight = FontWeight.W900,
                            fontSize = 20.sp, color = OnBackground)
                        Text("千卡", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onShare,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) { Text("分享打卡", fontWeight = FontWeight.W700, fontSize = 15.sp) }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("完成", color = TextSecondary) }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

// ─── 格式化 ───
private fun formatNumber(n: Int): String {
    return if (n >= 10000) {
        String.format("%.1f", n / 10000f) + "万"
    } else {
        String.format("%,d", n)
    }
}


