package com.medtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Long = 0,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    val email: String,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    val phone: String? = null,
    @ColumnInfo(name = "profile_role")
    val profileRole: String = "patient",
    @ColumnInfo(name = "profile_photo_uri")
    val profilePhotoUri: String? = null,
    val age: Int? = null,
    @ColumnInfo(name = "birth_date")
    val birthDate: String? = null,
    val cnp: String? = null,
    @ColumnInfo(name = "height_cm")
    val heightCm: Int? = null,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Double? = null,
    val sex: String? = null,
    @ColumnInfo(name = "profile_completed")
    val profileCompleted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)

