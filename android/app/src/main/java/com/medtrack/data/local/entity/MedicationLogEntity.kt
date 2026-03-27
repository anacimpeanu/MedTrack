package com.medtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationScheduleEntity::class,
            parentColumns = ["schedule_id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["patient_id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["schedule_id", "taken_at"]),
        Index(value = ["patient_id", "taken_at"])
    ]
)
data class MedicationLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val logId: Long = 0,
    @ColumnInfo(name = "schedule_id")
    val scheduleId: Long,
    @ColumnInfo(name = "patient_id")
    val patientId: Long,
    @ColumnInfo(name = "taken_at")
    val takenAt: String,
    val status: String,
    val notes: String? = null
)

