package com.example.physics

import com.example.data.entity.VehicleEntity
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object PhysicsEngine {

    fun calculateSpecs(vehicle: VehicleEntity): VehiclePerformanceSpec {
        val baseTopSpeed = when (vehicle.category) {
            "Hypercar" -> 260f
            "Sports R" -> 220f
            "Drift Spec" -> 210f
            "Muscle" -> 230f
            "Off-Road Rally" -> 190f
            else -> 200f
        }
        val topSpeed = baseTopSpeed + (vehicle.engineStage - 1) * 12f + (vehicle.turboStage - 1) * 8f

        val baseAccel = when (vehicle.category) {
            "Hypercar" -> 24f
            "Muscle" -> 22f
            "Sports R" -> 19f
            "Drift Spec" -> 18f
            "Off-Road Rally" -> 20f
            else -> 18f
        }
        val accel = baseAccel + (vehicle.engineStage - 1) * 3f + (vehicle.turboStage - 1) * 4f

        val baseHandling = when (vehicle.category) {
            "Drift Spec" -> 3.2f
            "Sports R" -> 2.8f
            "Hypercar" -> 3.0f
            "Off-Road Rally" -> 2.6f
            "Muscle" -> 2.2f
            else -> 2.5f
        }
        val handling = baseHandling + (vehicle.tiresStage - 1) * 0.4f

        val baseBraking = 32f + (vehicle.brakesStage - 1) * 6f
        val baseNitroCap = 1.0f + (vehicle.nitroStage - 1) * 0.3f

        return VehiclePerformanceSpec(
            topSpeedKmh = topSpeed,
            accelerationPower = accel,
            handlingResponsiveness = handling,
            brakingPower = baseBraking,
            nitroCapacity = baseNitroCap
        )
    }

    fun stepPhysics(
        state: CarPhysicsState,
        spec: VehiclePerformanceSpec,
        track: TrackDef,
        throttleInput: Float, // 0.0..1.0
        brakeInput: Float, // 0.0..1.0
        handbrakeInput: Boolean,
        steeringInput: Float, // -1.0..1.0
        nitroInput: Boolean,
        dt: Float, // Frame time step in seconds (e.g. ~0.016s)
        currentTimeMs: Long
    ): CarPhysicsState {
        if (state.isFinished) return state

        var heading = state.headingAngle
        var vx = state.velocity.x
        var vy = state.velocity.y
        var currentSpeedMs = sqrt(vx * vx + vy * vy)
        var speedKmh = currentSpeedMs * 3.6f

        // Nitro handling
        var nitroRem = state.nitroRemaining
        var isNitroActive = false
        var enginePower = spec.accelerationPower

        if (nitroInput && nitroRem > 0.05f && throttleInput > 0.1f) {
            isNitroActive = true
            enginePower *= 2.2f
            nitroRem = max(0f, nitroRem - 0.35f * dt)
        } else if (state.isDrifting) {
            // Recharge nitro slowly during continuous drifts
            nitroRem = min(spec.nitroCapacity, nitroRem + 0.08f * dt)
        }

        // Speed cap
        val effectiveTopSpeedKmh = if (isNitroActive) spec.topSpeedKmh + 45f else spec.topSpeedKmh
        val maxSpeedMs = effectiveTopSpeedKmh / 3.6f

        // Heading unit vector
        val forwardX = cos(heading)
        val forwardY = sin(heading)
        val rightX = -sin(heading)
        val rightY = cos(heading)

        // Decompose velocity into forward & lateral components
        var forwardVel = vx * forwardX + vy * forwardY
        var lateralVel = vx * rightX + vy * rightY

        // Accelerate or Brake
        if (throttleInput > 0f) {
            val powerRatio = max(0.1f, 1.0f - (forwardVel / maxSpeedMs))
            forwardVel += enginePower * powerRatio * throttleInput * dt
        }
        if (brakeInput > 0f) {
            val brakeForce = spec.brakingPower * brakeInput * dt
            if (forwardVel > 0f) {
                forwardVel = max(0f, forwardVel - brakeForce)
            } else {
                forwardVel = min(0f, forwardVel + brakeForce * 0.5f) // Reverse
            }
        }

        // Air Drag & Friction
        val surfaceGrip = track.surfaceFriction
        val drag = 0.0012f * forwardVel * forwardVel
        val rollingFriction = 3.5f * dt
        if (forwardVel > 0) {
            forwardVel = max(0f, forwardVel - drag * dt - rollingFriction)
        } else if (forwardVel < 0) {
            forwardVel = min(0f, forwardVel + drag * dt + rollingFriction)
        }

        // Steering logic
        val speedFactor = min(1.0f, forwardVel / (30f / 3.6f)) // Turn responsiveness builds with speed
        val turnRate = spec.handlingResponsiveness * steeringInput * speedFactor * dt
        if (abs(forwardVel) > 0.5f) {
            heading += turnRate * (if (forwardVel >= 0) 1f else -1f)
        }

        // Lateral grip & Drifting physics
        val gripThreshold = 18f * surfaceGrip
        val slipAngle = atan2(lateralVel, max(0.1f, abs(forwardVel)))
        var isDrifting = false

        val triggerDrift = handbrakeInput || (abs(lateralVel) > gripThreshold && abs(steeringInput) > 0.4f)
        val gripDamping = if (triggerDrift) {
            isDrifting = true
            1.8f * surfaceGrip // Slides naturally sideways
        } else {
            12.0f * surfaceGrip // High tire grip locks back to line
        }

        lateralVel -= lateralVel * min(1.0f, gripDamping * dt)

        // Reconstruct velocity
        vx = forwardX * forwardVel + rightX * lateralVel
        vy = forwardY * forwardVel + rightY * lateralVel
        currentSpeedMs = sqrt(vx * vx + vy * vy)
        speedKmh = currentSpeedMs * 3.6f

        // New Position
        var px = state.position.x + vx * dt * 25f // Scale factor for canvas coordinates
        var py = state.position.y + vy * dt * 25f

        // Track boundary & Collision physics
        val trackCheck = checkTrackBounds(Vector2D(px, py), track)
        val particlesList = state.particles.map { it.copy(alpha = it.alpha - 2.0f * dt) }
            .filter { it.alpha > 0.05f }.toMutableList()

        if (trackCheck.isOffTrack) {
            // Off road friction penalty
            forwardVel *= (1f - 0.25f * dt)
            if (trackCheck.distanceFromTrack > (track.trackWidth / 2f + 30f)) {
                // Wall Bounce
                px = state.position.x - vx * dt * 10f
                py = state.position.y - vy * dt * 10f
                vx *= -0.4f
                vy *= -0.4f
                // Spawn wall spark particles
                for (p in 0..3) {
                    particlesList.add(
                        Particle(
                            x = px, y = py,
                            vx = (Math.random().toFloat() - 0.5f) * 120f,
                            vy = (Math.random().toFloat() - 0.5f) * 120f,
                            colorHex = 0xFFF59E0BL, size = 10f
                        )
                    )
                }
            }
        }

        // Tire Smoke & Skidmarks
        val skidMarksList = state.skidMarks.map { it.copy(alpha = it.alpha - 0.4f * dt) }
            .filter { it.alpha > 0.05f }.toMutableList()

        if (isDrifting && speedKmh > 35f) {
            if (skidMarksList.size < 120) {
                skidMarksList.add(
                    SkidMarkPoint(
                        position = Vector2D(px, py),
                        alpha = 0.8f,
                        headingAngle = heading
                    )
                )
            }
            // Spawn tire smoke
            particlesList.add(
                Particle(
                    x = px - forwardX * 15f + (Math.random().toFloat() - 0.5f) * 10f,
                    y = py - forwardY * 15f + (Math.random().toFloat() - 0.5f) * 10f,
                    vx = -forwardX * 30f + (Math.random().toFloat() - 0.5f) * 20f,
                    vy = -forwardY * 30f + (Math.random().toFloat() - 0.5f) * 20f,
                    colorHex = 0x88E2E8F0L,
                    size = 14f
                )
            )
        }

        if (isNitroActive) {
            // Spawn nitro flame particles behind exhaust
            particlesList.add(
                Particle(
                    x = px - forwardX * 22f,
                    y = py - forwardY * 22f,
                    vx = -forwardX * 180f,
                    vy = -forwardY * 180f,
                    colorHex = 0xFF06B6D4L,
                    size = 16f
                )
            )
        }

        // Drift Score Combo Accumulator
        var driftScoreTotal = state.driftScoreTotal
        var currentCombo = state.currentDriftCombo
        if (isDrifting && speedKmh > 30f) {
            val driftAdd = (speedKmh * abs(slipAngle) * 8f * dt).toInt()
            currentCombo += driftAdd
            driftScoreTotal += driftAdd
        } else if (!isDrifting && currentCombo > 0) {
            currentCombo = 0
        }

        // RPM & Gear calculation for tachometer
        val rpm = min(8000f, max(1000f, (speedKmh % 50f) / 50f * 6000f + 1800f))
        val gear = min(6, max(1, (speedKmh / 45f).toInt() + 1))

        // Checkpoints & Laps
        var nextCheckpointIdx = state.checkpointIndex
        var currentLap = state.currentLap
        var isFinished = state.isFinished
        var lapStartTime = state.lapStartTimeMs
        var currentLapTime = state.currentLapTimeMs
        var lastLapTime = state.lastLapTimeMs
        var bestLapTime = state.bestLapTimeMs

        if (lapStartTime == 0L) {
            lapStartTime = currentTimeMs
        }
        currentLapTime = currentTimeMs - lapStartTime

        if (track.checkpoints.isNotEmpty()) {
            val targetCp = track.checkpoints[nextCheckpointIdx]
            val distToCp = Vector2D(px, py).distanceTo(targetCp.center)
            if (distToCp < targetCp.width) {
                // Checkpoint crossed!
                nextCheckpointIdx = (nextCheckpointIdx + 1) % track.checkpoints.size
                if (nextCheckpointIdx == 0) {
                    // Lap complete!
                    lastLapTime = currentLapTime
                    if (bestLapTime == 0L || lastLapTime < bestLapTime) {
                        bestLapTime = lastLapTime
                    }
                    if (currentLap >= track.totalLaps) {
                        isFinished = true
                    } else {
                        currentLap += 1
                        lapStartTime = currentTimeMs
                        currentLapTime = 0L
                    }
                }
            }
        }

        return state.copy(
            position = Vector2D(px, py),
            velocity = Vector2D(vx, vy),
            speedKmh = speedKmh,
            headingAngle = heading,
            steeringAngle = turnRate,
            isDrifting = isDrifting,
            driftAngle = slipAngle,
            driftScoreTotal = driftScoreTotal,
            currentDriftCombo = currentCombo,
            nitroRemaining = nitroRem,
            isNitroActive = isNitroActive,
            rpm = rpm,
            gear = gear,
            currentLap = currentLap,
            checkpointIndex = nextCheckpointIdx,
            lapStartTimeMs = lapStartTime,
            currentLapTimeMs = currentLapTime,
            lastLapTimeMs = lastLapTime,
            bestLapTimeMs = bestLapTime,
            isFinished = isFinished,
            skidMarks = skidMarksList,
            particles = particlesList
        )
    }

    private data class TrackBoundsResult(
        val isOffTrack: Boolean,
        val distanceFromTrack: Float
    )

    private fun checkTrackBounds(pos: Vector2D, track: TrackDef): TrackBoundsResult {
        if (track.nodes.size < 2) return TrackBoundsResult(false, 0f)
        var minDist = Float.MAX_VALUE

        for (i in track.nodes.indices) {
            val p1 = track.nodes[i]
            val p2 = track.nodes[(i + 1) % track.nodes.size]
            val dist = distanceToSegment(pos, p1, p2)
            if (dist < minDist) {
                minDist = dist
            }
        }

        val halfWidth = track.trackWidth / 2f
        val isOff = minDist > halfWidth
        return TrackBoundsResult(isOff, minDist)
    }

    private fun distanceToSegment(p: Vector2D, v: Vector2D, w: Vector2D): Float {
        val l2 = (v.x - w.x) * (v.x - w.x) + (v.y - w.y) * (v.y - w.y)
        if (l2 == 0f) return p.distanceTo(v)
        var t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2
        t = max(0f, min(1f, t))
        val proj = Vector2D(v.x + t * (w.x - v.x), v.y + t * (w.y - v.y))
        return p.distanceTo(proj)
    }
}
