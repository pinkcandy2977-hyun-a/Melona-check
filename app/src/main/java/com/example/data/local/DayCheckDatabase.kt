package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [HabitEntity::class, HabitCompletionEntity::class, RedeemedGiftEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DayCheckDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: DayCheckDatabase? = null

        fun getInstance(context: Context): DayCheckDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DayCheckDatabase::class.java,
                    "daycheck_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default starter habits on initial creation
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).habitDao().insertHabits(
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
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
