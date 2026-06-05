package com.harold.my_stats.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReportEntity::class, AppSettingsEntity::class], version = 3, exportSchema = false)
abstract class MyStatsDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var instance: MyStatsDatabase? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context.applicationContext,
                            MyStatsDatabase::class.java,
                            "my_stats.db"
                        ).fallbackToDestructiveMigration().build()
                    }
                }
            }
        }

        fun get(): MyStatsDatabase = requireNotNull(instance)
    }
}
