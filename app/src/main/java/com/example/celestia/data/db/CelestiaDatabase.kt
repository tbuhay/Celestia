package com.example.celestia.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.IssReading

@Database(entities = [KpReading::class, IssReading::class], version = 4, exportSchema = false)
abstract class CelestiaDatabase : RoomDatabase() {

    abstract fun dao(): CelestiaDao

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