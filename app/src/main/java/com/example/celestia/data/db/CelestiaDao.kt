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
 * **Data Access Object (DAO) for Celestia’s local Room database.**
 *
 * This interface defines all structured read/write operations for Celestia’s
 * offline cache layer, including:
 *
 * - **Kp Index readings** (NOAA)
 * - **ISS position** and metadata
 * - **NASA asteroid approach data**
 * - **Lunar phase calculations**
 * - **User-created Observation Journal entries**
 *
 * All retrieval methods return **Flow**, enabling real-time UI updates whenever
 * the database changes—no polling or manual refresh needed.
 *
 * Insert operations default to `REPLACE`, ensuring the database always
 * contains the most recent authoritative API results.
 */
@Dao
interface CelestiaDao {

    // -------------------------------------------------------------------------
    // KP INDEX (NOAA) — kp_readings
    // -------------------------------------------------------------------------

    /**
     * Inserts or replaces a full batch of Kp Index readings.
     *
     * @param readings List of readings returned from the NOAA API.
     */

    /**
     * Inserts or replaces a full batch of Kp Index readings.
     *
     * Presentation note:
     * - When the primary key used to be an auto-generated integer,
     *   this insert method created duplicates on every refresh.
     *
     * - After switching the KpReading primary key to the timestamp,
     *   this REPLACE strategy now keeps the table clean and stable
     *   (usually ~400 rows instead of 180,000).
     *
     * - This solved the lag and performance issues in Milestone 2.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<KpReading>)

    /**
     * Returns all stored Kp readings ordered from newest → oldest.
     *
     * @return Flow that emits whenever readings are inserted or cleared.
     */
    @Query("SELECT * FROM kp_readings ORDER BY timestamp DESC")
    fun getAll(): Flow<List<KpReading>>

    /**
     * Clears all Kp Index entries.
     */
    @Query("DELETE FROM kp_readings")
    suspend fun clearKpReadings()

    // -------------------------------------------------------------------------
    // ISS LOCATION — iss_reading
    // -------------------------------------------------------------------------

    /**
     * Retrieves the most recent ISS reading.
     *
     * @return Flow emitting the latest [IssReading], or `null` if empty.
     */
    @Query("SELECT * FROM iss_reading LIMIT 1")
    fun getIssReading(): Flow<IssReading?>

    /**
     * Saves the latest ISS reading (overwrites existing row).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssReading(reading: IssReading)

    /**
     * Removes the stored ISS data.
     */
    @Query("DELETE FROM iss_reading")
    suspend fun clearIssReadings()

    // -------------------------------------------------------------------------
    // ASTEROID APPROACHES — asteroid_approaches
    // -------------------------------------------------------------------------

    /**
     * Inserts or replaces a list of asteroid close-approach objects.
     *
     * @param asteroids Parsed list from NASA NEO feed.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsteroids(asteroids: List<AsteroidApproach>)

    /**
     * Deletes all asteroid records.
     */
    @Query("DELETE FROM asteroid_approaches")
    suspend fun clearAsteroids()

    /**
     * Returns all stored asteroid approaches sorted by upcoming date.
     *
     * @return Flow emitting a chronologically ascending list.
     */
    @Query("SELECT * FROM asteroid_approaches ORDER BY approachDate ASC")
    fun getAllAsteroids(): Flow<List<AsteroidApproach>>

    /**
     * Returns the **soonest upcoming asteroid** (used in dashboard previews).
     *
     * @return Flow emitting one row, or `null` if table is empty.
     */
    @Query("SELECT * FROM asteroid_approaches ORDER BY approachDate ASC LIMIT 1")
    fun getNextAsteroid(): Flow<AsteroidApproach?>

    // -------------------------------------------------------------------------
    // LUNAR PHASE — lunar_phase
    // -------------------------------------------------------------------------

    /**
     * Retrieves the currently cached lunar phase dataset.
     *
     * @return Flow emitting the latest [LunarPhaseEntity] or null.
     */
    @Query("SELECT * FROM lunar_phase LIMIT 1")
    fun getLunarPhase(): Flow<LunarPhaseEntity?>

    /**
     * Inserts or replaces the lunar phase row (table designed to hold 1 record).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLunarPhase(entity: LunarPhaseEntity)

    /**
     * Clears the lunar_phase table.
     */
    @Query("DELETE FROM lunar_phase")
    suspend fun clearLunarPhase()

    // -------------------------------------------------------------------------
    // OBSERVATION JOURNAL — observation_entries
    // -------------------------------------------------------------------------

    /**
     * Inserts or replaces a single Observation Journal entry.
     *
     * Each entry receives a stable `id` from Room if inserted new, or replaces
     * an existing entry when editing.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(entry: ObservationEntry)

    /**
     * Returns all journal entries sorted newest → oldest.
     *
     * @return Flow emitting the full list of user-created observations.
     */
    @Query("SELECT * FROM observation_entries ORDER BY timestamp DESC")
    fun getAllObservations(): Flow<List<ObservationEntry>>

    /**
     * Retrieves a single Observation Entry by its primary key.
     *
     * @param id The unique ID of the journal entry.
     * @return The matching [ObservationEntry] or null.
     */
    @Query("SELECT * FROM observation_entries WHERE id = :id")
    suspend fun getObservationById(id: Int): ObservationEntry?

    /**
     * Deletes a single Observation Entry.
     *
     * Uses Room's `@Delete` to match by primary key.
     */
    @Delete
    suspend fun deleteObservation(entry: ObservationEntry)
}
