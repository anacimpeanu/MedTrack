package com.medtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtrack.data.local.entity.MedicationEntity
import com.medtrack.data.local.entity.MedicationScheduleEntity
import com.medtrack.data.local.entity.PatientMedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentPlanDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPatientMedication(item: PatientMedicationEntity): Long

    @Query("SELECT * FROM patient_medications WHERE patient_id = :patientId AND is_active = 1 ORDER BY start_date DESC")
    fun observeActiveMedicationPlans(patientId: Long): Flow<List<PatientMedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedule(schedule: MedicationScheduleEntity): Long

    @Query("SELECT * FROM medication_schedule WHERE patient_medication_id = :patientMedicationId ORDER BY intake_time")
    fun observeSchedules(patientMedicationId: Long): Flow<List<MedicationScheduleEntity>>
}

