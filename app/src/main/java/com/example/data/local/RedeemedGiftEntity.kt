package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "redeemed_gifts")
data class RedeemedGiftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val giftTitle: String,
    val giftCategory: String, // "NAVER_PAY" or "GIFTICON"
    val brandName: String,
    val requiredMelona: Int,
    val voucherCode: String,
    val redeemedDate: String,
    val iconEmoji: String = "🎁",
    val isUsed: Boolean = false
)
