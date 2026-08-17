package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.CustomTrackEntity
import com.example.data.entity.TrackRecordEntity
import com.example.data.entity.UserProfileEntity
import com.example.data.entity.VehicleEntity
import com.example.data.repository.GameRepository
import com.example.multiplayer.MultiplayerManager
import com.example.multiplayer.OnlineRival
import com.example.physics.CarPhysicsState
import com.example.physics.PhysicsEngine
import com.example.physics.TrackDef
import com.example.physics.TrackPresets
import com.example.physics.Vector2D
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GameMode {
    TIME_ATTACK,
    ONLINE_MULTIPLAYER,
    DRIFT_CHALLENGE,
    CUSTOM_TRACK
}

data class RaceResultSummary(
    val finalPosition: Int,
    val bestLapTimeMs: Long,
    val totalDriftScore: Int,
    val cashEarned: Int,
    val rankXpEarned: Int,
    val isNewRecord: Boolean
)

class RaceGameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = GameRepository(
        db.vehicleDao(),
        db.trackRecordDao(),
        db.customTrackDao(),
        db.userProfileDao()
    )

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val vehicles: StateFlow<List<VehicleEntity>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customTracks: StateFlow<List<CustomTrackEntity>> = repository.allCustomTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTrack = MutableStateFlow(TrackPresets.getPresetTracks().first())
    val selectedTrack: StateFlow<TrackDef> = _selectedTrack.asStateFlow()

    private val _selectedGameMode = MutableStateFlow(GameMode.ONLINE_MULTIPLAYER)
    val selectedGameMode: StateFlow<GameMode> = _selectedGameMode.asStateFlow()

    private val _racePhysicsState = MutableStateFlow(CarPhysicsState())
    val racePhysicsState: StateFlow<CarPhysicsState> = _racePhysicsState.asStateFlow()

    private val _onlineRivals = MutableStateFlow<List<OnlineRival>>(emptyList())
    val onlineRivals: StateFlow<List<OnlineRival>> = _onlineRivals.asStateFlow()

    private val _isRaceActive = MutableStateFlow(false)
    val isRaceActive: StateFlow<Boolean> = _isRaceActive.asStateFlow()

    private val _activeEmote = MutableStateFlow<String?>(null)
    val activeEmote: StateFlow<String?> = _activeEmote.asStateFlow()

    private val _raceResult = MutableStateFlow<RaceResultSummary?>(null)
    val raceResult: StateFlow<RaceResultSummary?> = _raceResult.asStateFlow()

    val currentTrackRecords: StateFlow<List<TrackRecordEntity>> = combine(
        _selectedTrack,
        repository.allVehicles
    ) { track, _ ->
        track.id
    }.combine(repository.allVehicles) { trackId, _ ->
        trackId
    }.let {
        repository.getTrackRecords(_selectedTrack.value.id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
        }
    }

    fun selectVehicle(vehicleId: String) {
        viewModelScope.launch {
            repository.selectVehicle(vehicleId)
        }
    }

    fun buyVehicle(vehicle: VehicleEntity) {
        val profile = userProfile.value ?: return
        if (profile.cash >= vehicle.price && !vehicle.isUnlocked) {
            viewModelScope.launch {
                val updatedProfile = profile.copy(
                    cash = profile.cash - vehicle.price,
                    selectedVehicleId = vehicle.id
                )
                repository.updateProfile(updatedProfile)
                repository.updateVehicle(vehicle.copy(isUnlocked = true))
            }
        }
    }

    fun upgradePart(vehicleId: String, partType: String) {
        val vehicle = vehicles.value.find { it.id == vehicleId } ?: return
        val profile = userProfile.value ?: return
        val cost = 2500

        if (profile.cash < cost) return

        val updatedVehicle = when (partType) {
            "engine" -> if (vehicle.engineStage < 5) vehicle.copy(engineStage = vehicle.engineStage + 1) else vehicle
            "turbo" -> if (vehicle.turboStage < 5) vehicle.copy(turboStage = vehicle.turboStage + 1) else vehicle
            "tires" -> if (vehicle.tiresStage < 5) vehicle.copy(tiresStage = vehicle.tiresStage + 1) else vehicle
            "brakes" -> if (vehicle.brakesStage < 5) vehicle.copy(brakesStage = vehicle.brakesStage + 1) else vehicle
            "nitro" -> if (vehicle.nitroStage < 5) vehicle.copy(nitroStage = vehicle.nitroStage + 1) else vehicle
            else -> vehicle
        }

        if (updatedVehicle != vehicle) {
            viewModelScope.launch {
                repository.updateProfile(profile.copy(cash = profile.cash - cost))
                repository.updateVehicle(updatedVehicle)
            }
        }
    }

    fun updateVehicleCustomization(
        vehicleId: String,
        primaryColorHex: Long,
        secondaryColorHex: Long,
        rimColorHex: Long,
        neonColorHex: Long,
        spoilerStyle: Int,
        liveryStyle: Int
    ) {
        val vehicle = vehicles.value.find { it.id == vehicleId } ?: return
        val updated = vehicle.copy(
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            rimColorHex = rimColorHex,
            neonColorHex = neonColorHex,
            spoilerStyle = spoilerStyle,
            liveryStyle = liveryStyle
        )
        viewModelScope.launch {
            repository.updateVehicle(updated)
        }
    }

    fun selectTrack(trackId: String) {
        val preset = TrackPresets.getPresetTracks().find { it.id == trackId }
        if (preset != null) {
            _selectedTrack.value = preset
        } else {
            val custom = customTracks.value.find { it.id == trackId }
            if (custom != null) {
                // parse custom nodes
                val parsedNodes = parseNodesJson(custom.nodesJson)
                _selectedTrack.value = TrackPresets.buildTrackDef(
                    id = custom.id,
                    name = custom.name,
                    description = "Custom User-Built Track",
                    surfaceType = custom.surfaceType,
                    surfaceFriction = if (custom.surfaceType == "Dirt") 0.65f else 1.0f,
                    nodes = parsedNodes
                )
            }
        }
    }

    fun setGameMode(mode: GameMode) {
        _selectedGameMode.value = mode
    }

    fun startRace() {
        val track = _selectedTrack.value
        val startPos = track.nodes.firstOrNull() ?: Vector2D(0f, 0f)
        val initialDir = if (track.nodes.size > 1) (track.nodes[1] - track.nodes[0]).normalized() else Vector2D(1f, 0f)

        _racePhysicsState.value = CarPhysicsState(
            position = startPos,
            headingAngle = initialDir.angle(),
            checkpointIndex = 0,
            currentLap = 1
        )
        _raceResult.value = null

        val currentCarId = userProfile.value?.selectedVehicleId ?: "car_apex_gt"
        if (_selectedGameMode.value == GameMode.ONLINE_MULTIPLAYER) {
            _onlineRivals.value = MultiplayerManager.generateRivalsForTrack(track.id, currentCarId)
        } else {
            _onlineRivals.value = emptyList()
        }

        _isRaceActive.value = true
    }

    fun tickPhysics(
        dt: Float,
        throttle: Float,
        brake: Float,
        handbrake: Boolean,
        steer: Float,
        nitro: Boolean
    ) {
        if (!_isRaceActive.value) return

        val profile = userProfile.value
        val activeVehicle = vehicles.value.find { it.id == profile?.selectedVehicleId }
            ?: vehicles.value.firstOrNull()
            ?: return

        val specs = PhysicsEngine.calculateSpecs(activeVehicle)
        val currentState = _racePhysicsState.value
        val track = _selectedTrack.value

        val newState = PhysicsEngine.stepPhysics(
            state = currentState,
            spec = specs,
            track = track,
            throttleInput = throttle,
            brakeInput = brake,
            handbrakeInput = handbrake,
            steeringInput = steer,
            nitroInput = nitro,
            dt = dt,
            currentTimeMs = System.currentTimeMillis()
        )

        _racePhysicsState.value = newState

        if (_selectedGameMode.value == GameMode.ONLINE_MULTIPLAYER) {
            val updatedRivals = MultiplayerManager.updateRivalPositions(
                rivals = _onlineRivals.value,
                trackNodes = track.nodes,
                dt = dt,
                playerSpeedKmh = newState.speedKmh
            )
            _onlineRivals.value = updatedRivals
        }

        if (newState.isFinished && _raceResult.value == null) {
            finishRace(newState)
        }
    }

    private fun finishRace(finalState: CarPhysicsState) {
        _isRaceActive.value = false
        val profile = userProfile.value ?: return

        // Calculate player rank & position among rivals
        val playerLapProgress = (finalState.currentLap - 1) + 1.0f
        val rivals = _onlineRivals.value
        var pos = 1
        for (r in rivals) {
            if (r.lapProgressRatio > playerLapProgress) {
                pos++
            }
        }

        val baseReward = when (pos) {
            1 -> 5000
            2 -> 3200
            3 -> 2000
            else -> 1200
        }
        val driftBonus = finalState.driftScoreTotal / 5
        val totalCash = baseReward + driftBonus
        val rankXp = when (pos) {
            1 -> 150
            2 -> 90
            3 -> 50
            else -> 20
        }

        val summary = RaceResultSummary(
            finalPosition = pos,
            bestLapTimeMs = finalState.bestLapTimeMs,
            totalDriftScore = finalState.driftScoreTotal,
            cashEarned = totalCash,
            rankXpEarned = rankXp,
            isNewRecord = true
        )
        _raceResult.value = summary

        viewModelScope.launch {
            val newCash = profile.cash + totalCash
            val newRank = profile.rankPoints + rankXp
            repository.updateProfile(profile.copy(cash = newCash, rankPoints = newRank))

            repository.saveTrackRecord(
                TrackRecordEntity(
                    trackId = _selectedTrack.value.id,
                    vehicleId = profile.selectedVehicleId,
                    driverName = profile.driverName,
                    bestLapMs = finalState.bestLapTimeMs,
                    bestDriftScore = finalState.driftScoreTotal
                )
            )
        }
    }

    fun sendEmote(emoji: String) {
        _activeEmote.value = emoji
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            _activeEmote.value = null
        }
    }

    fun createCustomTrack(name: String, surfaceType: String, nodes: List<Vector2D>) {
        val jsonStr = nodesToJson(nodes)
        val newTrack = CustomTrackEntity(
            id = "custom_" + System.currentTimeMillis(),
            name = name.ifBlank { "My Custom Track" },
            creatorName = userProfile.value?.driverName ?: "Apex Driver",
            surfaceType = surfaceType,
            nodesJson = jsonStr
        )
        viewModelScope.launch {
            repository.saveCustomTrack(newTrack)
        }
    }

    private fun nodesToJson(nodes: List<Vector2D>): String {
        return nodes.joinToString(";") { "${it.x},${it.y}" }
    }

    private fun parseNodesJson(json: String): List<Vector2D> {
        if (json.isBlank()) return emptyList()
        return try {
            json.split(";").mapNotNull {
                val parts = it.split(",")
                if (parts.size == 2) {
                    Vector2D(parts[0].toFloat(), parts[1].toFloat())
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
