package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DayCheckViewModel
import com.example.ui.components.ResetConfirmDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MelonaShopScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.DayCheckTheme
import com.example.ui.theme.SageGreenCheckActive
import com.example.ui.theme.SageGreenPrimary
import com.example.ui.theme.SageGreenPrimaryContainer
import com.example.ui.theme.VividMintContainer
import com.example.ui.theme.VividMintPrimary

enum class AppTab {
    HOME,
    CALENDAR,
    SHOP,
    STATS
}

class MainActivity : ComponentActivity() {

    private val viewModel: DayCheckViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayCheckTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var currentTab by remember { mutableStateOf(AppTab.HOME) }
                var showResetDialog by remember { mutableStateOf(false) }

                BackHandler(enabled = currentTab != AppTab.HOME) {
                    currentTab = AppTab.HOME
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(
                                                1.dp,
                                                VividMintPrimary.copy(alpha = 0.25f),
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_melona_stamp),
                                            contentDescription = "메로나",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "메로나 Check!",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = VividMintPrimary,
                                        fontSize = 20.sp
                                    )
                                }
                            },
                            actions = {
                                // Melona Point balance badge leading to shop
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { currentTab = AppTab.SHOP }
                                        .testTag("appbar_melona_badge"),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, VividMintPrimary.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_melona_stamp),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "%,d개".format(uiState.currentMelonas),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = VividMintPrimary
                                        )
                                    }
                                }

                                if (uiState.isToday && uiState.completedCount > 0) {
                                    IconButton(
                                        onClick = { showResetDialog = true },
                                        modifier = Modifier.testTag("appbar_reset_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RestartAlt,
                                            contentDescription = "오늘 체크 초기화",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = (currentTab == AppTab.HOME),
                                onClick = { currentTab = AppTab.HOME },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.HOME) Icons.Filled.TaskAlt else Icons.Outlined.TaskAlt,
                                        contentDescription = "오늘의 할 일"
                                    )
                                },
                                label = { Text("오늘의 할 일", fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VividMintPrimary,
                                    selectedTextColor = VividMintPrimary,
                                    indicatorColor = VividMintContainer
                                ),
                                modifier = Modifier.testTag("nav_item_home")
                            )

                            NavigationBarItem(
                                selected = (currentTab == AppTab.CALENDAR),
                                onClick = { currentTab = AppTab.CALENDAR },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                        contentDescription = "메로나 캘린더"
                                    )
                                },
                                label = { Text("캘린더", fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VividMintPrimary,
                                    selectedTextColor = VividMintPrimary,
                                    indicatorColor = VividMintContainer
                                ),
                                modifier = Modifier.testTag("nav_item_calendar")
                            )

                            NavigationBarItem(
                                selected = (currentTab == AppTab.SHOP),
                                onClick = { currentTab = AppTab.SHOP },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.SHOP) Icons.Filled.CardGiftcard else Icons.Outlined.CardGiftcard,
                                        contentDescription = "선물 상점"
                                    )
                                },
                                label = { Text("선물 상점", fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VividMintPrimary,
                                    selectedTextColor = VividMintPrimary,
                                    indicatorColor = VividMintContainer
                                ),
                                modifier = Modifier.testTag("nav_item_shop")
                            )

                            NavigationBarItem(
                                selected = (currentTab == AppTab.STATS),
                                onClick = { currentTab = AppTab.STATS },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == AppTab.STATS) Icons.Filled.Insights else Icons.Outlined.Insights,
                                        contentDescription = "통계"
                                    )
                                },
                                label = { Text("통계", fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = VividMintPrimary,
                                    selectedTextColor = VividMintPrimary,
                                    indicatorColor = VividMintContainer
                                ),
                                modifier = Modifier.testTag("nav_item_stats")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentTab) {
                        AppTab.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                onToggleHabit = { id -> viewModel.toggleHabit(id) },
                                onResetTodayChecks = { viewModel.resetTodayChecks() },
                                onAddHabit = { title, category, emoji ->
                                    viewModel.addHabit(title, category, emoji)
                                },
                                onDeleteHabit = { id -> viewModel.deleteHabit(id) },
                                onDateSelect = { date -> viewModel.selectDate(date) },
                                onTodayClick = { viewModel.selectToday() },
                                onCategorySelect = { category -> viewModel.setFilterCategory(category) },
                                onDismissCelebration = { viewModel.dismissCelebration() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppTab.CALENDAR -> {
                            CalendarScreen(
                                uiState = uiState,
                                onDateSelected = { date -> viewModel.selectDate(date) },
                                onPrevMonth = { viewModel.previousCalendarMonth() },
                                onNextMonth = { viewModel.nextCalendarMonth() },
                                onJumpToToday = { viewModel.selectToday() },
                                onToggleHabit = { id -> viewModel.toggleHabit(id) },
                                onDeleteHabit = { id -> viewModel.deleteHabit(id) },
                                onGoToHabitsTab = { currentTab = AppTab.HOME },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppTab.SHOP -> {
                            MelonaShopScreen(
                                uiState = uiState,
                                onExchangeGift = { gift -> viewModel.exchangeGift(gift) },
                                onDismissGiftSuccess = { viewModel.dismissGiftSuccess() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppTab.STATS -> {
                            StatsScreen(
                                uiState = uiState,
                                onDateSelected = { date ->
                                    viewModel.selectDate(date)
                                    currentTab = AppTab.HOME
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }

                    if (showResetDialog) {
                        ResetConfirmDialog(
                            isToday = uiState.isToday,
                            onDismiss = { showResetDialog = false },
                            onConfirm = {
                                viewModel.resetTodayChecks()
                            }
                        )
                    }
                }
            }
        }
    }
}
