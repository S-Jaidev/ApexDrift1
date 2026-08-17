package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserProfileEntity
import com.example.data.entity.VehicleEntity
import com.example.physics.PhysicsEngine
import com.example.ui.components.VehicleCanvasPreview
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.NitroAmber
import com.example.ui.theme.RacingRed

@Composable
fun GarageScreen(
    vehicles: List<VehicleEntity>,
    userProfile: UserProfileEntity?,
    onSelectVehicle: (String) -> Unit,
    onBuyVehicle: (VehicleEntity) -> Unit,
    onUpgradePart: (vehicleId: String, partType: String) -> Unit,
    onSaveCustomization: (
        vehicleId: String,
        primaryHex: Long,
        secondaryHex: Long,
        rimHex: Long,
        neonHex: Long,
        spoiler: Int,
        livery: Int
    ) -> Unit,
    onBack: () -> Unit
) {
    val activeCarId = userProfile?.selectedVehicleId ?: "car_apex_gt"
    val activeVehicle = vehicles.find { it.id == activeCarId } ?: vehicles.firstOrNull()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Performance Upgrades, 1 = Paint & Aero

    // Color Pickers local state
    var primaryColorHex by remember(activeVehicle) { mutableLongStateOf(activeVehicle?.primaryColorHex ?: 0xFFDC2626L) }
    var secondaryColorHex by remember(activeVehicle) { mutableLongStateOf(activeVehicle?.secondaryColorHex ?: 0xFF0F172AL) }
    var rimColorHex by remember(activeVehicle) { mutableLongStateOf(activeVehicle?.rimColorHex ?: 0xFFF1F5F9L) }
    var neonColorHex by remember(activeVehicle) { mutableLongStateOf(activeVehicle?.neonColorHex ?: 0x00000000L) }
    var spoilerStyle by remember(activeVehicle) { mutableIntStateOf(activeVehicle?.spoilerStyle ?: 1) }
    var liveryStyle by remember(activeVehicle) { mutableIntStateOf(activeVehicle?.liveryStyle ?: 1) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberSlate)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("garage_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GARAGE & TUNING",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }

            // Cash Display
            Row(
                modifier = Modifier
                    .background(Color(0xFF065F46), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Cash",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$${userProfile?.cash ?: 0}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Vehicle Selector Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vehicles) { car ->
                val isSelected = car.id == activeCarId
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .clickable {
                            onSelectVehicle(car.id)
                        }
                        .testTag("vehicle_card_${car.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CarbonDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ElectricCyan) else null
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(CyberSlate, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            VehicleCanvasPreview(
                                vehicle = car,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = car.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Text(
                            text = car.category,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        if (!car.isUnlocked) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = GoldYellow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$${car.price}",
                                    color = GoldYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        } else if (isSelected) {
                            Text(
                                text = "ACTIVE",
                                color = ElectricCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Vehicle Stage Preview
        if (activeVehicle != null) {
            val previewCar = activeVehicle.copy(
                primaryColorHex = primaryColorHex,
                secondaryColorHex = secondaryColorHex,
                rimColorHex = rimColorHex,
                neonColorHex = neonColorHex,
                spoilerStyle = spoilerStyle,
                liveryStyle = liveryStyle
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 20.dp)
                    .background(CarbonDark, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                VehicleCanvasPreview(
                    vehicle = previewCar,
                    isEngineActive = true,
                    headingAngleDegrees = 0f
                )

                // Unlock/Buy Button if locked
                if (!activeVehicle.isUnlocked) {
                    val canAfford = (userProfile?.cash ?: 0) >= activeVehicle.price
                    Button(
                        onClick = { onBuyVehicle(activeVehicle) },
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldYellow),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .testTag("buy_vehicle_button")
                    ) {
                        Text(
                            text = if (canAfford) "BUY VEHICLE FOR $${activeVehicle.price}" else "NEED $${activeVehicle.price}",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customization & Tuning Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CarbonDark,
                contentColor = ElectricCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElectricCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("PERFORMANCE TUNING", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("PAINT & AERO", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                if (selectedTab == 0) {
                    // Performance Specs & Upgrades
                    val specs = PhysicsEngine.calculateSpecs(previewCar)

                    Text(text = "CURRENT STATS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    StatRow("TOP SPEED", "${specs.topSpeedKmh.toInt()} KM/H", specs.topSpeedKmh / 300f, ElectricCyan)
                    StatRow("ACCELERATION", "${specs.accelerationPower.toInt()} HP", specs.accelerationPower / 40f, NitroAmber)
                    StatRow("HANDLING GRIP", "${(specs.handlingResponsiveness * 30).toInt()} %", specs.handlingResponsiveness / 4.5f, RacingRed)
                    StatRow("BRAKING", "${specs.brakingPower.toInt()} BAR", specs.brakingPower / 60f, GoldYellow)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "MECHANICAL UPGRADES ($2,500 EACH)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    UpgradeRow("ENGINE TUNING", activeVehicle.engineStage, "engine") {
                        onUpgradePart(activeVehicle.id, "engine")
                    }
                    UpgradeRow("TURBOCHARGER", activeVehicle.turboStage, "turbo") {
                        onUpgradePart(activeVehicle.id, "turbo")
                    }
                    UpgradeRow("RACING TIRES", activeVehicle.tiresStage, "tires") {
                        onUpgradePart(activeVehicle.id, "tires")
                    }
                    UpgradeRow("SPORT BRAKES", activeVehicle.brakesStage, "brakes") {
                        onUpgradePart(activeVehicle.id, "brakes")
                    }
                    UpgradeRow("NITRO SYSTEM", activeVehicle.nitroStage, "nitro") {
                        onUpgradePart(activeVehicle.id, "nitro")
                    }
                } else {
                    // Paint & Aero Customizer
                    Text(text = "PRIMARY BODY PAINT", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ColorPickerRow(selectedHex = primaryColorHex) { primaryColorHex = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "ACCENT COLOR", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ColorPickerRow(selectedHex = secondaryColorHex) { secondaryColorHex = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "WHEEL RIMS COLOR", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ColorPickerRow(selectedHex = rimColorHex) { rimColorHex = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "UNDERGLOW NEON LIGHTS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    NeonPickerRow(selectedHex = neonColorHex) { neonColorHex = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "REAR SPOILER WING", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OptionPickerRow(
                        options = listOf("None", "Sport Wing", "GT Carbon", "Cyber Aero"),
                        selectedIdx = spoilerStyle
                    ) { spoilerStyle = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "DECAL LIVERY", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OptionPickerRow(
                        options = listOf("Clean", "Stripes", "Flames", "Cyber Hex"),
                        selectedIdx = liveryStyle
                    ) { liveryStyle = it }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onSaveCustomization(
                                activeVehicle.id,
                                primaryColorHex,
                                secondaryColorHex,
                                rimColorHex,
                                neonColorHex,
                                spoilerStyle,
                                liveryStyle
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_customization_button")
                    ) {
                        Text("APPLY CUSTOM STYLING", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, valStr: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = valStr, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(2.dp))
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
private fun UpgradeRow(
    title: String,
    currentStage: Int,
    tag: String,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Stage $currentStage / 5", color = ElectricCyan, fontSize = 11.sp)
            }

            if (currentStage < 5) {
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = NitroAmber),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("upgrade_${tag}_button")
                ) {
                    Text(text = "UPGRADE $2.5K", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            } else {
                Text(text = "MAXED OUT", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ColorPickerRow(selectedHex: Long, onSelect: (Long) -> Unit) {
    val palette = listOf(
        0xFFDC2626L, // Red
        0xFF06B6D4L, // Cyan
        0xFF10B981L, // Green
        0xFFF59E0BL, // Amber
        0xFF7C3AEDL, // Purple
        0xFF020617L, // Black
        0xFFF8FAFCL, // White
        0xFFEAB308L  // Gold
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        palette.forEach { colorHex ->
            val isSelected = colorHex == selectedHex
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(colorHex))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun NeonPickerRow(selectedHex: Long, onSelect: (Long) -> Unit) {
    val palette = listOf(
        0x00000000L, // None
        0xFF06B6D4L, // Cyan
        0xFF10B981L, // Emerald
        0xFFEC4899L, // Magenta
        0xFFF59E0BL  // Gold
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        palette.forEach { colorHex ->
            val isSelected = colorHex == selectedHex
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (colorHex == 0x00000000L) Color.DarkGray else Color(colorHex))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (colorHex == 0x00000000L && !isSelected) {
                    Text("Off", color = Color.Gray, fontSize = 10.sp)
                } else if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun OptionPickerRow(
    options: List<String>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, optionName ->
            val isSelected = index == selectedIdx
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isSelected) ElectricCyan else CarbonDark,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionName,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
