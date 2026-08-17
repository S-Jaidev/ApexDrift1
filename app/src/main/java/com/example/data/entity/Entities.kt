package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // Sports, Drift, Muscle, Hypercar, Offroad
    val isUnlocked: Boolean = false,
    val price: Int = 0,
    val primaryColorHex: Long = 0xFFDC2626L, // Red default
    val secondaryColorHex: Long = 0xFF1E293BL, // Slate dark default
    val rimColorHex: Long = 0xFFE2E8F0L, // Silver default
    val neonColorHex: Long = 0x00000000L, // None default
    val spoilerStyle: Int = 0, // 0 = None, 1 = Sport Wing, 2 = GT Carbon, 3 = Cyber Aero
    val liveryStyle: Int = 0, // 0 = Clean, 1 = Racing Stripes, 2 = Flames, 3 = Cyber Hex
    val engineStage: Int = 1,
    val turboStage: Int = 1,
    val tiresStage: Int = 1,
    val brakesStage: Int = 1,
    val nitroStage: Int = 1
)

@Entity(tableName = "track_records")
data class TrackRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackId: String,
    val vehicleId: String,
    val driverName: String,
    val bestLapMs: Long,
    val bestDriftScore: Int,
    val dateRecorded: Long = System.currentTimeMillis(),
    val ghostTelemetryJson: String = ""
)

@Entity(tableName = "custom_tracks")
data class CustomTrackEntity(
    @PrimaryKey val id: String,
    val name: String,
    val creatorName: String,
    val surfaceType: String, // Asphalt, Dirt, CyberGrid
    val nodesJson: String, // JSON list of points
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val driverName: String = "Apex Racer",
    val cash: Int = 15000,
    val rankPoints: Int = 1200, // E.g. Gold III Division
    val selectedVehicleId: String = "car_apex_gt",
    val steeringType: Int = 0 // 0 = Steering Wheel, 1 = Buttons, 2 = Tilt
)
