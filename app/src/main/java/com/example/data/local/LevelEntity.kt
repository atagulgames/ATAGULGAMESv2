package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val gridSize: Int,
    val nodesJson: String,
    val edgesJson: String = "",
    val parEchoes: Int = 0,
    val hintOrderJson: String = "",
    val mechanicType: String = "STANDARD", // STANDARD, DECAYING, LOCK_KEY, ONE_WAY, GHOST, MAZE
    val decayLifetime: Int = 3,
    val isGhostEchoes: Boolean = false,
    val description: String = "",
    val stars: Int = 0,
    val isCompleted: Boolean = false,
    val isUnlocked: Boolean = false,
    val bestEchoCount: Int = -1
)
