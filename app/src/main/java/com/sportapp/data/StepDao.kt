package com.sportapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query("SELECT * FROM step_records WHERE date = :date")
    suspend fun getStepRecord(date: String): StepRecord?

    @Query("SELECT * FROM step_records WHERE date = :date")
    fun observeStepRecord(date: String): Flow<StepRecord?>

    @Query("SELECT * FROM step_records ORDER BY date DESC LIMIT 7")
    fun getWeekSteps(): Flow<List<StepRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: StepRecord)

    @Query("UPDATE step_records SET totalSteps = :steps, lastUpdatedMs = :time WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE step_records SET activeMinutes = activeMinutes + :minutes WHERE date = :date")
    suspend fun addActiveMinutes(date: String, minutes: Float)
}
