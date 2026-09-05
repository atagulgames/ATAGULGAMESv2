package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.hypot

data class Point(val x: Float, val y: Float) {
    fun distanceTo(other: Point): Float = hypot(x - other.x, y - other.y)
}

enum class NodeType {
    NORMAL,
    KEY,
    GATE
}

data class Node(
    val id: Int,
    val x: Float,
    val y: Float,
    val connected: Boolean = false,
    val type: NodeType = NodeType.NORMAL,
    val keyForGateId: Int = -1,
    val isKeyCollected: Boolean = false,
    val isGateUnlocked: Boolean = false
) {
    fun toPoint(): Point = Point(x, y)
}

data class Segment(
    val p1: Point,
    val p2: Point,
    val fromNodeId: Int = -1,
    val toNodeId: Int = -1
) {
    val length: Float get() = p1.distanceTo(p2)
}

data class DirectedEdge(
    val fromId: Int,
    val toId: Int
)

data class EchoStroke(
    val id: Int,
    val segments: List<Segment>,
    val remainingAttempts: Int = 3,
    val maxLifetime: Int = 3,
    val isGhost: Boolean = false
)

data class LevelNode(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: NodeType = NodeType.NORMAL,
    val keyForGateId: Int = -1
)

data class LevelData(
    val levelId: Int,
    val title: String,
    val gridSize: Int,
    val nodes: List<LevelNode>,
    val directedEdges: List<DirectedEdge> = emptyList(),
    val parEchoes: Int = 0,
    val hintOrder: List<Int> = emptyList(),
    val mechanicType: String = "STANDARD", // STANDARD, DECAYING, LOCK_KEY, ONE_WAY, GHOST, MAZE
    val decayLifetime: Int = 3,
    val isGhostEchoes: Boolean = false,
    val description: String = ""
)

enum class GameStatus {
    PLAYING,
    VICTORY,
    DEADLOCK
}

enum class ScreenState {
    INTRO,
    MAIN_MENU,
    PLAYING_LEVEL,
    DAILY_CHALLENGE
}

data class EchoStats(
    val totalEchoesCreated: Int = 0,
    val completedLevelsCount: Int = 0,
    val totalPlayTimeSec: Long = 0L
)

enum class StrokeTheme(
    val displayName: String,
    val primaryColor: Color,
    val glowColor: Color
) {
    NEON_CYAN("Okyanus Mavisi", Color(0xFF0284C7), Color(0x440284C7)),
    SOLAR_FLAME("Güneş Turuncusu", Color(0xFFEA580C), Color(0x44EA580C)),
    CYBER_MAGENTA("Fuşya Enerji", Color(0xFFD946EF), Color(0x44D946EF)),
    ZEN_INK("Gece Kobaltı", Color(0xFF312E81), Color(0x44312E81))
}

enum class EchoTheme(
    val displayName: String,
    val echoColor: Color,
    val glowColor: Color
) {
    ELECTRIC_RED("Kızıl Lazer", Color(0xFFDC2626), Color(0x44DC2626)),
    SHATTERED_ICE("Buz Mavisi", Color(0xFF0284C7), Color(0x440284C7)),
    VOID_DARK("Mor Sis", Color(0xFF7C3AED), Color(0x447C3AED))
}
