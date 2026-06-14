package com.medtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.medtrack.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLog(log: MedicationLogEntity): Long

    @Update
    suspend fun updateLog(log: MedicationLogEntity)

    @Query("DELETE FROM medication_logs WHERE log_id = :id")
    suspend fun deleteLog(id: Long)

    @Query("SELECT * FROM medication_logs WHERE patient_id = :patientId ORDER BY taken_at DESC")
    fun observeLogsByPatient(patientId: Long): Flow<List<MedicationLogEntity>>
}

