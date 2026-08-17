package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CustomTrackEntity
import com.example.data.entity.TrackRecordEntity
import com.example.data.entity.UserProfileEntity
import com.example.data.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVehicles(vehicles: List<VehicleEntity>)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)
}

@Dao
interface TrackRecordDao {
    @Query("SELECT * FROM track_records WHERE trackId = :trackId ORDER BY bestLapMs ASC LIMIT 10")
    fun getTopRecordsForTrack(trackId: String): Flow<List<TrackRecordEntity>>

    @Query("SELECT * FROM track_records WHERE trackId = :trackId ORDER BY bestLapMs ASC LIMIT 1")
    suspend fun getBestRecordForTrack(trackId: String): TrackRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TrackRecordEntity)
}

@Dao
interface CustomTrackDao {
    @Query("SELECT * FROM custom_tracks ORDER BY dateCreated DESC")
    fun getAllCustomTracks(): Flow<List<CustomTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTrack(track: CustomTrackEntity)

    @Query("DELETE FROM custom_tracks WHERE id = :id")
    suspend fun deleteCustomTrack(id: String)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
