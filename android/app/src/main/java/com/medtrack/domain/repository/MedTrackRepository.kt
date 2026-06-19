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
    suspend fun getUserByEmail(email: String): UserEntity?
    suspend fun getUserById(userId: Long): UserEntity?
    suspend fun updateUser(user: UserEntity)

    suspend fun addPatient(patient: PatientEntity): Long
    suspend fun getPatientById(patientId: Long): PatientEntity?
    suspend fun updatePatient(patient: PatientEntity)

    fun observePatientsByUser(userId: Long): Flow<List<PatientEntity>>

    fun observePatientsByCaretaker(caretakerId: Long): Flow<List<PatientEntity>>

    fun observeAvailablePatients(): Flow<List<PatientEntity>>

    suspend fun assignCaretakerToPatient(
        patientId: Long,
        caretakerId: Long
    )

    suspend fun addMedication(medication: MedicationEntity): Long
    suspend fun addPatientMedication(plan: PatientMedicationEntity): Long
    fun observeActivePlans(patientId: Long): Flow<List<PatientMedicationEntity>>
    suspend fun getPatientMedicationById(id: Long): PatientMedicationEntity?
    suspend fun updatePatientMedication(item: PatientMedicationEntity)
    suspend fun deletePatientMedication(id: Long)

    suspend fun addSchedule(schedule: MedicationScheduleEntity): Long
    fun observeSchedules(patientMedicationId: Long): Flow<List<MedicationScheduleEntity>>

    suspend fun addLog(log: MedicationLogEntity): Long
    fun observeLogsByPatient(patientId: Long): Flow<List<MedicationLogEntity>>
    suspend fun updateLog(log: MedicationLogEntity)
    suspend fun deleteLog(id: Long)

    suspend fun addNotification(notification: NotificationEntity): Long
    fun observeNotifications(userId: Long): Flow<List<NotificationEntity>>

    suspend fun addAppointment(appointment: AppointmentEntity): Long
    fun observeAppointments(patientId: Long): Flow<List<AppointmentEntity>>
    suspend fun updateAppointmentStatus(
        appointmentId: Long,
        status: String
    )

    suspend fun updateMedicalProfile(
        patientId: Long,
        bloodType: String?,
        allergies: String?,
        chronicConditions: String?,
        emergencyContact: String?,
        emergencyPhone: String?,
        familyDoctor: String?,
        insuranceProvider: String?
    )

    suspend fun getUsersByIds(userIds: List<Long>): List<UserEntity>
}