package com.sportapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportapp.*
import com.sportapp.ui.theme.*

// ───────── SECTION TITLE ─────────
@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = OnBackground,
            fontWeight = FontWeight.W700
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

// ───────── DASHBOARD CARD ─────────
@Composable
fun DashCard(item: DashBoardItem) {
    Card(
        modifier = Modifier
            .width(148.dp)
            .padding(end = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            item.accentColor == 0xFF5BC0B8 -> PrimaryLight
                            item.accentColor == 0xFFFF7E5F -> OrangeLight
                            else -> BlueLight
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = 18.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W800,
                    color = OnBackground
                )
                Text(
                    text = " ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 3.dp)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Divider)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(item.progress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(item.accentColor),
                                    Color(item.accentColor).copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
        }
    }
}

// ───────── WORKOUT CARD (NO STATS) ─────────
@Composable
fun WorkoutCard(category: WorkoutCategory, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(category.bgColors.map { Color(it) })
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.icon, fontSize = 24.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.W700,
                color = OnBackground
            )
            Text(
                category.desc,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "🔥 ${category.calories} · ⏱ ${category.duration}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

// ───────── ROUTE MAP ─────────
@Composable
fun RouteMapCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌳 滨江公园跑道", fontWeight = FontWeight.W700, fontSize = 15.sp)
                Text("2.3km · 32min", color = TextSecondary, fontSize = 12.sp)
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFE8F6F3),
                                Color(0xFFD0E8E4),
                                Color(0xFFB8DAD4),
                                Color(0xFFC8E0DC)
                            )
                        )
                    )
            ) {
                listOf(
                    0.28f to 0.42f, 0.50f to 0.55f, 0.72f to 0.38f
                ).forEachIndexed { index, (top, width) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(width)
                            .height(3.dp)
                            .align(Alignment.TopStart)
                            .offset(
                                x = if (index == 1) 40.dp else 0.dp,
                                y = (top * 140).dp
                            )
                            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight(0.50f)
                        .align(Alignment.TopStart)
                        .offset(x = 150.dp)
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight(0.55f)
                        .align(Alignment.TopStart)
                        .offset(x = 270.dp, y = 40.dp)
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                )

                Canvas(modifier = Modifier.matchParentSize()) {
                    val path = Path().apply {
                        moveTo(20.dp.toPx(), 105.dp.toPx())
                        quadraticBezierTo(70.dp.toPx(), 95.dp.toPx(), 100.dp.toPx(), 90.dp.toPx())
                        quadraticBezierTo(130.dp.toPx(), 86.dp.toPx(), 160.dp.toPx(), 78.dp.toPx())
                        quadraticBezierTo(190.dp.toPx(), 70.dp.toPx(), 220.dp.toPx(), 72.dp.toPx())
                        quadraticBezierTo(250.dp.toPx(), 74.dp.toPx(), 270.dp.toPx(), 68.dp.toPx())
                        quadraticBezierTo(290.dp.toPx(), 62.dp.toPx(), 310.dp.toPx(), 58.dp.toPx())
                    }
                    drawPath(
                        path,
                        color = Primary,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 16.dp, y = 98.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(3.dp, Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 220.dp, y = 65.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(3.dp, Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = 200.dp, y = 60.dp)
                        .clip(CircleShape)
                        .background(ErrorRed)
                        .border(3.dp, Color.White, CircleShape)
                )

                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        "📍 实时位置",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Text(
                    "⛳ 起点",
                    color = Color.Black.copy(alpha = 0.25f),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, bottom = 6.dp)
                )
                Text(
                    "🏁 终点",
                    color = Color.Black.copy(alpha = 0.25f),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                )
                Text(
                    "🚻 洗手间",
                    color = Color.Black.copy(alpha = 0.25f),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 6.dp, end = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MapStatItem("8'12\"", "平均配速")
                MapStatItem("145", "平均心率")
                MapStatItem("168", "最高心率")
                MapStatItem("32", "时长(分)")
            }
        }
    }
}

@Composable
private fun MapStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.W800, fontSize = 16.sp, color = OnBackground)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

// ───────── BADGE ─────────
@Composable
fun BadgeItem(badge: AchievementBadge) {
    Column(
        modifier = Modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    if (badge.earned) BadgeBgEarned else BadgeBgLocked
                )
                .border(
                    width = if (badge.earned) 2.dp else 2.dp,
                    color = if (badge.earned) BadgeGold else BadgeBorderLocked,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                badge.icon,
                fontSize = 28.sp,
                color = Color.Black.copy(alpha = if (badge.earned) 1f else 0.6f)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(badge.name, fontSize = 11.sp, fontWeight = FontWeight.W500, color = OnBackground)
        Text(badge.subtitle, fontSize = 9.sp, color = TextTertiary)
    }
}

// ───────── REMINDER DIALOG ─────────
@Composable
fun ReminderDialog(onDismiss: () -> Unit, onStart: () -> Unit) {
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
                Text("⏰", fontSize = 52.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "该运动啦！",
                    fontWeight = FontWeight.W800,
                    fontSize = 20.sp,
                    color = OnBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "你已经坐了很久了，\n起来活动一下身体吧 🏃",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("马上开始", fontWeight = FontWeight.W700, fontSize = 15.sp)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("稍后提醒", color = TextSecondary)
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
