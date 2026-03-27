package com.medtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtrack.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPatient(patient: PatientEntity): Long

    @Query("SELECT * FROM patients WHERE user_id = :userId ORDER BY full_name")
    fun observePatientsByUser(userId: Long): Flow<List<PatientEntity>>
}

