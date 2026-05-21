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
