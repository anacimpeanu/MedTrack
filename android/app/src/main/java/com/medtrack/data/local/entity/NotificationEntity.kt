package com.medtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["patient_id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MedicationScheduleEntity::class,
            parentColumns = ["schedule_id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "status", "sent_at"]),
        Index(value = ["patient_id"]),
        Index(value = ["schedule_id"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    val notificationId: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "patient_id")
    val patientId: Long? = null,
    @ColumnInfo(name = "schedule_id")
    val scheduleId: Long? = null,
    val title: String,
    val message: String,
    @ColumnInfo(name = "sent_at")
    val sentAt: String,
    @ColumnInfo(name = "read_at")
    val readAt: String? = null,
    val status: String = "pending"
)

