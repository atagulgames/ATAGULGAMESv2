package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {

    @Query("SELECT * FROM levels ORDER BY id ASC")
    fun getAllLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE id = :id")
    suspend fun getLevelById(id: Int): LevelEntity?

    @Query("SELECT COUNT(*) FROM levels")
    suspend fun getLevelCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(levels: List<LevelEntity>)

    @Query("UPDATE levels SET stars = :stars, isCompleted = :isCompleted, bestEchoCount = :bestEchoCount WHERE id = :id")
    suspend fun updateProgress(id: Int, stars: Int, isCompleted: Boolean, bestEchoCount: Int)

    @Query("UPDATE levels SET isUnlocked = 1 WHERE id = :id")
    suspend fun unlockLevel(id: Int)

    @Query("UPDATE levels SET title = :title, gridSize = :gridSize, nodesJson = :nodesJson, edgesJson = :edgesJson, parEchoes = :parEchoes, hintOrderJson = :hintOrderJson, mechanicType = :mechanicType, decayLifetime = :decayLifetime, isGhostEchoes = :isGhostEchoes, description = :description WHERE id = :id")
    suspend fun updateLevelDefinition(
        id: Int,
        title: String,
        gridSize: Int,
        nodesJson: String,
        edgesJson: String,
        parEchoes: Int,
        hintOrderJson: String,
        mechanicType: String,
        decayLifetime: Int,
        isGhostEchoes: Boolean,
        description: String
    )

    @Query("SELECT SUM(stars) FROM levels WHERE isCompleted = 1")
    fun getTotalStars(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM levels WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("UPDATE levels SET isUnlocked = CASE WHEN id = 1 THEN 1 ELSE 0 END, isCompleted = 0, stars = 0, bestEchoCount = -1")
    suspend fun resetAllProgress()
}
