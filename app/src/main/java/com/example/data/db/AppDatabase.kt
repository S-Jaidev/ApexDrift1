package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CustomTrackDao
import com.example.data.dao.TrackRecordDao
import com.example.data.dao.UserProfileDao
import com.example.data.dao.VehicleDao
import com.example.data.entity.CustomTrackEntity
import com.example.data.entity.TrackRecordEntity
import com.example.data.entity.UserProfileEntity
import com.example.data.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        TrackRecordEntity::class,
        CustomTrackEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun trackRecordDao(): TrackRecordDao
    abstract fun customTrackDao(): CustomTrackDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apex_drift_game.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
