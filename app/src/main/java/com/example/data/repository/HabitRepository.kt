package com.example.data.repository

import com.example.data.local.HabitCompletionEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.RedeemedGiftEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allCompletions: Flow<List<HabitCompletionEntity>> = habitDao.getAllCompletions()
    val allRedeemedGifts: Flow<List<RedeemedGiftEntity>> = habitDao.getAllRedeemedGifts()

    fun getCompletionsForDate(date: String): Flow<List<HabitCompletionEntity>> {
        return habitDao.getCompletionsForDate(date)
    }

    suspend fun addHabit(title: String, category: String, iconEmoji: String): Long {
        val habit = HabitEntity(
            title = title.trim(),
            category = category.trim(),
            iconEmoji = iconEmoji,
            createdAt = System.currentTimeMillis()
        )
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.removeHabitWithCompletions(habitId)
    }

    suspend fun toggleCompletion(habitId: Long, date: String, completed: Boolean) {
        if (completed) {
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    date = date,
                    completedAt = System.currentTimeMillis()
                )
            )
        } else {
            habitDao.deleteCompletion(habitId, date)
        }
    }

    suspend fun resetCompletionsForDate(date: String) {
        habitDao.deleteCompletionsForDate(date)
    }

    suspend fun redeemGift(
        title: String,
        category: String,
        brand: String,
        requiredMelona: Int,
        emoji: String,
        date: String,
        voucherCode: String
    ): Long {
        val gift = RedeemedGiftEntity(
            giftTitle = title,
            giftCategory = category,
            brandName = brand,
            requiredMelona = requiredMelona,
            voucherCode = voucherCode,
            redeemedDate = date,
            iconEmoji = emoji
        )
        return habitDao.insertRedeemedGift(gift)
    }

    suspend fun deleteRedeemedGift(id: Long) {
        habitDao.deleteRedeemedGift(id)
    }

    suspend fun ensureStarterHabitsIfEmpty() {
        if (habitDao.getHabitCount() == 0) {
            habitDao.insertHabits(
                listOf(
                    HabitEntity(
                        title = "아침 물 한 잔 마시기",
                        category = "건강",
                        iconEmoji = "💧",
                        sortOrder = 0
                    ),
                    HabitEntity(
                        title = "가벼운 스트레칭 10분",
                        category = "운동",
                        iconEmoji = "🧘",
                        sortOrder = 1
                    ),
                    HabitEntity(
                        title = "비타민/영양제 챙겨먹기",
                        category = "건강",
                        iconEmoji = "💊",
                        sortOrder = 2
                    ),
                    HabitEntity(
                        title = "마음 편한 독서 15분",
                        category = "성장",
                        iconEmoji = "📖",
                        sortOrder = 3
                    ),
                    HabitEntity(
                        title = "감사한 일 세 가지 기록",
                        category = "일상",
                        iconEmoji = "✨",
                        sortOrder = 4
                    )
                )
            )
        }
    }
}
