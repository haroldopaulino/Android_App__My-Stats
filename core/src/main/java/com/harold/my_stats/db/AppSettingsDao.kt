package com.harold.my_stats.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun get(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: AppSettingsEntity)

    @Query("UPDATE app_settings SET endpointUrl = :endpointUrl, updatedAtEpochMs = :updatedAt WHERE id = 1")
    suspend fun updateEndpoint(endpointUrl: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE app_settings SET darkTheme = :darkTheme, updatedAtEpochMs = :updatedAt WHERE id = 1")
    suspend fun updateTheme(darkTheme: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE app_settings SET uploadIntervalMs = :uploadIntervalMs, updatedAtEpochMs = :updatedAt WHERE id = 1")
    suspend fun updateUploadInterval(uploadIntervalMs: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE app_settings SET totalSentEndpointCount = totalSentEndpointCount + 1, updatedAtEpochMs = :updatedAt WHERE id = 1")
    suspend fun incrementSentEndpointCount(updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE app_settings SET totalFailedEndpointCount = totalFailedEndpointCount + 1, updatedAtEpochMs = :updatedAt WHERE id = 1")
    suspend fun incrementFailedEndpointCount(updatedAt: Long = System.currentTimeMillis())
}
