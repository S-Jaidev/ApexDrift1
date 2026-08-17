package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.physics.TrackPresets
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.MultiplayerLobbyScreen
import com.example.ui.screens.RacingScreen
import com.example.ui.screens.TrackBuilderScreen
import com.example.ui.theme.ApexDriftTheme
import com.example.ui.theme.CharcoalDark
import com.example.viewmodel.GameMode
import com.example.viewmodel.RaceGameViewModel

enum class Screen {
    DASHBOARD,
    GARAGE,
    MULTIPLAYER_LOBBY,
    TRACK_BUILDER,
    RACING
}

class MainActivity : ComponentActivity() {

    private val viewModel: RaceGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApexDriftTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CharcoalDark)
                ) {
                    ApexDriftApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun ApexDriftApp(viewModel: RaceGameViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

    val userProfile by viewModel.userProfile.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val customTracks by viewModel.customTracks.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val selectedGameMode by viewModel.selectedGameMode.collectAsState()
    val racePhysicsState by viewModel.racePhysicsState.collectAsState()
    val onlineRivals by viewModel.onlineRivals.collectAsState()
    val activeEmote by viewModel.activeEmote.collectAsState()
    val raceResult by viewModel.raceResult.collectAsState()

    val activeCar = vehicles.find { it.id == userProfile?.selectedVehicleId } ?: vehicles.firstOrNull()

    when (currentScreen) {
        Screen.DASHBOARD -> {
            MainDashboardScreen(
                userProfile = userProfile,
                selectedVehicle = activeCar,
                onStartRace = { mode ->
                    viewModel.setGameMode(mode)
                    viewModel.startRace()
                    currentScreen = Screen.RACING
                },
                onNavigateGarage = { currentScreen = Screen.GARAGE },
                onNavigateMultiplayerLobby = { currentScreen = Screen.MULTIPLAYER_LOBBY },
                onNavigateTrackBuilder = { currentScreen = Screen.TRACK_BUILDER },
                onNavigateLeaderboard = { currentScreen = Screen.MULTIPLAYER_LOBBY }
            )
        }

        Screen.GARAGE -> {
            GarageScreen(
                vehicles = vehicles,
                userProfile = userProfile,
                onSelectVehicle = { id -> viewModel.selectVehicle(id) },
                onBuyVehicle = { car -> viewModel.buyVehicle(car) },
                onUpgradePart = { carId, part -> viewModel.upgradePart(carId, part) },
                onSaveCustomization = { carId, primary, secondary, rim, neon, spoiler, livery ->
                    viewModel.updateVehicleCustomization(carId, primary, secondary, rim, neon, spoiler, livery)
                },
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }

        Screen.MULTIPLAYER_LOBBY -> {
            MultiplayerLobbyScreen(
                userProfile = userProfile,
                activeRoom = null,
                onCreateRoom = { trackId -> viewModel.selectTrack(trackId) },
                onJoinRoom = { _ -> },
                onStartMatch = {
                    viewModel.setGameMode(GameMode.ONLINE_MULTIPLAYER)
                    viewModel.startRace()
                    currentScreen = Screen.RACING
                },
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }

        Screen.TRACK_BUILDER -> {
            TrackBuilderScreen(
                onSaveTrack = { name, surface, nodes ->
                    viewModel.createCustomTrack(name, surface, nodes)
                    currentScreen = Screen.MULTIPLAYER_LOBBY
                },
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }

        Screen.RACING -> {
            RacingScreen(
                track = selectedTrack,
                selectedVehicle = activeCar,
                carState = racePhysicsState,
                onlineRivals = onlineRivals,
                activeEmote = activeEmote,
                raceResult = raceResult,
                onTick = { dt, th, br, hb, st, ni ->
                    viewModel.tickPhysics(dt, th, br, hb, st, ni)
                },
                onSendEmote = { emote -> viewModel.sendEmote(emote) },
                onExitRace = {
                    currentScreen = Screen.DASHBOARD
                }
            )
        }
    }
}

