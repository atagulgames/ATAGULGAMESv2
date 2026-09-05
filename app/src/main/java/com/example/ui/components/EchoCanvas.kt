package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.DirectedEdge
import com.example.model.EchoStroke
import com.example.model.EchoTheme
import com.example.model.GameStatus
import com.example.model.Node
import com.example.model.NodeType
import com.example.model.Point
import com.example.model.Segment
import com.example.model.StrokeTheme
import com.example.viewmodel.EchoUiState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private val ColorGridDot = Color(0x3394A3B8)
private val ColorHintGold = Color(0xFFD97706)
private val ColorKeyGold = Color(0xFFD97706)
private val ColorGateLocked = Color(0xFFE11D48)
private val ColorGateUnlocked = Color(0xFF059669)

@Composable
fun EchoCanvas(
    state: EchoUiState,
    onPointerDown: (Point) -> Unit,
    onPointerMove: (Point) -> Unit,
    onPointerUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EchoPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Shake offset if collision alert is active
    val shakeX = if (state.isCollisionAlertActive) remember(state.isCollisionAlertActive) {
        (Random.nextInt(-14, 14)).dp
    } else 0.dp
    val shakeY = if (state.isCollisionAlertActive) remember(state.isCollisionAlertActive) {
        (Random.nextInt(-14, 14)).dp
    } else 0.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .offset(x = shakeX, y = shakeY)
            .testTag("echo_canvas_container")
    ) {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        val virtualW = 360f
        val virtualH = 480f

        val scale = min(canvasWidth / virtualW, canvasHeight / virtualH) * 0.90f
        val offsetX = (canvasWidth - virtualW * scale) / 2f
        val offsetY = (canvasHeight - virtualH * scale) / 2f

        fun toVirtual(screen: Offset): Point {
            val vx = (screen.x - offsetX) / scale
            val vy = (screen.y - offsetY) / scale
            return Point(vx, vy)
        }

        fun toScreen(vPoint: Point): Offset {
            return Offset(vPoint.x * scale + offsetX, vPoint.y * scale + offsetY)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("echo_canvas")
                .pointerInput(state.gameStatus) {
                    if (state.gameStatus == GameStatus.PLAYING) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            onPointerDown(toVirtual(down.position))
                            down.consume()

                            do {
                                val event = awaitPointerEvent()
                                val current = event.changes.firstOrNull()
                                if (current != null) {
                                    if (current.pressed) {
                                        onPointerMove(toVirtual(current.position))
                                        current.consume()
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            onPointerUp()
                        }
                    }
                }
        ) {
            // 1. Grid Background
            drawGridBackground(size.width, size.height)

            // 2. Directed edge arrows (if level has one-way edges)
            drawDirectedEdgeArrows(
                edges = state.level.directedEdges,
                nodes = state.nodes,
                toScreen = ::toScreen,
                scale = scale
            )

            // 3. Hint path
            if (state.isHintActive && state.level.hintOrder.isNotEmpty()) {
                drawHintPath(
                    hintOrder = state.level.hintOrder,
                    nodes = state.nodes,
                    toScreen = ::toScreen,
                    scale = scale
                )
            }

            // 4. Past Echo Barriers (Collision Obstacles with Echo Theme & Decaying opacity)
            drawEchoBarriers(
                echoes = state.echoes,
                echoTheme = state.echoTheme,
                isShrinkerActive = state.isEchoShrinkerActive,
                pulseAlpha = pulseAlpha,
                toScreen = ::toScreen,
                scale = scale
            )

            // 5. Active Player Stroke with Stroke Theme
            drawPlayerStroke(
                segments = state.currentStrokeSegments,
                currentPointerPos = state.currentPointerPos,
                visitedNodeIds = state.visitedNodeIds,
                nodes = state.nodes,
                strokeTheme = state.strokeTheme,
                toScreen = ::toScreen,
                scale = scale
            )

            // 6. Game Nodes (with Normal, Key, Gate styles, and Character Node 1)
            drawNodes(
                nodes = state.nodes,
                visitedNodeIds = state.visitedNodeIds,
                collectedKeyIds = state.collectedKeyIds,
                strokeTheme = state.strokeTheme,
                isHintActive = state.isHintActive,
                hintOrder = state.level.hintOrder,
                pulseAlpha = pulseAlpha,
                isDrawing = state.isDrawing,
                toScreen = ::toScreen,
                scale = scale
            )

            // 7. Proximity Warning / Electric Glitch
            if (state.isProximityAlertActive) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x33FF2E63), Color.Transparent),
                        center = center,
                        radius = size.width * 0.9f
                    )
                )
            }

            // 8. Violent Collision Flash
            if (state.isCollisionAlertActive) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x99FF2E63), Color(0x33FF2E63), Color.Transparent),
                        center = center,
                        radius = size.width * 0.75f
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawGridBackground(w: Float, h: Float) {
    val step = 40f
    val points = mutableListOf<Offset>()
    var x = step / 2f
    while (x < w) {
        var y = step / 2f
        while (y < h) {
            points.add(Offset(x, y))
            y += step
        }
        x += step
    }
    drawPoints(
        points = points,
        pointMode = PointMode.Points,
        color = ColorGridDot,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDirectedEdgeArrows(
    edges: List<DirectedEdge>,
    nodes: List<Node>,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    for (edge in edges) {
        val fromNode = nodes.firstOrNull { it.id == edge.fromId }
        val toNode = nodes.firstOrNull { it.id == edge.toId }
        if (fromNode != null && toNode != null) {
            val p1 = toScreen(fromNode.toPoint())
            val p2 = toScreen(toNode.toPoint())

            // Midpoint
            val midX = (p1.x + p2.x) / 2f
            val midY = (p1.y + p2.y) / 2f

            val angle = atan2(p2.y - p1.y, p2.x - p1.x)
            val arrowSize = 12f * (scale / 1.5f).coerceAtLeast(1f)

            // Draw arrow head pointing toward p2
            val path = Path().apply {
                moveTo(midX, midY)
                lineTo(
                    (midX - arrowSize * cos(angle - Math.PI / 6)).toFloat(),
                    (midY - arrowSize * sin(angle - Math.PI / 6)).toFloat()
                )
                moveTo(midX, midY)
                lineTo(
                    (midX - arrowSize * cos(angle + Math.PI / 6)).toFloat(),
                    (midY - arrowSize * sin(angle + Math.PI / 6)).toFloat()
                )
            }
            drawPath(path, color = Color(0x9900F0FF), style = Stroke(width = 3f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawEchoBarriers(
    echoes: List<EchoStroke>,
    echoTheme: EchoTheme,
    isShrinkerActive: Boolean,
    pulseAlpha: Float,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val widthFactor = if (isShrinkerActive) 0.5f else 1.0f

    for (echo in echoes) {
        val lifeRatio = (echo.remainingAttempts.toFloat() / echo.maxLifetime).coerceIn(0.2f, 1f)
        val alpha = if (echo.isGhost) 0.20f else (lifeRatio * pulseAlpha)

        val glowColor = echoTheme.glowColor.copy(alpha = alpha * 0.5f)
        val barrierColor = echoTheme.echoColor.copy(alpha = alpha)

        val glowWidth = 14f * widthFactor * (scale / 1.5f).coerceAtLeast(1f)
        val coreWidth = 5f * widthFactor * (scale / 1.5f).coerceAtLeast(1f)

        for (seg in echo.segments) {
            val start = toScreen(seg.p1)
            val end = toScreen(seg.p2)

            // Neon glow aura
            drawLine(
                color = glowColor,
                start = start,
                end = end,
                strokeWidth = glowWidth,
                cap = StrokeCap.Round
            )

            // Core lethal laser
            drawLine(
                color = barrierColor,
                start = start,
                end = end,
                strokeWidth = coreWidth,
                cap = StrokeCap.Round
            )

            // Echo endpoint danger nodes
            drawCircle(
                color = barrierColor,
                radius = 3.5f * widthFactor * (scale / 1.5f).coerceAtLeast(1f),
                center = start
            )
            drawCircle(
                color = barrierColor,
                radius = 3.5f * widthFactor * (scale / 1.5f).coerceAtLeast(1f),
                center = end
            )
        }
    }
}

private fun DrawScope.drawPlayerStroke(
    segments: List<Segment>,
    currentPointerPos: Point?,
    visitedNodeIds: List<Int>,
    nodes: List<Node>,
    strokeTheme: StrokeTheme,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val glowWidth = 16f * (scale / 1.5f).coerceAtLeast(1f)
    val coreWidth = 6f * (scale / 1.5f).coerceAtLeast(1f)

    for (seg in segments) {
        val start = toScreen(seg.p1)
        val end = toScreen(seg.p2)

        drawLine(
            color = strokeTheme.glowColor,
            start = start,
            end = end,
            strokeWidth = glowWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = strokeTheme.primaryColor,
            start = start,
            end = end,
            strokeWidth = coreWidth,
            cap = StrokeCap.Round
        )
    }

    if (currentPointerPos != null && visitedNodeIds.isNotEmpty()) {
        val lastNodeId = visitedNodeIds.last()
        val lastNode = nodes.firstOrNull { it.id == lastNodeId }
        if (lastNode != null) {
            val start = toScreen(lastNode.toPoint())
            val end = toScreen(currentPointerPos)

            drawLine(
                color = strokeTheme.glowColor,
                start = start,
                end = end,
                strokeWidth = glowWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = strokeTheme.primaryColor,
                start = start,
                end = end,
                strokeWidth = coreWidth,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = strokeTheme.glowColor,
                radius = 18f * (scale / 1.5f).coerceAtLeast(1f),
                center = end,
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = strokeTheme.primaryColor,
                radius = 5f * (scale / 1.5f).coerceAtLeast(1f),
                center = end
            )
        }
    }
}

private fun DrawScope.drawHintPath(
    hintOrder: List<Int>,
    nodes: List<Node>,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
    for (i in 0 until hintOrder.size - 1) {
        val fromId = hintOrder[i]
        val toId = hintOrder[i + 1]
        val fromNode = nodes.firstOrNull { it.id == fromId }
        val toNode = nodes.firstOrNull { it.id == toId }
        if (fromNode != null && toNode != null) {
            val p1 = toScreen(fromNode.toPoint())
            val p2 = toScreen(toNode.toPoint())
            drawLine(
                color = ColorHintGold.copy(alpha = 0.65f),
                start = p1,
                end = p2,
                strokeWidth = 3f * (scale / 1.5f).coerceAtLeast(1f),
                pathEffect = dashEffect,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawNodes(
    nodes: List<Node>,
    visitedNodeIds: List<Int>,
    collectedKeyIds: Set<Int>,
    strokeTheme: StrokeTheme,
    isHintActive: Boolean,
    hintOrder: List<Int>,
    pulseAlpha: Float,
    isDrawing: Boolean,
    toScreen: (Point) -> Offset,
    scale: Float
) {
    val baseRadius = 20f * (scale / 1.5f).coerceAtLeast(1f)

    for (node in nodes) {
        val pos = toScreen(node.toPoint())
        val isVisited = visitedNodeIds.contains(node.id)
        val isCharacterStart = node.id == 1

        // Character starting node special glowing pulse & beacon
        if (isCharacterStart) {
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.28f * pulseAlpha),
                radius = baseRadius * (1.75f + 0.25f * (1f - pulseAlpha)),
                center = pos
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                radius = baseRadius * 1.35f,
                center = pos,
                style = Stroke(width = 3.5f)
            )

            // When idle before drawing, show "KARAKTER" tag above Node 1
            if (visitedNodeIds.isEmpty() && !isDrawing) {
                drawContext.canvas.nativeCanvas.apply {
                    val labelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#0284C7")
                        textSize = baseRadius * 0.75f
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText("KARAKTER", pos.x, pos.y - baseRadius * 1.6f, labelPaint)
                }
            }
        }

        // Highlight next target node in the strict sequence (sıra numarası rehberi)
        val nextExpectedId = if (visitedNodeIds.isEmpty()) 1 else (visitedNodeIds.lastOrNull() ?: 0) + 1
        val isNextTarget = isDrawing && node.id == nextExpectedId
        if (isNextTarget) {
            drawCircle(
                color = Color(0xFF0284C7).copy(alpha = 0.30f * pulseAlpha),
                radius = baseRadius * (1.6f + 0.25f * (1f - pulseAlpha)),
                center = pos
            )
            drawCircle(
                color = Color(0xFF0284C7),
                radius = baseRadius * 1.25f,
                center = pos,
                style = Stroke(width = 2.5f)
            )
        }

        when (node.type) {
            NodeType.KEY -> {
                // Key Node: Golden Amber styling with Diamond shape
                val isCollected = collectedKeyIds.contains(node.keyForGateId)
                val keyColor = if (isCollected) strokeTheme.primaryColor else ColorKeyGold

                drawCircle(
                    color = keyColor.copy(alpha = 0.35f),
                    radius = baseRadius * 1.5f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = baseRadius,
                    center = pos
                )
                drawCircle(
                    color = keyColor,
                    radius = baseRadius,
                    center = pos,
                    style = Stroke(width = 3.5f)
                )

                // Diamond icon in center
                val path = Path().apply {
                    moveTo(pos.x, pos.y - baseRadius * 0.55f)
                    lineTo(pos.x + baseRadius * 0.55f, pos.y)
                    lineTo(pos.x, pos.y + baseRadius * 0.55f)
                    lineTo(pos.x - baseRadius * 0.55f, pos.y)
                    close()
                }
                drawPath(path, color = keyColor.copy(alpha = 0.4f))
            }

            NodeType.GATE -> {
                // Gate Node: Locked Red border until key gathered
                val isUnlocked = collectedKeyIds.contains(node.keyForGateId)
                val gateColor = if (isUnlocked) ColorGateUnlocked else ColorGateLocked

                drawCircle(
                    color = gateColor.copy(alpha = 0.25f),
                    radius = baseRadius * 1.5f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF131A2A),
                    radius = baseRadius,
                    center = pos
                )
                drawCircle(
                    color = gateColor,
                    radius = baseRadius,
                    center = pos,
                    style = Stroke(width = 3.5f)
                )

                // Lock core symbol
                drawCircle(
                    color = gateColor.copy(alpha = 0.35f),
                    radius = baseRadius * 0.45f,
                    center = pos
                )
            }

            NodeType.NORMAL -> {
                if (isVisited) {
                    drawCircle(
                        color = strokeTheme.glowColor,
                        radius = baseRadius * 1.5f,
                        center = pos
                    )
                    drawCircle(
                        color = strokeTheme.primaryColor,
                        radius = baseRadius,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius * 0.35f,
                        center = pos
                    )
                } else if (isCharacterStart) {
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius,
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFF0284C7),
                        radius = baseRadius,
                        center = pos,
                        style = Stroke(width = 3.5f)
                    )
                } else {
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius,
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFFCBD5E1),
                        radius = baseRadius,
                        center = pos,
                        style = Stroke(width = 2.5f)
                    )
                }
            }
        }

        // Draw node number (1, 2, 3... N) or hint step
        val displayText = if (isHintActive) {
            val stepIndex = hintOrder.indexOf(node.id)
            if (stepIndex != -1) (stepIndex + 1).toString() else node.id.toString()
        } else {
            node.id.toString()
        }

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = when {
                    isVisited -> android.graphics.Color.WHITE
                    isCharacterStart -> android.graphics.Color.parseColor("#0284C7")
                    node.type == NodeType.KEY -> android.graphics.Color.parseColor("#D97706")
                    node.type == NodeType.GATE -> if (collectedKeyIds.contains(node.keyForGateId)) android.graphics.Color.parseColor("#059669") else android.graphics.Color.parseColor("#E11D48")
                    else -> android.graphics.Color.parseColor("#0F172A")
                }
                textSize = baseRadius * 1.05f
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val textY = pos.y - ((paint.descent() + paint.ascent()) / 2f)
            drawText(displayText, pos.x, textY, paint)
        }
    }
}
