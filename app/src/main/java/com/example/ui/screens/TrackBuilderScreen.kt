package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physics.Vector2D
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.RacingRed

@Composable
fun TrackBuilderScreen(
    onSaveTrack: (name: String, surfaceType: String, nodes: List<Vector2D>) -> Unit,
    onBack: () -> Unit
) {
    var trackName by remember { mutableStateOf("") }
    var selectedSurface by remember { mutableStateOf("Asphalt") }
    val nodes = remember { mutableStateListOf<Vector2D>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberSlate)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("track_builder_back")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("CUSTOM TRACK BUILDER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (nodes.size >= 3) {
                        onSaveTrack(trackName, selectedSurface, nodes)
                    }
                },
                enabled = nodes.size >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("save_track_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("SAVE TRACK", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = trackName,
                onValueChange = { trackName = it },
                placeholder = { Text("Track Name...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("track_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Button(
                onClick = { nodes.clear() },
                colors = ButtonDefaults.buttonColors(containerColor = RacingRed),
                modifier = Modifier.testTag("clear_track_button")
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas Track Node Builder Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CarbonDark, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        nodes.add(Vector2D(offset.x, offset.y))
                    }
                }
                .testTag("track_builder_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw grid guides
                for (x in 0..size.width.toInt() step 60) {
                    drawLine(Color.DarkGray.copy(alpha = 0.2f), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
                }
                for (y in 0..size.height.toInt() step 60) {
                    drawLine(Color.DarkGray.copy(alpha = 0.2f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                }

                // Draw track path lines
                if (nodes.size >= 2) {
                    for (i in nodes.indices) {
                        val p1 = nodes[i]
                        val p2 = nodes[(i + 1) % nodes.size]
                        drawLine(
                            color = ElectricCyan,
                            start = Offset(p1.x, p1.y),
                            end = Offset(p2.x, p2.y),
                            strokeWidth = 35f
                        )
                    }
                }

                // Draw node anchor points
                nodes.forEachIndexed { idx, n ->
                    drawCircle(
                        color = if (idx == 0) GoldYellow else Color.White,
                        radius = 12f,
                        center = Offset(n.x, n.y)
                    )
                }
            }

            if (nodes.isEmpty()) {
                Text(
                    text = "TAP ON CANVAS TO PLACE TRACK WAYPOINTS",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
