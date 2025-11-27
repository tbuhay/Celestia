package com.example.celestia.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.data.model.IssReading
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.LunarPhaseEntity
import kotlinx.coroutines.flow.Flow

@Dao interface CelestiaDao {

    // ---------------- KP INDEX READINGS ----------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<KpReading>)

    @Query("SELECT * FROM kp_readings ORDER BY timestamp DESC")
    fun getAll(): Flow<List<KpReading>>

    @Query("DELETE FROM kp_readings")
    suspend fun clear()

    // Insert without overwriting older rows (prevents duplicates)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(readings: List<KpReading>)

    // Delete old rows using string-based ISO timestamp comparison
    @Query("DELETE FROM kp_readings WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: String)

    // ---------------- ISS LOCATION ----------------

    @Query("SELECT * FROM iss_reading LIMIT 1")
    fun getIssReading(): Flow<IssReading?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssReading(reading: IssReading)

    // ---------------- ASTEROIDS ----------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsteroids(asteroids: List<AsteroidApproach>)

    @Query("DELETE FROM asteroid_approaches")
    suspend fun clearAsteroids()

    @Query("SELECT * FROM asteroid_approaches ORDER BY approachDate ASC")
    fun getAllAsteroids(): Flow<List<AsteroidApproach>>

    @Query("SELECT * FROM asteroid_approaches ORDER BY approachDate ASC LIMIT 1")
    fun getNextAsteroid(): Flow<AsteroidApproach?>

    // ---------------- LUNAR PHASES ----------------

    @Query("SELECT * FROM lunar_phase LIMIT 1")
    fun getLunarPhase(): Flow<LunarPhaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLunarPhase(entity: LunarPhaseEntity)
}
