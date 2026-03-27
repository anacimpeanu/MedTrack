package com.medtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_schedule",
    foreignKeys = [
        ForeignKey(
            entity = PatientMedicationEntity::class,
            parentColumns = ["patient_medication_id"],
            childColumns = ["patient_medication_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patient_medication_id"]),
        Index(value = ["patient_medication_id", "intake_time", "days_of_week"], unique = true)
    ]
)
data class MedicationScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "schedule_id")
    val scheduleId: Long = 0,
    @ColumnInfo(name = "patient_medication_id")
    val patientMedicationId: Long,
    @ColumnInfo(name = "intake_time")
    val intakeTime: String,
    @ColumnInfo(name = "days_of_week")
    val daysOfWeek: String,
    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: String
)

