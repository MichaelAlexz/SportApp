package com.sportapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout_records ORDER BY startTimeMs DESC")
    fun getAllWorkouts(): Flow<List<WorkoutRecord>>

    @Query("SELECT * FROM workout_records WHERE type = :type ORDER BY startTimeMs DESC")
    fun getWorkoutsByType(type: String): Flow<List<WorkoutRecord>>

    @Query("SELECT * FROM workout_records WHERE startTimeMs >= :sinceMs ORDER BY startTimeMs DESC")
    fun getWorkoutsSince(sinceMs: Long): Flow<List<WorkoutRecord>>

    @Query("SELECT * FROM workout_records WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutRecord?

    @Query("""
        SELECT SUM(distanceMeters) FROM workout_records 
        WHERE startTimeMs >= :sinceMs AND isCompleted = 1
    """)
    fun getTotalDistanceSince(sinceMs: Long): Flow<Float?>

    @Query("""
        SELECT SUM(durationSeconds) FROM workout_records 
        WHERE startTimeMs >= :sinceMs AND isCompleted = 1
    """)
    fun getTotalDurationSince(sinceMs: Long): Flow<Long?>

    @Query("""
        SELECT SUM(caloriesBurned) FROM workout_records 
        WHERE startTimeMs >= :sinceMs AND isCompleted = 1
    """)
    fun getTotalCaloriesSince(sinceMs: Long): Flow<Float?>

    @Query("SELECT COUNT(*) FROM workout_records WHERE startTimeMs >= :sinceMs AND isCompleted = 1")
    fun getWorkoutCountSince(sinceMs: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WorkoutRecord): Long

    @Update
    suspend fun update(record: WorkoutRecord)

    @Delete
    suspend fun delete(record: WorkoutRecord)

    @Query("DELETE FROM workout_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
