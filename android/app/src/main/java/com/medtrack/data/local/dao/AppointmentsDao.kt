package com.medtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtrack.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Query("SELECT * FROM appointments WHERE patient_id = :patientId ORDER BY appointment_date")
    fun observeAppointments(patientId: Long): Flow<List<AppointmentEntity>>
}

