package com.medtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patients",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "patient_id")
    val patientId: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    @ColumnInfo(name = "birth_date")
    val birthDate: String? = null,
    val gender: String? = null,
    @ColumnInfo(name = "blood_type")
    val bloodType: String? = null,
    val allergies: String? = null,
    @ColumnInfo(name = "chronic_conditions")
    val chronicConditions: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String
)

