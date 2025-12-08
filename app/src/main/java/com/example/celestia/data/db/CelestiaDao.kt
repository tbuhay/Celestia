package com.example.celestia.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.data.model.IssReading
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.LunarPhaseEntity
import com.example.celestia.data.model.ObservationEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Celestia's local Room database.
 *
 * This interface provides all database operations for:
 * - Kp Index readings
 * - ISS location data
 * - Near-Earth object (asteroid) approach data
 * - Lunar phase information
 *
 * All read queries return Flows so the UI can automatically react
 * to database updates without manual refresh calls.
 */
@Dao
interface CelestiaDao {

    // -------------------------------------------------------------------------
    // KP INDEX READINGS
    // -------------------------------------------------------------------------

    /**
     * Inserts a list of Kp Index readings into the database.
     * Existing records with matching IDs or timestamps are replaced.
     *
     * @param readings List of KpReading objects fetched from NOAA API.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<KpReading>)

    /**
     * Retrieves all stored Kp Index readings, ordered newest first.
     *
     * @return A Flow emitting a list of KpReading objects.
     */
    @Query("SELECT * FROM kp_readings ORDER BY timestamp DESC")
    fun getAll(): Flow<List<KpReading>>

    /**
     * Deletes all Kp Index readings from the table.
     */
    @Query("DELETE FROM kp_readings")
    suspend fun clearKpReadings()

    // -------------------------------------------------------------------------
    // ISS LOCATION
    // -------------------------------------------------------------------------

    /**
     * Retrieves the most recent ISS reading.
     *
     * @return A Flow emitting the latest IssReading or null if none exists.
     */
    @Query("SELECT * FROM iss_reading LIMIT 1")
    fun getIssReading(): Flow<IssReading?>

    /**
     * Inserts (or replaces) the latest ISS position data.
     *
     * @param reading The most recent ISS reading from the API.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssReading(reading: IssReading)

    /**
     * Clears the ISS reading table.
     */
    @Query("DELETE FROM iss_reading")
    suspend fun clearIssReadings()

    // -------------------------------------------------------------------------
    // ASTEROIDS (NEO APPROACHES)
    // -------------------------------------------------------------------------

    /**
     * Inserts or replaces a list of asteroid approach data.
     *
     * @param asteroids List of AsteroidApproach objects returned from NASA API.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsteroids(asteroids: List<AsteroidApproach>)

    /**
     * Deletes all asteroid approach records.
     */
    @Query("DELETE FROM asteroid_approaches")
    suspend fun clearAsteroids()

    /**
     * Retrieves all asteroid approaches sorted by closest upcoming date.
     *
     * @return A Flow emitting a list of AsteroidApproach.
     */
    @Query("SELECT * FROM asteroid_approaches ORDER BY approachDate ASC")
    fun getAllAsteroids(): Flow<List<AsteroidApproach>>

    /**
     * Retrieves the next upcoming asteroid approach.
     *
     * @return A Flow emitting the closest future AsteroidApproach or null.
     */
    @Query("SELECT * FROM asteroid_approaches ORDER BY approachDate ASC LIMIT 1")
    fun getNextAsteroid(): Flow<AsteroidApproach?>

    // -------------------------------------------------------------------------
    // LUNAR PHASE
    // -------------------------------------------------------------------------

    /**
     * Retrieves the most recently stored lunar phase.
     *
     * @return A Flow emitting a LunarPhaseEntity or null.
     */
    @Query("SELECT * FROM lunar_phase LIMIT 1")
    fun getLunarPhase(): Flow<LunarPhaseEntity?>

    /**
     * Inserts or replaces the current lunar phase.
     *
     * @param entity A LunarPhaseEntity from the Astro API.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLunarPhase(entity: LunarPhaseEntity)

    /**
     * Clears the lunar phase table.
     */
    @Query("DELETE FROM lunar_phase")
    suspend fun clearLunarPhase()

    // -------------------------------------------------------------------------
    // OBSERVATION JOURNAL
    // -------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(entry: ObservationEntry)

    @Query("SELECT * FROM observation_entries ORDER BY timestamp DESC")
    fun getAllObservations(): Flow<List<ObservationEntry>>

    @Query("SELECT * FROM observation_entries WHERE id = :id")
    suspend fun getObservationById(id: Int): ObservationEntry?

    @Delete
    suspend fun deleteObservation(entry: ObservationEntry)
}
