package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsByHabitId(habitId: Long)

    @Transaction
    suspend fun removeHabitWithCompletions(habitId: Long) {
        deleteCompletionsByHabitId(habitId)
        deleteHabitById(habitId)
    }

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions")
    fun getAllCompletions(): Flow<List<HabitCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("DELETE FROM habit_completions WHERE date = :date")
    suspend fun deleteCompletionsForDate(date: String)

    // Redeemed Gifts operations
    @Query("SELECT * FROM redeemed_gifts ORDER BY id DESC")
    fun getAllRedeemedGifts(): Flow<List<RedeemedGiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedeemedGift(gift: RedeemedGiftEntity): Long

    @Update
    suspend fun updateRedeemedGift(gift: RedeemedGiftEntity)

    @Query("DELETE FROM redeemed_gifts WHERE id = :id")
    suspend fun deleteRedeemedGift(id: Long)
}
