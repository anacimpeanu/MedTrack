package com.medtrack.domain.repository

import com.medtrack.data.local.entity.AppointmentEntity
import com.medtrack.data.local.entity.MedicationEntity
import com.medtrack.data.local.entity.MedicationLogEntity
import com.medtrack.data.local.entity.MedicationScheduleEntity
import com.medtrack.data.local.entity.NotificationEntity
import com.medtrack.data.local.entity.PatientEntity
import com.medtrack.data.local.entity.PatientMedicationEntity
import com.medtrack.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface MedTrackRepository {
    suspend fun addUser(user: UserEntity): Long
    suspend fun addPatient(patient: PatientEntity): Long
    fun observePatientsByUser(userId: Long): Flow<List<PatientEntity>>

    suspend fun addMedication(medication: MedicationEntity): Long
    suspend fun addPatientMedication(plan: PatientMedicationEntity): Long
    fun observeActivePlans(patientId: Long): Flow<List<PatientMedicationEntity>>

    suspend fun addSchedule(schedule: MedicationScheduleEntity): Long
    fun observeSchedules(patientMedicationId: Long): Flow<List<MedicationScheduleEntity>>

    suspend fun addLog(log: MedicationLogEntity): Long
    fun observeLogsByPatient(patientId: Long): Flow<List<MedicationLogEntity>>

    suspend fun addNotification(notification: NotificationEntity): Long
    fun observeNotifications(userId: Long): Flow<List<NotificationEntity>>

    suspend fun addAppointment(appointment: AppointmentEntity): Long
    fun observeAppointments(patientId: Long): Flow<List<AppointmentEntity>>
}

