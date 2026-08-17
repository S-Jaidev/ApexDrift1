package com.example.multiplayer

import com.example.physics.Vector2D
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnlineRival(
    val id: String,
    val name: String,
    val rankTitle: String, // e.g. "Apex Legend", "Master Drift", "Pro Speedster"
    val vehicleId: String,
    val primaryColorHex: Long,
    val position: Vector2D,
    val headingAngle: Float,
    val speedKmh: Float,
    val isDrifting: Boolean,
    val currentLap: Int,
    val checkpointIndex: Int,
    val lapProgressRatio: Float, // 0.0 to 1.0 for ranking
    val isGhost: Boolean = false
)

data class MultiplayerRoom(
    val roomCode: String,
    val trackId: String,
    val hostName: String,
    val maxPlayers: Int = 4,
    val currentPlayers: List<String>,
    val isReadyToStart: Boolean = false
)

object MultiplayerManager {

    private val _roomState = MutableStateFlow<MultiplayerRoom?>(null)
    val roomState: StateFlow<MultiplayerRoom?> = _roomState.asStateFlow()

    fun createRoom(trackId: String, hostName: String): String {
        val code = "APEX-" + (1000..9999).random()
        _roomState.value = MultiplayerRoom(
            roomCode = code,
            trackId = trackId,
            hostName = hostName,
            currentPlayers = listOf(hostName, "CyberRacer_99", "DriftKing_JP", "Viper_Apex")
        )
        return code
    }

    fun joinRoom(code: String, playerName: String): Boolean {
        val upperCode = code.uppercase().trim()
        _roomState.value = MultiplayerRoom(
            roomCode = upperCode,
            trackId = "tokyo_expressway",
            hostName = "Host_Driver",
            currentPlayers = listOf("Host_Driver", playerName, "NightHawk_R", "SpeedDemon")
        )
        return true
    }

    fun generateRivalsForTrack(trackId: String, playerVehicleId: String): List<OnlineRival> {
        val rivalPresets = listOf(
            OnlineRival(
                id = "rival_1",
                name = "CyberRacer_99",
                rankTitle = "Apex Legend",
                vehicleId = "car_hyperion",
                primaryColorHex = 0xFF7C3AEDL,
                position = Vector2D(0f, -40f),
                headingAngle = 0f,
                speedKmh = 0f,
                isDrifting = false,
                currentLap = 1,
                checkpointIndex = 0,
                lapProgressRatio = 0.02f
            ),
            OnlineRival(
                id = "rival_2",
                name = "DriftKing_JP",
                rankTitle = "Master Drifter",
                vehicleId = "car_phantom_drift",
                primaryColorHex = 0xFF0284C7L,
                position = Vector2D(-35f, 20f),
                headingAngle = 0f,
                speedKmh = 0f,
                isDrifting = false,
                currentLap = 1,
                checkpointIndex = 0,
                lapProgressRatio = 0.01f
            ),
            OnlineRival(
                id = "rival_3",
                name = "ThunderV8_Max",
                rankTitle = "Pro Speedster",
                vehicleId = "car_muscle_thunder",
                primaryColorHex = 0xFFD97706L,
                position = Vector2D(-70f, -20f),
                headingAngle = 0f,
                speedKmh = 0f,
                isDrifting = false,
                currentLap = 1,
                checkpointIndex = 0,
                lapProgressRatio = 0.00f
            )
        )
        return rivalPresets
    }

    fun updateRivalPositions(
        rivals: List<OnlineRival>,
        trackNodes: List<Vector2D>,
        dt: Float,
        playerSpeedKmh: Float
    ): List<OnlineRival> {
        if (trackNodes.size < 2) return rivals

        return rivals.mapIndexed { index, rival ->
            // Simulate realistic online AI/Ghost rival trajectory along track nodes with speed variance
            val baseSpeedKmh = when (index) {
                0 -> 185f + (Math.sin(System.currentTimeMillis() / 800.0).toFloat() * 15f)
                1 -> 175f + (Math.cos(System.currentTimeMillis() / 600.0).toFloat() * 20f)
                else -> 165f + (Math.sin(System.currentTimeMillis() / 1000.0).toFloat() * 12f)
            }
            val targetSpeed = baseSpeedKmh
            val speedMs = targetSpeed / 3.6f

            // Find target node based on current progress
            val totalNodes = trackNodes.size
            val currentIdx = rival.checkpointIndex
            val p1 = trackNodes[currentIdx]
            val p2 = trackNodes[(currentIdx + 1) % totalNodes]

            val dir = (p2 - p1).normalized()
            val targetHeading = dir.angle()

            var newPos = rival.position + dir * (speedMs * dt * 25f)

            var newCheckpointIdx = currentIdx
            var newLap = rival.currentLap
            if (newPos.distanceTo(p2) < 90f) {
                newCheckpointIdx = (currentIdx + 1) % totalNodes
                if (newCheckpointIdx == 0) {
                    newLap += 1
                }
            }

            val isDrifting = index == 1 && (newCheckpointIdx % 3 == 0)
            val lapProgress = (newLap - 1) + (newCheckpointIdx.toFloat() / totalNodes)

            rival.copy(
                position = newPos,
                headingAngle = targetHeading,
                speedKmh = targetSpeed,
                isDrifting = isDrifting,
                currentLap = newLap,
                checkpointIndex = newCheckpointIdx,
                lapProgressRatio = lapProgress
            )
        }
    }
}
