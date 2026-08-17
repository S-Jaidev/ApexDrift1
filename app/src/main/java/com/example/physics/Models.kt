package com.example.physics

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(val x: Float, val y: Float) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    fun length(): Float = hypot(x, y)
    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }
    fun distanceTo(other: Vector2D): Float = hypot(x - other.x, y - other.y)
    fun dot(other: Vector2D): Float = x * other.x + y * other.y
    fun angle(): Float = atan2(y, x)
}

data class TrackCheckpoint(
    val id: Int,
    val center: Vector2D,
    val direction: Vector2D,
    val width: Float = 160f
)

data class TrackDef(
    val id: String,
    val name: String,
    val description: String,
    val surfaceType: String, // Asphalt, Dirt, CyberGrid
    val surfaceFriction: Float, // 1.0 = High grip, 0.6 = Slippery dirt
    val trackWidth: Float = 160f,
    val totalLaps: Int = 3,
    val nodes: List<Vector2D>, // Path nodes
    val checkpoints: List<TrackCheckpoint> = emptyList()
)

data class SkidMarkPoint(
    val position: Vector2D,
    val alpha: Float = 1.0f,
    val headingAngle: Float
)

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val alpha: Float = 1.0f,
    val colorHex: Long,
    val size: Float = 8f
)

data class CarPhysicsState(
    val position: Vector2D = Vector2D(0f, 0f),
    val velocity: Vector2D = Vector2D(0f, 0f),
    val speedKmh: Float = 0f,
    val headingAngle: Float = 0f, // Radians
    val steeringAngle: Float = 0f, // Radians
    val isDrifting: Boolean = false,
    val driftAngle: Float = 0f, // Radians
    val driftScoreTotal: Int = 0,
    val currentDriftCombo: Int = 0,
    val nitroRemaining: Float = 1.0f, // 0.0 to 1.0
    val isNitroActive: Boolean = false,
    val rpm: Float = 1000f,
    val gear: Int = 1,
    val currentLap: Int = 1,
    val checkpointIndex: Int = 0,
    val lapStartTimeMs: Long = 0L,
    val currentLapTimeMs: Long = 0L,
    val lastLapTimeMs: Long = 0L,
    val bestLapTimeMs: Long = 0L,
    val isFinished: Boolean = false,
    val sectorTimesMs: List<Long> = emptyList(),
    val skidMarks: List<SkidMarkPoint> = emptyList(),
    val particles: List<Particle> = emptyList()
)

data class VehiclePerformanceSpec(
    val topSpeedKmh: Float, // e.g. 240.0
    val accelerationPower: Float, // e.g. 18.0
    val handlingResponsiveness: Float, // e.g. 2.5
    val brakingPower: Float, // e.g. 35.0
    val nitroCapacity: Float // e.g. 1.0
)
