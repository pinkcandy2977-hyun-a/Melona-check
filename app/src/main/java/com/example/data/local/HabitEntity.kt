package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "일상",
    val iconEmoji: String = "🌱",
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
