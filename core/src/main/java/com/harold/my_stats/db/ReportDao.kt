package com.harold.my_stats.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReportEntity): Long

    @Query("SELECT * FROM debug_reports WHERE uploaded = 0 ORDER BY createdAtEpochMs ASC LIMIT :limit")
    suspend fun getPending(limit: Int = 20): List<ReportEntity>

    @Query("SELECT * FROM debug_reports ORDER BY createdAtEpochMs DESC LIMIT 1")
    suspend fun latest(): ReportEntity?

    @Query("SELECT COUNT(*) FROM debug_reports WHERE uploaded = 0")
    suspend fun pendingCount(): Int

    @Query("SELECT payloadJson FROM debug_reports WHERE uploaded = 0 ORDER BY createdAtEpochMs ASC")
    suspend fun pendingPayloads(): List<String>

    @Query("SELECT COALESCE(SUM(LENGTH(payloadJson)), 0) FROM debug_reports WHERE uploaded = 0")
    suspend fun pendingStoredPayloadSizeBytes(): Long


    @Query("SELECT COUNT(*) FROM debug_reports")
    suspend fun localCount(): Int

    @Query("SELECT payloadJson FROM debug_reports ORDER BY createdAtEpochMs ASC")
    suspend fun localPayloads(): List<String>

    @Query("SELECT COUNT(*) FROM debug_reports")
    suspend fun totalCount(): Int

    @Query("UPDATE debug_reports SET uploaded = 1, lastError = NULL WHERE localId = :id")
    suspend fun markUploaded(id: Long)

    @Query("UPDATE debug_reports SET uploadAttemptCount = uploadAttemptCount + 1, lastError = :error WHERE localId = :id")
    suspend fun markFailed(id: Long, error: String?)

    @Query("DELETE FROM debug_reports WHERE localId = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM debug_reports WHERE localId NOT IN (SELECT localId FROM debug_reports ORDER BY createdAtEpochMs DESC LIMIT :maxRows)")
    suspend fun trimToMaxRows(maxRows: Int)
}
