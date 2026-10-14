package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DayCheckUiState
import com.example.ui.components.AddHabitDialog
import com.example.ui.components.DateSelectorRow
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.HabitItemCard
import com.example.ui.components.ResetConfirmDialog
import com.example.ui.components.TodaySummaryCard
import com.example.ui.theme.SageGreenCheckActive
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer

@Composable
fun HomeScreen(
    uiState: DayCheckUiState,
    onToggleHabit: (Long) -> Unit,
    onResetTodayChecks: () -> Unit,
    onAddHabit: (String, String, String) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onDateSelect: (String) -> Unit,
    onTodayClick: () -> Unit,
    onCategorySelect: (String) -> Unit,
    onDismissCelebration: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Pair<Long, String>?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_habit_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Today Summary Card with circular progress
            item {
                TodaySummaryCard(
                    completionRate = uiState.completionRate,
                    completedCount = uiState.completedCount,
                    totalCount = uiState.totalCount,
                    streakDays = uiState.statsSummary.currentStreak,
                    isToday = uiState.isToday,
                    selectedDateDisplay = uiState.selectedDate,
                    onResetTodayClick = { showResetDialog = true }
                )
            }

            // 2. Horizontal Date Selector Row
            item {
                DateSelectorRow(
                    dateList = uiState.dateStrip,
                    isTodaySelected = uiState.isToday,
                    onDateSelect = onDateSelect,
                    onTodayClick = onTodayClick
                )
            }

            // 3. Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_filter_row")
                ) {
                    items(uiState.categories) { category ->
                        val isSelected = (uiState.selectedCategory == category)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelect(category) },
                            label = {
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_chip_$category")
                        )
                    }
                }
            }

            // 4. Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (uiState.isToday) "오늘의 할 일" else "${uiState.selectedDate} 할 일 목록",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = SageGreenPrimaryContainer
                        ) {
                            Text(
                                text = "${uiState.habits.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SageGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (uiState.isToday && uiState.completedCount > 0) {
                        Text(
                            text = "체크 해제 시 기록 취소 가능",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 5. Habits List or Empty State
            if (uiState.habits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("empty_state_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🌱", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "등록된 할 일이 없습니다",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "아래 ‘+ 할 일 추가’ 버튼을 눌러\n오늘 해야 할 일을 등록해보세요!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(uiState.habits, key = { it.id }) { habit ->
                    HabitItemCard(
                        habit = habit,
                        onToggle = { onToggleHabit(habit.id) },
                        onDelete = { habitToDelete = Pair(habit.id, habit.title) }
                    )
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_habit"),
            containerColor = SageGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "할 일 추가"
                )
            },
            text = {
                Text(
                    text = "할 일 추가",
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }

    // Dialogs
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, emoji ->
                onAddHabit(title, category, emoji)
            }
        )
    }

    if (showResetDialog) {
        ResetConfirmDialog(
            isToday = uiState.isToday,
            onDismiss = { showResetDialog = false },
            onConfirm = {
                onResetTodayChecks()
            }
        )
    }

    habitToDelete?.let { (id, title) ->
        DeleteConfirmDialog(
            habitTitle = title,
            onDismiss = { habitToDelete = null },
            onConfirm = {
                onDeleteHabit(id)
                habitToDelete = null
            }
        )
    }

    // 100% Achievement Celebration Modal
    if (uiState.showCelebration) {
        AlertDialog(
            onDismissRequest = onDismissCelebration,
            icon = {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = SageGreenCheckActive,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "🎉 오늘 목표 100% 달성!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "오늘 계획한 모든 할 일을 완료하셨습니다.\n성실하고 부지런한 당신, 정말 멋져요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissCelebration,
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("button_dismiss_celebration")
                ) {
                    Text("고마워요! 계속 힘내기", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
