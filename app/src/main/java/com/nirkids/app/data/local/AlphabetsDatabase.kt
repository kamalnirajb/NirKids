package com.nirkids.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nirkids.app.data.model.AlphabetEntity
import com.nirkids.app.data.model.ProgressEntity

@Database(entities = [AlphabetEntity::class, ProgressEntity::class], version = 2, exportSchema = true)
abstract class AlphabetsDatabase : RoomDatabase() {
    abstract fun alphabetDao(): AlphabetDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AlphabetsDatabase? = null

        fun getInstance(database: android.content.Context): AlphabetsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    database,
                    AlphabetsDatabase::class.java,
                    "alphabets_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
