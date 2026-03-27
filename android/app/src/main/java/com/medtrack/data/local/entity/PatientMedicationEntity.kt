package com.medtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patient_medications",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["patient_id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["medication_id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patient_id"]),
        Index(value = ["medication_id"]),
        Index(value = ["is_active"]),
        Index(value = ["patient_id", "medication_id", "start_date"], unique = true)
    ]
)
data class PatientMedicationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "patient_medication_id")
    val patientMedicationId: Long = 0,
    @ColumnInfo(name = "patient_id")
    val patientId: Long,
    @ColumnInfo(name = "medication_id")
    val medicationId: Long,
    @ColumnInfo(name = "dosage_amount")
    val dosageAmount: Double,
    @ColumnInfo(name = "dosage_unit")
    val dosageUnit: String,
    val frequency: String,
    @ColumnInfo(name = "start_date")
    val startDate: String,
    @ColumnInfo(name = "end_date")
    val endDate: String? = null,
    val instructions: String? = null,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)

