package com.avidlearner.rem.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {

    @Query("SELECT * FROM timers ORDER BY hour ASC, minute ASC")
    fun getAllTimers(): Flow<List<Timer>>

    @Query("SELECT * FROM timers WHERE isEnabled = 1")
    suspend fun getEnabledTimersSync(): List<Timer>

    @Query("SELECT * FROM timers WHERE id = :id LIMIT 1")
    suspend fun getTimerById(id: Int): Timer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: Timer): Long

    @Update
    suspend fun updateTimer(timer: Timer)

    @Delete
    suspend fun deleteTimer(timer: Timer)
}
