package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LaneEntity::class,
        LaneHistoryEntity::class,
        LaneLogEntity::class,
        ExtensionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LaneDatabase : RoomDatabase() {

    abstract fun laneDao(): LaneDao

    companion object {
        @Volatile
        private var INSTANCE: LaneDatabase? = null

        fun getDatabase(context: Context): LaneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LaneDatabase::class.java,
                    "lane_browser_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
