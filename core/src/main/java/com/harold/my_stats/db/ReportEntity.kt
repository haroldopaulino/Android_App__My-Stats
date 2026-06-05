package com.harold.my_stats.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debug_reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val createdAtEpochMs: Long,
    val payloadJson: String,
    val uploaded: Boolean = false,
    val uploadAttemptCount: Int = 0,
    val lastError: String? = null
)
