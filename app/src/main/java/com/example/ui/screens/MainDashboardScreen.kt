package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.UserProfileEntity
import com.example.data.entity.VehicleEntity
import com.example.physics.PhysicsEngine
import com.example.ui.components.VehicleCanvasPreview
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NitroAmber
import com.example.ui.theme.RacingRed
import com.example.ui.theme.SoftLavender
import com.example.viewmodel.GameMode

@Composable
fun MainDashboardScreen(
    userProfile: UserProfileEntity?,
    selectedVehicle: VehicleEntity?,
    onStartRace: (GameMode) -> Unit,
    onNavigateGarage: () -> Unit,
    onNavigateMultiplayerLobby: () -> Unit,
    onNavigateTrackBuilder: () -> Unit,
    onNavigateLeaderboard: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberSlate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Header Profile Bar
            DriverHeaderBar(userProfile)

            // Badges row inspired by Professional Polish design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF31111D), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PHYSICS PRO",
                        color = Color(0xFFFFB2BE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF1D192B), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "6.2kW ACTIVE AERO",
                        color = SoftLavender,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Hero Showcase Section
            HeroShowcaseCard(
                vehicle = selectedVehicle,
                onNavigateGarage = onNavigateGarage
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Modes Grid Header
            Text(
                text = "RACE MODES & TRACKS",
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            // Game Mode Cards
            ModeCard(
                title = "ONLINE MULTIPLAYER RIVALS",
                subtitle = "Compete against live online racers on global tracks",
                icon = Icons.Default.Groups,
                accentColor = ElectricCyan,
                tag = "mode_multiplayer",
                onClick = { onStartRace(GameMode.ONLINE_MULTIPLAYER) }
            )

            ModeCard(
                title = "TIME ATTACK CHAMPIONSHIP",
                subtitle = "Beat track records & race against rival telemetry ghosts",
                icon = Icons.Default.Timer,
                accentColor = NitroAmber,
                tag = "mode_time_attack",
                onClick = { onStartRace(GameMode.TIME_ATTACK) }
            )

            ModeCard(
                title = "DRIFT KING CHALLENGE",
                subtitle = "Chain massive angle drifts to score maximum combo points",
                icon = Icons.Default.Speed,
                accentColor = RacingRed,
                tag = "mode_drift",
                onClick = { onStartRace(GameMode.DRIFT_CHALLENGE) }
            )

            ModeCard(
                title = "CUSTOM TRACK BUILDER",
                subtitle = "Design custom track layouts & publish online",
                icon = Icons.Default.Build,
                accentColor = GoldYellow,
                tag = "mode_track_builder",
                onClick = onNavigateTrackBuilder
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Quick Launch Floating Button
        Button(
            onClick = { onStartRace(GameMode.ONLINE_MULTIPLAYER) },
            colors = ButtonDefaults.buttonColors(containerColor = SoftLavender),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(56.dp)
                .testTag("quick_start_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Race Now",
                    tint = DeepPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ENTER MULTIPLAYER MATCH",
                    color = DeepPurple,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun DriverHeaderBar(profile: UserProfileEntity?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CarbonDark,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile?.driverName ?: "Apex Racer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(ElectricCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, ElectricCyan, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO",
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Rank",
                        tint = GoldYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rank Points: ${profile?.rankPoints ?: 1200} PTS",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Cash Display
            Row(
                modifier = Modifier
                    .background(Color(0xFF065F46), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Cash",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$${profile?.cash ?: 25000}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun HeroShowcaseCard(
    vehicle: VehicleEntity?,
    onNavigateGarage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable { onNavigateGarage() }
            .testTag("garage_showcase_card"),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SELECTED VEHICLE",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = vehicle?.name ?: "Apex GT-8",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Button(
                    onClick = onNavigateGarage,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Customize",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "GARAGE", color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Car Preview Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(CyberSlate, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (vehicle != null) {
                    VehicleCanvasPreview(
                        vehicle = vehicle,
                        headingAngleDegrees = 0f,
                        isEngineActive = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Specs Quick Progress
            if (vehicle != null) {
                val specs = PhysicsEngine.calculateSpecs(vehicle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpecBarItem("SPEED", "${specs.topSpeedKmh.toInt()} KM/H", specs.topSpeedKmh / 300f, ElectricCyan)
                    SpecBarItem("ACCEL", "${specs.accelerationPower.toInt()} HP", specs.accelerationPower / 35f, NitroAmber)
                    SpecBarItem("GRIP", "${(specs.handlingResponsiveness * 30).toInt()} %", specs.handlingResponsiveness / 4.5f, RacingRed)
                }
            }
        }
    }
}

@Composable
private fun SpecBarItem(
    label: String,
    valueStr: String,
    progress: Float,
    color: Color
) {
    Column(modifier = Modifier.width(100.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = valueStr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.DarkGray
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Select",
                tint = Color.Gray
            )
        }
    }
}
