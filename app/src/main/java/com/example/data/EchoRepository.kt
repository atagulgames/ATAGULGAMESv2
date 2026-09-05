package com.example.data

import android.content.Context
import com.example.data.local.EchoDatabase
import com.example.data.local.LevelEntity
import com.example.model.LevelData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EchoRepository(context: Context) {

    private val db = EchoDatabase.getDatabase(context)
    private val levelDao = db.levelDao()

    fun getAllLevels(): Flow<List<LevelEntity>> = levelDao.getAllLevels()

    fun getTotalStars(): Flow<Int?> = levelDao.getTotalStars()

    fun getCompletedCount(): Flow<Int> = levelDao.getCompletedCount()

    suspend fun getLevelData(id: Int): LevelData? = withContext(Dispatchers.IO) {
        val entity = levelDao.getLevelById(id) ?: return@withContext null
        LevelCatalog.entityToLevelData(entity)
    }

    suspend fun ensureLevelsPopulated() = withContext(Dispatchers.IO) {
        val count = levelDao.getLevelCount()
        val levels = LevelCatalog.create250Levels()
        if (count < 250) {
            levelDao.insertAll(levels)
        }
        // Sync level definitions (titles, nodes, mechanics) while preserving completion and stars
        for (lvl in levels) {
            levelDao.updateLevelDefinition(
                id = lvl.id,
                title = lvl.title,
                gridSize = lvl.gridSize,
                nodesJson = lvl.nodesJson,
                edgesJson = lvl.edgesJson,
                parEchoes = lvl.parEchoes,
                hintOrderJson = lvl.hintOrderJson,
                mechanicType = lvl.mechanicType,
                decayLifetime = lvl.decayLifetime,
                isGhostEchoes = lvl.isGhostEchoes,
                description = lvl.description
            )
        }
    }

    suspend fun recordVictory(levelId: Int, echoCount: Int, parEchoes: Int) = withContext(Dispatchers.IO) {
        val stars = when {
            echoCount <= parEchoes -> 3
            echoCount <= parEchoes + 2 -> 2
            else -> 1
        }
        val current = levelDao.getLevelById(levelId)
        val bestEchoes = if (current != null && current.bestEchoCount >= 0) {
            minOf(current.bestEchoCount, echoCount)
        } else {
            echoCount
        }
        val maxStars = if (current != null) maxOf(current.stars, stars) else stars

        levelDao.updateProgress(levelId, maxStars, true, bestEchoes)
        // Unlock next level up to 250
        if (levelId < 250) {
            levelDao.unlockLevel(levelId + 1)
        }
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        levelDao.resetAllProgress()
    }
}
