package com.example.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val sleepQualityRating: Float, // 1 to 5 scale
    val notes: String = ""
)

@Dao
interface SleepSessionDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY endTimeMillis DESC")
    fun getAllSleepSessions(): Flow<List<SleepSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSession(session: SleepSession): Long

    @Update
    suspend fun updateSleepSession(session: SleepSession)

    @Delete
    suspend fun deleteSleepSession(session: SleepSession)
}
