package com.example.data.repository

import com.example.data.dao.CustomTrackDao
import com.example.data.dao.TrackRecordDao
import com.example.data.dao.UserProfileDao
import com.example.data.dao.VehicleDao
import com.example.data.entity.CustomTrackEntity
import com.example.data.entity.TrackRecordEntity
import com.example.data.entity.UserProfileEntity
import com.example.data.entity.VehicleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GameRepository(
    private val vehicleDao: VehicleDao,
    private val trackRecordDao: TrackRecordDao,
    private val customTrackDao: CustomTrackDao,
    private val userProfileDao: UserProfileDao
) {
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    val allCustomTracks: Flow<List<CustomTrackEntity>> = customTrackDao.getAllCustomTracks()

    fun getTrackRecords(trackId: String): Flow<List<TrackRecordEntity>> =
        trackRecordDao.getTopRecordsForTrack(trackId)

    suspend fun initDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingProfile = userProfileDao.getUserProfileDirect()
        if (existingProfile == null) {
            userProfileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    driverName = "Apex Driver",
                    cash = 25000,
                    rankPoints = 1250,
                    selectedVehicleId = "car_apex_gt"
                )
            )
        }

        val defaultVehicles = listOf(
            VehicleEntity(
                id = "car_apex_gt",
                name = "Apex GT-8",
                category = "Sports R",
                isUnlocked = true,
                price = 0,
                primaryColorHex = 0xFFDC2626L,
                secondaryColorHex = 0xFF0F172AL,
                rimColorHex = 0xFFF1F5F9L,
                spoilerStyle = 1,
                liveryStyle = 1,
                engineStage = 1,
                turboStage = 1,
                tiresStage = 1,
                brakesStage = 1,
                nitroStage = 1
            ),
            VehicleEntity(
                id = "car_phantom_drift",
                name = "Phantom RX-7",
                category = "Drift Spec",
                isUnlocked = false,
                price = 15000,
                primaryColorHex = 0xFF2563EBL,
                secondaryColorHex = 0xFF1E1B4BL,
                rimColorHex = 0xFF94A3B8L,
                spoilerStyle = 2,
                liveryStyle = 3,
                engineStage = 2,
                turboStage = 2,
                tiresStage = 1,
                brakesStage = 2,
                nitroStage = 1
            ),
            VehicleEntity(
                id = "car_muscle_thunder",
                name = "Thunder V8 Interceptor",
                category = "Muscle",
                isUnlocked = false,
                price = 28000,
                primaryColorHex = 0xFFD97706L,
                secondaryColorHex = 0xFF18181BL,
                rimColorHex = 0xFFCBD5E1L,
                spoilerStyle = 0,
                liveryStyle = 2,
                engineStage = 3,
                turboStage = 1,
                tiresStage = 1,
                brakesStage = 1,
                nitroStage = 2
            ),
            VehicleEntity(
                id = "car_hyperion",
                name = "Hyperion Apex Horizon",
                category = "Hypercar",
                isUnlocked = false,
                price = 60000,
                primaryColorHex = 0xFF7C3AEDL,
                secondaryColorHex = 0xFF020617L,
                rimColorHex = 0xFFF8FAFCL,
                spoilerStyle = 3,
                neonColorHex = 0xFF06B6D4L,
                liveryStyle = 3,
                engineStage = 4,
                turboStage = 3,
                tiresStage = 3,
                brakesStage = 3,
                nitroStage = 3
            ),
            VehicleEntity(
                id = "car_rally_buggy",
                name = "Mojave Dirt King",
                category = "Off-Road Rally",
                isUnlocked = false,
                price = 20000,
                primaryColorHex = 0xFF16A34AL,
                secondaryColorHex = 0xFF292524L,
                rimColorHex = 0xFFE2E8F0L,
                spoilerStyle = 1,
                liveryStyle = 1,
                engineStage = 2,
                turboStage = 2,
                tiresStage = 3,
                brakesStage = 2,
                nitroStage = 1
            )
        )

        for (v in defaultVehicles) {
            val existing = vehicleDao.getVehicleById(v.id)
            if (existing == null) {
                vehicleDao.insertOrUpdateVehicles(listOf(v))
            }
        }

        // Add pre-loaded track benchmark records
        if (trackRecordDao.getBestRecordForTrack("tokyo_expressway") == null) {
            trackRecordDao.insertRecord(
                TrackRecordEntity(
                    trackId = "tokyo_expressway",
                    vehicleId = "car_hyperion",
                    driverName = "CyberRacer_99",
                    bestLapMs = 42150L,
                    bestDriftScore = 8450
                )
            )
            trackRecordDao.insertRecord(
                TrackRecordEntity(
                    trackId = "tokyo_expressway",
                    vehicleId = "car_phantom_drift",
                    driverName = "DriftKing_JP",
                    bestLapMs = 44800L,
                    bestDriftScore = 12900
                )
            )
            trackRecordDao.insertRecord(
                TrackRecordEntity(
                    trackId = "redwood_ridge",
                    vehicleId = "car_apex_gt",
                    driverName = "RedwoodSpeed",
                    bestLapMs = 51200L,
                    bestDriftScore = 6200
                )
            )
            trackRecordDao.insertRecord(
                TrackRecordEntity(
                    trackId = "sahara_dunes",
                    vehicleId = "car_rally_buggy",
                    driverName = "DuneMaster",
                    bestLapMs = 48900L,
                    bestDriftScore = 9100
                )
            )
        }
    }

    suspend fun updateVehicle(vehicle: VehicleEntity) = withContext(Dispatchers.IO) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun selectVehicle(vehicleId: String) = withContext(Dispatchers.IO) {
        val currentProfile = userProfileDao.getUserProfileDirect()
            ?: UserProfileEntity(id = 1)
        userProfileDao.insertOrUpdateProfile(currentProfile.copy(selectedVehicleId = vehicleId))
    }

    suspend fun updateProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun saveTrackRecord(record: TrackRecordEntity) = withContext(Dispatchers.IO) {
        trackRecordDao.insertRecord(record)
    }

    suspend fun saveCustomTrack(track: CustomTrackEntity) = withContext(Dispatchers.IO) {
        customTrackDao.insertCustomTrack(track)
    }

    suspend fun deleteCustomTrack(id: String) = withContext(Dispatchers.IO) {
        customTrackDao.deleteCustomTrack(id)
    }
}
