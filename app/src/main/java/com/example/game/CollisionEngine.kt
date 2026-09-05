package com.example.game

import com.example.model.Point
import com.example.model.Segment
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object CollisionEngine {

    /**
     * Exact CCW formula:
     * CCW(A, B, C) = (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x)
     */
    fun ccw(a: Point, b: Point, c: Point): Boolean {
        return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x)
    }

    /**
     * Determines whether segment P1P2 intersects segment P3P4.
     * Guards against false positives where segments meet cleanly at a shared vertex/node.
     */
    fun doLinesIntersect(
        p1: Point,
        p2: Point,
        p3: Point,
        p4: Point,
        endpointTolerance: Float = 8.0f
    ): Boolean {
        if (p1.distanceTo(p2) < 4.0f || p3.distanceTo(p4) < 4.0f) {
            return false
        }

        // Check if endpoints coincide (e.g. shared node vertex)
        if (p1.distanceTo(p3) <= endpointTolerance ||
            p1.distanceTo(p4) <= endpointTolerance ||
            p2.distanceTo(p3) <= endpointTolerance ||
            p2.distanceTo(p4) <= endpointTolerance
        ) {
            return false
        }

        val ccw1 = ccw(p1, p3, p4)
        val ccw2 = ccw(p2, p3, p4)
        val ccw3 = ccw(p1, p2, p3)
        val ccw4 = ccw(p1, p2, p4)

        return (ccw1 != ccw2) && (ccw3 != ccw4)
    }

    /**
     * Checks if a candidate segment collides with any past echo segments.
     * hitboxScale allows the Echo Shrinker bonus (e.g. 0.5f scale factor).
     */
    fun checkCollisionWithEchoes(
        candidate: Segment,
        echoes: List<Segment>,
        endpointTolerance: Float = 8.0f,
        hitboxScale: Float = 1.0f
    ): Segment? {
        val effectiveTolerance = endpointTolerance * hitboxScale
        for (echo in echoes) {
            if (doLinesIntersect(candidate.p1, candidate.p2, echo.p1, echo.p2, effectiveTolerance)) {
                return echo
            }
        }
        return null
    }

    /**
     * Self-Intersection check (Anlık Yol Çarpışması):
     * Checks whether candidate segment (being drawn) intersects any earlier non-adjacent
     * segments of the active stroke.
     */
    fun checkSelfIntersection(
        activeSegments: List<Segment>,
        candidate: Segment
    ): Boolean {
        if (activeSegments.size < 2) return false

        // Check all segments except the immediate predecessor (which shares candidate.p1)
        val checkLimit = activeSegments.size - 1
        for (i in 0 until checkLimit) {
            val pastSegment = activeSegments[i]
            if (doLinesIntersect(candidate.p1, candidate.p2, pastSegment.p1, pastSegment.p2, endpointTolerance = 8.0f)) {
                return true
            }
        }
        return false
    }

    /**
     * Distance from point P to line segment AB.
     */
    fun distanceToSegment(p: Point, a: Point, b: Point): Float {
        val lengthSq = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (lengthSq < 0.0001f) return p.distanceTo(a)

        // Projection factor t clamped between 0 and 1
        val t = max(0f, min(1f, ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / lengthSq))
        val projX = a.x + t * (b.x - a.x)
        val projY = a.y + t * (b.y - a.y)

        val dx = p.x - projX
        val dy = p.y - projY
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Finds the minimum distance from point P to any segment in echoes.
     */
    fun minDistanceToEchoes(point: Point, echoes: List<Segment>): Float {
        if (echoes.isEmpty()) return Float.MAX_VALUE
        var minDst = Float.MAX_VALUE
        for (echo in echoes) {
            val d = distanceToSegment(point, echo.p1, echo.p2)
            if (d < minDst) {
                minDst = d
            }
        }
        return minDst
    }
}
