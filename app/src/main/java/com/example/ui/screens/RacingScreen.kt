package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.VehicleEntity
import com.example.multiplayer.OnlineRival
import com.example.physics.CarPhysicsState
import com.example.physics.TrackDef
import com.example.physics.Vector2D
import com.example.ui.components.VehicleCanvasPreview
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NitroAmber
import com.example.ui.theme.RacingRed
import com.example.viewmodel.RaceResultSummary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RacingScreen(
    track: TrackDef,
    selectedVehicle: VehicleEntity?,
    carState: CarPhysicsState,
    onlineRivals: List<OnlineRival>,
    activeEmote: String?,
    raceResult: RaceResultSummary?,
    onTick: (
        dt: Float,
        throttle: Float,
        brake: Float,
        handbrake: Boolean,
        steer: Float,
        nitro: Boolean
    ) -> Unit,
    onSendEmote: (String) -> Unit,
    onExitRace: () -> Unit
) {
    var throttleInput by remember { mutableFloatStateOf(0f) }
    var brakeInput by remember { mutableFloatStateOf(0f) }
    var handbrakeInput by remember { mutableStateOf(false) }
    var steeringInput by remember { mutableFloatStateOf(0f) }
    var nitroInput by remember { mutableStateOf(false) }

    var isSteeringWheelDragging by remember { mutableStateOf(false) }
    var steeringWheelAngle by remember { mutableFloatStateOf(0f) }

    // Interactive Game Physics Frame Tick
    LaunchedEffect(Unit) {
        var lastTimeMs = System.currentTimeMillis()
        while (true) {
            withFrameMillis { currentFrameMs ->
                val dt = ((currentFrameMs - lastTimeMs) / 1000f).coerceIn(0.005f, 0.05f)
                lastTimeMs = currentFrameMs

                onTick(
                    dt,
                    throttleInput,
                    brakeInput,
                    handbrakeInput,
                    steeringInput,
                    nitroInput
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
    ) {
        // Main Driving Render Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("racing_canvas")
        ) {
            val canvasCenterX = size.width / 2f
            val canvasCenterY = size.height / 2f

            // Camera offset translates world coordinates to keep player car centered
            val cameraOffsetX = canvasCenterX - carState.position.x
            val cameraOffsetY = canvasCenterY - carState.position.y

            // 1. Render Track Surface & Boundaries
            drawTrackSurface(
                track = track,
                cameraX = cameraOffsetX,
                cameraY = cameraOffsetY
            )

            // 2. Render Skid Marks
            carState.skidMarks.forEach { mark ->
                drawCircle(
                    color = Color.Black.copy(alpha = mark.alpha * 0.7f),
                    radius = 8f,
                    center = Offset(mark.position.x + cameraOffsetX, mark.position.y + cameraOffsetY)
                )
            }

            // 3. Render Particles (Smoke & Sparks)
            carState.particles.forEach { p ->
                drawCircle(
                    color = Color(p.colorHex).copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x + cameraOffsetX, p.y + cameraOffsetY)
                )
            }

            // 4. Render Online Rivals
            onlineRivals.forEach { rival ->
                val rivalScreenX = rival.position.x + cameraOffsetX
                val rivalScreenY = rival.position.y + cameraOffsetY

                // Render rival marker car
                rotate(
                    degrees = (rival.headingAngle * 180f / PI.toFloat()),
                    pivot = Offset(rivalScreenX, rivalScreenY)
                ) {
                    drawRoundRect(
                        color = Color(rival.primaryColorHex).copy(alpha = 0.85f),
                        topLeft = Offset(rivalScreenX - 14f, rivalScreenY - 26f),
                        size = Size(28f, 52f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }

                // Rival Nameplate
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 28f
                        isFakeBoldText = true
                    }
                    drawText(
                        rival.name,
                        rivalScreenX - 60f,
                        rivalScreenY - 35f,
                        paint
                    )
                }
            }

            // 5. Render Player Vehicle in Center
            val playerAngleDegrees = (carState.headingAngle * 180f / PI.toFloat()) + 90f
            rotate(
                degrees = playerAngleDegrees,
                pivot = Offset(canvasCenterX, canvasCenterY)
            ) {
                // Neon Glow
                if (selectedVehicle?.neonColorHex != 0L && selectedVehicle?.neonColorHex != 0x00000000L) {
                    drawCircle(
                        color = Color(selectedVehicle!!.neonColorHex).copy(alpha = 0.6f),
                        radius = 45f,
                        center = Offset(canvasCenterX, canvasCenterY)
                    )
                }

                // Tires
                val carW = 32f
                val carH = 60f
                val rimCol = Color(selectedVehicle?.rimColorHex ?: 0xFFFFFFFFL)

                // Front wheels rotate with steering input!
                val frontWheelTurnDegrees = steeringInput * 25f
                rotate(degrees = frontWheelTurnDegrees, pivot = Offset(canvasCenterX - carW / 2f, canvasCenterY - carH * 0.35f)) {
                    drawRoundRect(Color.Black, Offset(canvasCenterX - carW / 2f - 4f, canvasCenterY - carH * 0.35f - 8f), Size(8f, 16f), CornerRadius(3f, 3f))
                }
                rotate(degrees = frontWheelTurnDegrees, pivot = Offset(canvasCenterX + carW / 2f, canvasCenterY - carH * 0.35f)) {
                    drawRoundRect(Color.Black, Offset(canvasCenterX + carW / 2f - 4f, canvasCenterY - carH * 0.35f - 8f), Size(8f, 16f), CornerRadius(3f, 3f))
                }

                // Rear Wheels
                drawRoundRect(Color.Black, Offset(canvasCenterX - carW / 2f - 4f, canvasCenterY + carH * 0.25f - 8f), Size(8f, 16f), CornerRadius(3f, 3f))
                drawRoundRect(Color.Black, Offset(canvasCenterX + carW / 2f - 4f, canvasCenterY + carH * 0.25f - 8f), Size(8f, 16f), CornerRadius(3f, 3f))

                // Main Body Shell
                drawRoundRect(
                    color = Color(selectedVehicle?.primaryColorHex ?: 0xFFDC2626L),
                    topLeft = Offset(canvasCenterX - carW / 2f, canvasCenterY - carH / 2f),
                    size = Size(carW, carH),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Windshield
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(canvasCenterX - carW * 0.35f, canvasCenterY - carH * 0.25f),
                    size = Size(carW * 0.7f, carH * 0.25f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Headlight Beams
                drawCircle(Color(0xFF38BDF8), center = Offset(canvasCenterX - 10f, canvasCenterY - carH / 2f), radius = 6f)
                drawCircle(Color(0xFF38BDF8), center = Offset(canvasCenterX + 10f, canvasCenterY - carH / 2f), radius = 6f)
            }
        }

        // --- HUD Overlay Layer ---
        // Top Left: Race Stats & Lap Times
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onExitRace,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CarbonDark.copy(alpha = 0.8f), CircleShape)
                        .testTag("exit_race_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .background(CarbonDark.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "LAP ${carState.currentLap} / ${track.totalLaps}",
                        color = ElectricCyan,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lap Timer Card
            Box(
                modifier = Modifier
                    .background(CarbonDark.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = formatTimeMs(carState.currentLapTimeMs),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    if (carState.bestLapTimeMs > 0) {
                        Text(
                            text = "BEST: ${formatTimeMs(carState.bestLapTimeMs)}",
                            color = GoldYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Top Right: Minimap Radar
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(100.dp)
                .background(CarbonDark.copy(alpha = 0.85f), CircleShape)
                .border(2.dp, ElectricCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val scale = 0.04f

                // Draw Track path on radar
                track.nodes.forEachIndexed { i, p ->
                    val nextP = track.nodes[(i + 1) % track.nodes.size]
                    val radarP1 = center + Offset((p.x - carState.position.x) * scale, (p.y - carState.position.y) * scale)
                    val radarP2 = center + Offset((nextP.x - carState.position.x) * scale, (nextP.y - carState.position.y) * scale)
                    drawLine(Color.Gray, radarP1, radarP2, strokeWidth = 2f)
                }

                // Player dot
                drawCircle(ElectricCyan, radius = 4f, center = center)

                // Rival dots
                onlineRivals.forEach { r ->
                    val rRadar = center + Offset((r.position.x - carState.position.x) * scale, (r.position.y - carState.position.y) * scale)
                    drawCircle(NitroAmber, radius = 3.5f, center = rRadar)
                }
            }
        }

        // Center Drift Combo Banner
        if (carState.isDrifting || carState.currentDriftCombo > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
                    .background(RacingRed.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "🔥 EPIC DRIFT! +${carState.currentDriftCombo} PTS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }

        // Quick Emote Overlay
        if (activeEmote != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(CarbonDark.copy(alpha = 0.9f), CircleShape)
                    .padding(24.dp)
            ) {
                Text(text = activeEmote, fontSize = 48.sp)
            }
        }

        // Bottom Left: Steering Wheel / Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive Touch Steering Wheel
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(CarbonDark.copy(alpha = 0.7f), CircleShape)
                    .border(3.dp, ElectricCyan, CircleShape)
                    .rotate(steeringWheelAngle)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                steeringWheelAngle = 0f
                                steeringInput = 0f
                            },
                            onDragCancel = {
                                steeringWheelAngle = 0f
                                steeringInput = 0f
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            steeringWheelAngle = (steeringWheelAngle + dragAmount.x * 0.8f).coerceIn(-90f, 90f)
                            steeringInput = steeringWheelAngle / 90f
                        }
                    }
                    .testTag("steering_wheel"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Steer",
                    tint = ElectricCyan,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Quick Emoji Emote Bar
            Column {
                listOf("🔥", "⚡", "🏁", "🏎️").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .padding(vertical = 2.dp)
                            .background(CarbonDark.copy(alpha = 0.8f), CircleShape)
                            .clickable { onSendEmote(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                }
            }
        }

        // Bottom Right: Pedals & Nitro Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Nitro Boost Button
            Button(
                onClick = { nitroInput = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (carState.isNitroActive) ElectricCyan else NitroAmber
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("nitro_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Nitro Boost",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Handbrake / Drift Button
                Button(
                    onClick = { handbrakeInput = !handbrakeInput },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (handbrakeInput) RacingRed else CarbonDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(60.dp, 70.dp)
                        .testTag("handbrake_button")
                ) {
                    Text("DRIFT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                }

                // Brake Pedal
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRed.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(60.dp, 70.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    brakeInput = if (event.changes.any { it.pressed }) 1.0f else 0.0f
                                }
                            }
                        }
                        .testTag("brake_pedal")
                ) {
                    Text("BRAKE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                }

                // Throttle Accelerator
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(70.dp, 80.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    throttleInput = if (event.changes.any { it.pressed }) 1.0f else 0.0f
                                }
                            }
                        }
                        .testTag("throttle_pedal")
                ) {
                    Text("GAS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }

        // Speedometer & Tachometer Center Bottom Gauge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .background(CarbonDark.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${carState.speedKmh.toInt()}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "KM/H", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "GEAR ${carState.gear}", color = NitroAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Tachometer RPM Bar
                LinearProgressIndicator(
                    progress = { (carState.rpm / 8000f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(160.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (carState.rpm > 6500f) RacingRed else ElectricCyan,
                    trackColor = Color.DarkGray
                )
            }
        }

        // Race Finished Result Modal Dialog
        if (carState.isFinished && raceResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonDark),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RACE FINISHED!",
                            color = GoldYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${raceResult.finalPosition}st PLACE PODIUM",
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Best Lap Time:", color = Color.Gray)
                            Text(formatTimeMs(raceResult.bestLapTimeMs), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Drift Points:", color = Color.Gray)
                            Text("+${raceResult.totalDriftScore} PTS", color = RacingRed, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Prize Purse Earned:", color = Color.Gray)
                            Text("+$${raceResult.cashEarned}", color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onExitRace,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("continue_after_race_button")
                        ) {
                            Text("CLAIM REWARDS & EXIT", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawTrackSurface(
    track: TrackDef,
    cameraX: Float,
    cameraY: Float
) {
    if (track.nodes.size < 2) return

    val trackColor = when (track.surfaceType) {
        "Dirt" -> Color(0xFF78350F)
        "CyberGrid" -> Color(0xFF0F172A)
        else -> Color(0xFF1E293B)
    }

    // 1. Draw outer track belt ribbon
    val halfW = track.trackWidth / 2f
    for (i in track.nodes.indices) {
        val p1 = track.nodes[i]
        val p2 = track.nodes[(i + 1) % track.nodes.size]

        val screenP1 = Offset(p1.x + cameraX, p1.y + cameraY)
        val screenP2 = Offset(p2.x + cameraX, p2.y + cameraY)

        // Asphalt/Dirt Road Segment
        drawLine(
            color = trackColor,
            start = screenP1,
            end = screenP2,
            strokeWidth = track.trackWidth
        )

        // Kerb outer border lines
        drawLine(
            color = if (track.surfaceType == "CyberGrid") ElectricCyan else Color(0xFFEF4444),
            start = screenP1,
            end = screenP2,
            strokeWidth = 10f
        )
    }

    // 2. Center dash lines
    for (i in track.nodes.indices) {
        val p1 = track.nodes[i]
        val p2 = track.nodes[(i + 1) % track.nodes.size]

        val screenP1 = Offset(p1.x + cameraX, p1.y + cameraY)
        val screenP2 = Offset(p2.x + cameraX, p2.y + cameraY)

        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = screenP1,
            end = screenP2,
            strokeWidth = 4f
        )
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = (ms % 1000) / 10
    return String.format("%02d:%02d.%02d", minutes, seconds, millis)
}
