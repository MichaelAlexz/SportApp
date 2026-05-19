package com.sportapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoal(id: String): GoalEntity?

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeGoal(id: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals")
    fun observeAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity)

    @Query("UPDATE goals SET currentValue = currentValue + :increment WHERE id = :id")
    suspend fun addProgress(id: String, increment: Float)
}
