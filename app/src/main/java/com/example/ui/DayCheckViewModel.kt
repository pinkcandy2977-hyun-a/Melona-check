package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DayCheckDatabase
import com.example.data.local.HabitCompletionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.RedeemedGiftEntity
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HabitUiModel(
    val id: Long,
    val title: String,
    val category: String,
    val iconEmoji: String,
    val isCompleted: Boolean,
    val sortOrder: Int
)

data class DaySummary(
    val date: String,
    val dayOfWeek: String,
    val dayNumber: String,
    val completionRate: Int,
    val completedCount: Int,
    val totalCount: Int,
    val isToday: Boolean,
    val isSelected: Boolean
)

data class CalendarDay(
    val date: String, // "yyyy-MM-dd"
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val totalCount: Int,
    val completedCount: Int,
    val isAllDone: Boolean, // True when all tasks are completed on this day (Melona stamp!)
    val isPartiallyDone: Boolean
)

data class CalendarMonthState(
    val year: Int,
    val month: Int, // 1 - 12
    val days: List<CalendarDay> = emptyList(),
    val melonaCount: Int = 0 // Count of days with all tasks completed in this month
)

data class StatsSummary(
    val totalCompletedCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val weeklyAverageRate: Int = 0,
    val perfectDaysCount: Int = 0,
    val recentDays: List<DaySummary> = emptyList()
)

data class DayCheckUiState(
    val selectedDate: String = "",
    val todayDate: String = "",
    val habits: List<HabitUiModel> = emptyList(),
    val categories: List<String> = listOf("전체", "건강", "운동", "성장", "일상"),
    val selectedCategory: String = "전체",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val completionRate: Int = 0, // 0 - 100
    val isToday: Boolean = true,
    val dateStrip: List<DaySummary> = emptyList(),
    val calendarState: CalendarMonthState = CalendarMonthState(year = 2026, month = 10),
    val statsSummary: StatsSummary = StatsSummary(),
    val showCelebration: Boolean = false,
    // Melona Reward & Gift Shop State (1원 = 1메로나 시스템)
    val currentMelonas: Int = 1000,
    val totalEarnedMelonas: Int = 1000,
    val redeemedGifts: List<RedeemedGiftEntity> = emptyList(),
    val lastExchangedGift: RedeemedGiftEntity? = null
)

class DayCheckViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    private val dayOfWeekFormat = SimpleDateFormat("E", Locale.KOREA)
    private val dayNumberFormat = SimpleDateFormat("d", Locale.KOREA)

    private val todayString: String
        get() = dateFormat.format(Date())

    private val initialCalendar = Calendar.getInstance()
    private val _selectedDate = MutableStateFlow(todayString)
    private val _selectedCategory = MutableStateFlow("전체")
    private val _showCelebration = MutableStateFlow(false)
    private val _calendarYear = MutableStateFlow(initialCalendar.get(Calendar.YEAR))
    private val _calendarMonth = MutableStateFlow(initialCalendar.get(Calendar.MONTH) + 1) // 1 to 12
    private val _lastExchangedGift = MutableStateFlow<RedeemedGiftEntity?>(null)

    init {
        val database = DayCheckDatabase.getInstance(application)
        repository = HabitRepository(database.habitDao())

        viewModelScope.launch {
            repository.ensureStarterHabitsIfEmpty()
        }
    }

    val uiState: StateFlow<DayCheckUiState> = combine(
        repository.allHabits,
        repository.allCompletions,
        repository.allRedeemedGifts,
        _selectedDate,
        _selectedCategory,
        _showCelebration,
        _calendarYear,
        _calendarMonth,
        _lastExchangedGift
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val habits = args[0] as List<HabitEntity>
        @Suppress("UNCHECKED_CAST")
        val completions = args[1] as List<HabitCompletionEntity>
        @Suppress("UNCHECKED_CAST")
        val redeemedGifts = args[2] as List<RedeemedGiftEntity>
        val selectedDate = args[3] as String
        val selectedCategory = args[4] as String
        val showCelebration = args[5] as Boolean
        val calYear = args[6] as Int
        val calMonth = args[7] as Int
        val lastExchanged = args[8] as? RedeemedGiftEntity

        buildUiState(
            habits = habits,
            completions = completions,
            redeemedGifts = redeemedGifts,
            selectedDate = selectedDate,
            selectedCategory = selectedCategory,
            showCelebration = showCelebration,
            calYear = calYear,
            calMonth = calMonth,
            lastExchanged = lastExchanged
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DayCheckUiState(
            selectedDate = todayString,
            todayDate = todayString
        )
    )

    private fun buildUiState(
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
        redeemedGifts: List<RedeemedGiftEntity>,
        selectedDate: String,
        selectedCategory: String,
        showCelebration: Boolean,
        calYear: Int,
        calMonth: Int,
        lastExchanged: RedeemedGiftEntity?
    ): DayCheckUiState {
        val today = todayString
        val completionsByDate = completions.groupBy { it.date }
        val currentDayCompletions = completionsByDate[selectedDate]?.map { it.habitId }?.toSet() ?: emptySet()

        val allHabitUiModels = habits.map { habit ->
            HabitUiModel(
                id = habit.id,
                title = habit.title,
                category = habit.category,
                iconEmoji = habit.iconEmoji,
                isCompleted = currentDayCompletions.contains(habit.id),
                sortOrder = habit.sortOrder
            )
        }

        val filteredHabits = if (selectedCategory == "전체") {
            allHabitUiModels
        } else {
            allHabitUiModels.filter { it.category == selectedCategory }
        }

        val totalHabitsCount = allHabitUiModels.size
        val completedHabitsCount = allHabitUiModels.count { it.isCompleted }
        val completionRate = if (totalHabitsCount > 0) {
            ((completedHabitsCount.toFloat() / totalHabitsCount) * 100).toInt()
        } else 0

        // Build 10-day date strip
        val dateStrip = buildDateStrip(habits, completionsByDate, selectedDate, today)

        // Build monthly calendar grid with Melona completion stamps
        val calendarState = buildCalendarMonth(
            year = calYear,
            month = calMonth,
            habits = habits,
            completionsByDate = completionsByDate,
            selectedDate = selectedDate,
            today = today
        )

        // Calculate statistics
        val statsSummary = calculateStats(habits, completionsByDate, today)

        // Melona Reward Balance calculation (1원 = 1메로나 시스템)
        // 1 completed task = 100 Melonas
        // 1 perfect day (100% completion) = +200 Bonus Melonas
        // Starter Welcome Gift = +1,000 Melonas (네이버페이 포인트 1,000원권 바로 교환 가능)
        val earnedFromTasks = completions.size * 100
        val perfectDaysCount = completionsByDate.count { (_, list) ->
            habits.isNotEmpty() && list.size >= habits.size
        }
        val bonusFromDays = perfectDaysCount * 200
        val welcomeBonus = 1000
        val totalEarnedMelonas = welcomeBonus + earnedFromTasks + bonusFromDays
        val spentMelonas = redeemedGifts.sumOf { it.requiredMelona }
        val currentMelonas = (totalEarnedMelonas - spentMelonas).coerceAtLeast(0)

        // Extract available categories
        val defaultCategories = listOf("전체", "건강", "운동", "성장", "일상")
        val habitCategories = habits.map { it.category }.distinct()
        val allCategories = (defaultCategories + habitCategories).distinct()

        return DayCheckUiState(
            selectedDate = selectedDate,
            todayDate = today,
            habits = filteredHabits,
            categories = allCategories,
            selectedCategory = selectedCategory,
            completedCount = completedHabitsCount,
            totalCount = totalHabitsCount,
            completionRate = completionRate,
            isToday = (selectedDate == today),
            dateStrip = dateStrip,
            calendarState = calendarState,
            statsSummary = statsSummary,
            showCelebration = showCelebration,
            currentMelonas = currentMelonas,
            totalEarnedMelonas = totalEarnedMelonas,
            redeemedGifts = redeemedGifts,
            lastExchangedGift = lastExchanged
        )
    }

    private fun buildCalendarMonth(
        year: Int,
        month: Int,
        habits: List<HabitEntity>,
        completionsByDate: Map<String, List<HabitCompletionEntity>>,
        selectedDate: String,
        today: String
    ): CalendarMonthState {
        val totalHabits = habits.size
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ...
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prevCal = (cal.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val padLeading = firstDayOfWeek - 1

        val daysList = mutableListOf<CalendarDay>()
        var melonaCount = 0

        // 1. Leading days from previous month
        for (i in (daysInPrevMonth - padLeading + 1)..daysInPrevMonth) {
            val dCal = (prevCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, i) }
            val dateStr = dateFormat.format(dCal.time)
            val done = completionsByDate[dateStr]?.size ?: 0
            val isAll = totalHabits > 0 && done >= totalHabits
            daysList.add(
                CalendarDay(
                    date = dateStr,
                    dayOfMonth = i,
                    isCurrentMonth = false,
                    isToday = (dateStr == today),
                    isSelected = (dateStr == selectedDate),
                    totalCount = totalHabits,
                    completedCount = done,
                    isAllDone = isAll,
                    isPartiallyDone = done > 0 && !isAll
                )
            )
        }

        // 2. Current month days
        for (day in 1..daysInMonth) {
            val dCal = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            val dateStr = dateFormat.format(dCal.time)
            val done = completionsByDate[dateStr]?.size ?: 0
            val isAll = totalHabits > 0 && done >= totalHabits
            if (isAll) {
                melonaCount++
            }

            daysList.add(
                CalendarDay(
                    date = dateStr,
                    dayOfMonth = day,
                    isCurrentMonth = true,
                    isToday = (dateStr == today),
                    isSelected = (dateStr == selectedDate),
                    totalCount = totalHabits,
                    completedCount = done,
                    isAllDone = isAll,
                    isPartiallyDone = done > 0 && !isAll
                )
            )
        }

        // 3. Trailing days from next month
        val totalCells = if (daysList.size <= 35) 35 else 42
        val padTrailing = totalCells - daysList.size
        val nextCal = (cal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        for (day in 1..padTrailing) {
            val dCal = (nextCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            val dateStr = dateFormat.format(dCal.time)
            val done = completionsByDate[dateStr]?.size ?: 0
            val isAll = totalHabits > 0 && done >= totalHabits
            daysList.add(
                CalendarDay(
                    date = dateStr,
                    dayOfMonth = day,
                    isCurrentMonth = false,
                    isToday = (dateStr == today),
                    isSelected = (dateStr == selectedDate),
                    totalCount = totalHabits,
                    completedCount = done,
                    isAllDone = isAll,
                    isPartiallyDone = done > 0 && !isAll
                )
            )
        }

        return CalendarMonthState(
            year = year,
            month = month,
            days = daysList,
            melonaCount = melonaCount
        )
    }

    private fun buildDateStrip(
        habits: List<HabitEntity>,
        completionsByDate: Map<String, List<HabitCompletionEntity>>,
        selectedDate: String,
        today: String
    ): List<DaySummary> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val list = mutableListOf<DaySummary>()

        for (i in 0 until 10) {
            val date = calendar.time
            val dateStr = dateFormat.format(date)
            val dayOfWeek = dayOfWeekFormat.format(date)
            val dayNumber = dayNumberFormat.format(date)

            val dayCompletions = completionsByDate[dateStr]?.size ?: 0
            val totalHabits = habits.size
            val rate = if (totalHabits > 0) {
                ((dayCompletions.toFloat() / totalHabits) * 100).toInt().coerceAtMost(100)
            } else 0

            list.add(
                DaySummary(
                    date = dateStr,
                    dayOfWeek = dayOfWeek,
                    dayNumber = dayNumber,
                    completionRate = rate,
                    completedCount = dayCompletions,
                    totalCount = totalHabits,
                    isToday = (dateStr == today),
                    isSelected = (dateStr == selectedDate)
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun calculateStats(
        habits: List<HabitEntity>,
        completionsByDate: Map<String, List<HabitCompletionEntity>>,
        today: String
    ): StatsSummary {
        val totalCompletions = completionsByDate.values.sumOf { it.size }
        val totalHabits = habits.size

        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0
        var perfectDays = 0

        val calendar = Calendar.getInstance()
        val checkFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

        for (i in 0 until 30) {
            val dateStr = checkFormat.format(calendar.time)
            val doneCount = completionsByDate[dateStr]?.size ?: 0

            if (totalHabits > 0 && doneCount >= totalHabits) {
                perfectDays++
            }

            if (doneCount > 0) {
                tempStreak++
                if (tempStreak > bestStreak) bestStreak = tempStreak
            } else {
                if (i > 0 && currentStreak == 0) {
                    currentStreak = tempStreak
                }
                tempStreak = 0
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        if (currentStreak == 0) currentStreak = tempStreak

        val recent7Days = mutableListOf<DaySummary>()
        val chartCal = Calendar.getInstance()
        chartCal.add(Calendar.DAY_OF_YEAR, -6)
        var weeklySumRate = 0
        for (i in 0 until 7) {
            val d = chartCal.time
            val dStr = dateFormat.format(d)
            val done = completionsByDate[dStr]?.size ?: 0
            val rate = if (totalHabits > 0) ((done.toFloat() / totalHabits) * 100).toInt().coerceAtMost(100) else 0
            weeklySumRate += rate
            recent7Days.add(
                DaySummary(
                    date = dStr,
                    dayOfWeek = dayOfWeekFormat.format(d),
                    dayNumber = dayNumberFormat.format(d),
                    completionRate = rate,
                    completedCount = done,
                    totalCount = totalHabits,
                    isToday = (dStr == today),
                    isSelected = false
                )
            )
            chartCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val weeklyAverageRate = if (recent7Days.isNotEmpty()) weeklySumRate / recent7Days.size else 0

        return StatsSummary(
            totalCompletedCount = totalCompletions,
            currentStreak = currentStreak,
            bestStreak = bestStreak.coerceAtLeast(currentStreak),
            weeklyAverageRate = weeklyAverageRate,
            perfectDaysCount = perfectDays,
            recentDays = recent7Days
        )
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        try {
            val parsedDate = dateFormat.parse(date)
            if (parsedDate != null) {
                val cal = Calendar.getInstance().apply { time = parsedDate }
                _calendarYear.value = cal.get(Calendar.YEAR)
                _calendarMonth.value = cal.get(Calendar.MONTH) + 1
            }
        } catch (_: Exception) {}
    }

    fun selectToday() {
        _selectedDate.value = todayString
        val cal = Calendar.getInstance()
        _calendarYear.value = cal.get(Calendar.YEAR)
        _calendarMonth.value = cal.get(Calendar.MONTH) + 1
    }

    fun previousCalendarMonth() {
        if (_calendarMonth.value == 1) {
            _calendarMonth.value = 12
            _calendarYear.value -= 1
        } else {
            _calendarMonth.value -= 1
        }
    }

    fun nextCalendarMonth() {
        if (_calendarMonth.value == 12) {
            _calendarMonth.value = 1
            _calendarYear.value += 1
        } else {
            _calendarMonth.value += 1
        }
    }

    fun setFilterCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val currentState = uiState.value
            val habit = currentState.habits.find { it.id == habitId } ?: return@launch
            val newCompleted = !habit.isCompleted
            repository.toggleCompletion(habitId, currentState.selectedDate, newCompleted)

            if (newCompleted && currentState.totalCount > 0 && (currentState.completedCount + 1) == currentState.totalCount) {
                _showCelebration.value = true
            }
        }
    }

    fun dismissCelebration() {
        _showCelebration.value = false
    }

    fun resetTodayChecks() {
        viewModelScope.launch {
            val today = todayString
            repository.resetCompletionsForDate(today)
            _showCelebration.value = false
        }
    }

    fun addHabit(title: String, category: String, emoji: String) {
        viewModelScope.launch {
            repository.addHabit(title, category, emoji)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    // Melona Gift Exchange Function
    fun exchangeGift(gift: ShopGiftItem): Boolean {
        val currentState = uiState.value
        if (currentState.currentMelonas < gift.requiredMelona) {
            return false
        }

        viewModelScope.launch {
            val randomDigits = (1000..9999).random()
            val voucherCode = if (gift.category == "NAVER_PAY") {
                "NPAY-${dateFormat.format(Date()).replace("-", "")}-$randomDigits"
            } else {
                "GIFT-${gift.brand.take(3).uppercase()}-${randomDigits}-${(100..999).random()}"
            }
            val dateStr = dateFormat.format(Date())

            val giftEntity = RedeemedGiftEntity(
                giftTitle = gift.title,
                giftCategory = gift.category,
                brandName = gift.brand,
                requiredMelona = gift.requiredMelona,
                voucherCode = voucherCode,
                redeemedDate = dateStr,
                iconEmoji = gift.iconEmoji
            )

            val id = repository.redeemGift(
                title = gift.title,
                category = gift.category,
                brand = gift.brand,
                requiredMelona = gift.requiredMelona,
                emoji = gift.iconEmoji,
                date = dateStr,
                voucherCode = voucherCode
            )

            _lastExchangedGift.value = giftEntity.copy(id = id)
        }
        return true
    }

    fun dismissGiftSuccess() {
        _lastExchangedGift.value = null
    }
}
