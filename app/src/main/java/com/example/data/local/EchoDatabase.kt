package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LevelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EchoDatabase : RoomDatabase() {

    abstract fun levelDao(): LevelDao

    companion object {
        @Volatile
        private var INSTANCE: EchoDatabase? = null

        fun getDatabase(context: Context): EchoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EchoDatabase::class.java,
                    "echo_game_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
