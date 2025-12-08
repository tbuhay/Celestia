package com.example.celestia.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.IssReading
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.data.model.LunarPhaseEntity
import com.example.celestia.data.model.ObservationEntry

/**
 * Main Room database for the Celestia application.
 *
 * This database stores:
 * - Kp Index readings
 * - ISS location data
 * - Near-Earth object (asteroid) approach information
 * - Lunar phase metadata
 *
 * The database uses a singleton pattern to ensure only one
 * instance exists throughout the application lifecycle.
 */
@Database(
    entities = [
        KpReading::class,
        IssReading::class,
        AsteroidApproach::class,
        LunarPhaseEntity::class,
        ObservationEntry::class
    ],
    version = 8,
    exportSchema = false
)
abstract class CelestiaDatabase : RoomDatabase() {

    /**
     * Provides access to all DAO operations for Celestia.
     *
     * @return The CelestiaDao instance used for queries and inserts.
     */
    abstract fun celestiaDao(): CelestiaDao

    companion object {
        @Volatile
        private var INSTANCE: CelestiaDatabase? = null

        /**
         * Retrieves the singleton instance of the Celestia database.
         *
         * If the database is not yet created, it will be built using
         * Room’s databaseBuilder and configured with
         * `fallbackToDestructiveMigration()` to handle schema version changes.
         *
         * @param context Application context (required by Room).
         * @return The singleton CelestiaDatabase instance.
         */
        fun getInstance(context: Context): CelestiaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CelestiaDatabase::class.java,
                    "celestia_db"
                )
                    // Automatically wipes and rebuilds DB if version changes.
                    // Safe for a student project; production apps should use
                    // proper migrations instead.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
