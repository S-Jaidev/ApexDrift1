package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.data.entity.VehicleEntity

@Composable
fun VehicleCanvasPreview(
    vehicle: VehicleEntity,
    modifier: Modifier = Modifier,
    headingAngleDegrees: Float = 0f,
    isEngineActive: Boolean = false,
    isNitroActive: Boolean = false
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val carWidth = size.width * 0.45f
        val carHeight = size.height * 0.75f

        rotate(degrees = headingAngleDegrees, pivot = Offset(centerX, centerY)) {
            // Neon Underglow
            if (vehicle.neonColorHex != 0x00000000L && vehicle.neonColorHex != 0L) {
                val neonColor = Color(vehicle.neonColorHex)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(neonColor.copy(alpha = 0.7f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = carHeight * 0.65f
                    ),
                    center = Offset(centerX, centerY),
                    radius = carHeight * 0.65f
                )
            }

            // Nitro Exhaust Flame
            if (isNitroActive) {
                val flamePath = Path().apply {
                    moveTo(centerX - carWidth * 0.25f, centerY + carHeight * 0.5f)
                    lineTo(centerX, centerY + carHeight * 0.85f)
                    lineTo(centerX + carWidth * 0.25f, centerY + carHeight * 0.5f)
                    close()
                }
                drawPath(
                    path = flamePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color.Transparent),
                        startY = centerY + carHeight * 0.5f,
                        endY = centerY + carHeight * 0.85f
                    )
                )
            }

            // Wheels / Tires
            val tireWidth = carWidth * 0.22f
            val tireHeight = carHeight * 0.24f
            val rimColor = Color(vehicle.rimColorHex)

            // Front Left & Right Tires
            drawTire(centerX - carWidth * 0.48f, centerY - carHeight * 0.35f, tireWidth, tireHeight, rimColor)
            drawTire(centerX + carWidth * 0.26f, centerY - carHeight * 0.35f, tireWidth, tireHeight, rimColor)
            // Rear Left & Right Tires
            drawTire(centerX - carWidth * 0.48f, centerY + carHeight * 0.15f, tireWidth, tireHeight, rimColor)
            drawTire(centerX + carWidth * 0.26f, centerY + carHeight * 0.15f, tireWidth, tireHeight, rimColor)

            // Main Car Body Shell
            val primaryColor = Color(vehicle.primaryColorHex)
            val secondaryColor = Color(vehicle.secondaryColorHex)

            val bodyPath = Path().apply {
                // Hood / Nose
                moveTo(centerX, centerY - carHeight * 0.48f)
                cubicTo(
                    centerX + carWidth * 0.42f, centerY - carHeight * 0.42f,
                    centerX + carWidth * 0.48f, centerY - carHeight * 0.15f,
                    centerX + carWidth * 0.46f, centerY + carHeight * 0.25f
                )
                // Rear Bumper
                cubicTo(
                    centerX + carWidth * 0.42f, centerY + carHeight * 0.46f,
                    centerX - carWidth * 0.42f, centerY + carHeight * 0.46f,
                    centerX - carWidth * 0.46f, centerY + carHeight * 0.25f
                )
                cubicTo(
                    centerX - carWidth * 0.48f, centerY - carHeight * 0.15f,
                    centerX - carWidth * 0.42f, centerY - carHeight * 0.42f,
                    centerX, centerY - carHeight * 0.48f
                )
                close()
            }

            // Draw Body
            drawPath(
                path = bodyPath,
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.85f), secondaryColor),
                    start = Offset(centerX - carWidth, centerY - carHeight),
                    end = Offset(centerX + carWidth, centerY + carHeight)
                )
            )

            // Body Outline / Highlight
            drawPath(
                path = bodyPath,
                color = Color.White.copy(alpha = 0.3f),
                style = Stroke(width = 3f)
            )

            // Livery Style Details
            when (vehicle.liveryStyle) {
                1 -> { // Racing Stripe
                    drawRect(
                        color = secondaryColor,
                        topLeft = Offset(centerX - carWidth * 0.08f, centerY - carHeight * 0.46f),
                        size = Size(carWidth * 0.16f, carHeight * 0.9f)
                    )
                }
                2 -> { // Cyber Flames
                    val flame = Path().apply {
                        moveTo(centerX, centerY - carHeight * 0.3f)
                        lineTo(centerX - carWidth * 0.3f, centerY + carHeight * 0.2f)
                        lineTo(centerX, centerY)
                        lineTo(centerX + carWidth * 0.3f, centerY + carHeight * 0.2f)
                        close()
                    }
                    drawPath(flame, color = Color(0xFFF59E0B))
                }
                3 -> { // Cyber Hex accents
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.5f),
                        radius = carWidth * 0.3f,
                        center = Offset(centerX, centerY)
                    )
                }
            }

            // Windshield & Cabin Roof
            val windshieldPath = Path().apply {
                moveTo(centerX - carWidth * 0.3f, centerY - carHeight * 0.18f)
                lineTo(centerX + carWidth * 0.3f, centerY - carHeight * 0.18f)
                lineTo(centerX + carWidth * 0.25f, centerY + carHeight * 0.08f)
                lineTo(centerX - carWidth * 0.25f, centerY + carHeight * 0.08f)
                close()
            }
            drawPath(
                path = windshieldPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )

            // Roof
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(centerX - carWidth * 0.24f, centerY - carHeight * 0.06f),
                size = Size(carWidth * 0.48f, carHeight * 0.22f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Headlights Glow
            val headlightGlow = Brush.radialGradient(
                colors = listOf(Color(0xFF38BDF8), Color.Transparent),
                radius = 35f
            )
            drawCircle(brush = headlightGlow, center = Offset(centerX - carWidth * 0.32f, centerY - carHeight * 0.44f), radius = 25f)
            drawCircle(brush = headlightGlow, center = Offset(centerX + carWidth * 0.32f, centerY - carHeight * 0.44f), radius = 25f)

            // Rear Tail Lights Glow
            val tailLightColor = Color(0xFFEF4444)
            drawRoundRect(
                color = tailLightColor,
                topLeft = Offset(centerX - carWidth * 0.36f, centerY + carHeight * 0.43f),
                size = Size(carWidth * 0.22f, carHeight * 0.04f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = tailLightColor,
                topLeft = Offset(centerX + carWidth * 0.14f, centerY + carHeight * 0.43f),
                size = Size(carWidth * 0.22f, carHeight * 0.04f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Rear Spoiler
            if (vehicle.spoilerStyle > 0) {
                val spoilerWidth = when (vehicle.spoilerStyle) {
                    1 -> carWidth * 0.75f
                    2 -> carWidth * 0.9f
                    else -> carWidth * 1.05f
                }
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(centerX - spoilerWidth / 2f, centerY + carHeight * 0.44f),
                    size = Size(spoilerWidth, carHeight * 0.07f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
    }
}

private fun DrawScope.drawTire(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    rimColor: Color
) {
    // Rubber Tire Body
    drawRoundRect(
        color = Color(0xFF18181B),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Metallic Rim Center
    drawCircle(
        color = rimColor,
        center = Offset(x + w / 2f, y + h / 2f),
        radius = w * 0.28f
    )
}
