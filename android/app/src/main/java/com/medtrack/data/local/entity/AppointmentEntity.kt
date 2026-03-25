package com.medtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["patient_id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id", "appointment_date"])]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "appointment_id")
    val appointmentId: Long = 0,
    @ColumnInfo(name = "patient_id")
    val patientId: Long,
    @ColumnInfo(name = "doctor_name")
    val doctorName: String,
    val specialty: String? = null,
    @ColumnInfo(name = "appointment_date")
    val appointmentDate: String,
    val location: String? = null,
    val notes: String? = null
)

