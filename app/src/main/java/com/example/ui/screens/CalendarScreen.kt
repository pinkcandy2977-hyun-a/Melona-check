package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.CalendarDay
import com.example.ui.DayCheckUiState
import com.example.ui.components.HabitItemCard
import com.example.ui.theme.MelonaBackground
import com.example.ui.theme.MelonaSurfaceVariant
import com.example.ui.theme.VividMintCheckActive
import com.example.ui.theme.VividMintContainer
import com.example.ui.theme.VividMintPrimary

@Composable
fun CalendarScreen(
    uiState: DayCheckUiState,
    onDateSelected: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onToggleHabit: (Long) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onGoToHabitsTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calState = uiState.calendarState

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Melona Collection Header Card
        item {
            MelonaTrophyCard(
                year = calState.year,
                month = calState.month,
                melonaCount = calState.melonaCount
            )
        }

        // 2. Month Calendar Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_grid_card"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month Navigation Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPrevMonth,
                            modifier = Modifier.testTag("calendar_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "이전 달",
                                tint = VividMintPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${calState.year}년 ${calState.month}월",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(
                                onClick = onJumpToToday,
                                modifier = Modifier.testTag("calendar_today_button")
                            ) {
                                Text(
                                    text = "오늘",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = VividMintPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = onNextMonth,
                            modifier = Modifier.testTag("calendar_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "다음 달",
                                tint = VividMintPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Day of Week Header
                    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEachIndexed { index, day ->
                            val textColor = when (index) {
                                0 -> Color(0xFFE53935) // Sunday Red
                                6 -> Color(0xFF1E88E5) // Saturday Blue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calendar Day Grid (7 columns)
                    val days = calState.days
                    val rows = days.chunked(7)

                    rows.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { day ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CalendarDayCell(
                                        day = day,
                                        onClick = { onDateSelected(day.date) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MelonaBackground.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_melona_stamp),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "모든 할 일 완료 시 메로나 도장 찍힘!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. Selected Day Habits Status Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_selected_day_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${uiState.selectedDate} 실천 현황",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (uiState.isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = VividMintContainer
                                    ) {
                                        Text(
                                            text = "오늘",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = VividMintPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (uiState.totalCount > 0 && uiState.completedCount >= uiState.totalCount) {
                                    "🍈 축하합니다! 오늘의 메로나 도장을 획득하셨어요!"
                                } else {
                                    "${uiState.completedCount}/${uiState.totalCount} 완료 (${uiState.completionRate}%)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.totalCount > 0 && uiState.completedCount >= uiState.totalCount) {
                                    VividMintCheckActive
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (uiState.totalCount > 0 && uiState.completedCount >= uiState.totalCount) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_melona_stamp),
                                contentDescription = "메로나 달성",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { if (uiState.totalCount > 0) uiState.completedCount.toFloat() / uiState.totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = VividMintPrimary,
                        trackColor = MelonaSurfaceVariant.copy(alpha = 0.5f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // 4. Habits List for the selected date
        items(uiState.habits, key = { it.id }) { habit ->
            HabitItemCard(
                habit = habit,
                onToggle = { onToggleHabit(habit.id) },
                onDelete = { onDeleteHabit(habit.id) }
            )
        }
    }
}

@Composable
fun CalendarDayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = day.isSelected
    val isToday = day.isToday
    val isAllDone = day.isAllDone
    val isPartiallyDone = day.isPartiallyDone
    val isCurrentMonth = day.isCurrentMonth

    val cellBorder = when {
        isSelected -> Modifier.border(2.dp, VividMintPrimary, RoundedCornerShape(12.dp))
        isToday -> Modifier.border(1.5.dp, VividMintPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        else -> Modifier
    }

    val cellBackground = when {
        isAllDone -> MelonaBackground.copy(alpha = 0.65f)
        isSelected -> VividMintContainer.copy(alpha = 0.35f)
        isToday -> MelonaBackground.copy(alpha = 0.35f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cellBackground)
            .then(cellBorder)
            .clickable(onClick = onClick)
            .testTag("calendar_day_cell_${day.date}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day Number
            Text(
                text = "${day.dayOfMonth}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected || isAllDone) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    !isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    isToday -> VividMintPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Status Badge / Melona Stamp
            if (isAllDone) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_melona_stamp),
                    contentDescription = "메로나 달성 도장",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isPartiallyDone) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(VividMintPrimary)
                )
            } else {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}

@Composable
fun MelonaTrophyCard(
    year: Int,
    month: Int,
    melonaCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("melona_trophy_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MelonaBackground,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big Melona Icon Display
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.5.dp, VividMintPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_melona_stamp),
                    contentDescription = "메로나 도장",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${month}월의 메로나 컬렉션",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (melonaCount > 0) {
                        "이번 달 메로나 도장 ${melonaCount}개를 획득했어요! 🍈"
                    } else {
                        "오늘 할 일을 모두 완료하고 첫 메로나 도장을 받아보세요!"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = VividMintPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${melonaCount}개",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
