package com.example.celestia.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.IssReading
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.data.model.LunarPhaseEntity

@Database(entities = [KpReading::class, IssReading::class, AsteroidApproach::class, LunarPhaseEntity::class], version = 6, exportSchema = false)
abstract class CelestiaDatabase : RoomDatabase() {

    abstract fun celestiaDao(): CelestiaDao

    companion object {
        @Volatile private var INSTANCE: CelestiaDatabase? = null

        fun getInstance(context: Context): CelestiaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CelestiaDatabase::class.java,
                    "celestia_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}