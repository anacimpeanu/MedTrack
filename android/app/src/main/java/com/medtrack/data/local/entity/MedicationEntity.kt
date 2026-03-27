package com.medtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    indices = [Index(value = ["name", "manufacturer"], unique = true)]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "medication_id")
    val medicationId: Long = 0,
    val name: String,
    val description: String? = null,
    val type: String? = null,
    val manufacturer: String? = null,
    @ColumnInfo(name = "default_dose_unit")
    val defaultDoseUnit: String? = null
)

