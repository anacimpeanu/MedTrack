package com.medtrack.data.repository

import com.medtrack.data.local.dao.AppointmentsDao
import com.medtrack.data.local.dao.LogsDao
import com.medtrack.data.local.dao.NotificationsDao
import com.medtrack.data.local.dao.PatientsDao
import com.medtrack.data.local.dao.TreatmentPlanDao
import com.medtrack.data.local.dao.UsersDao
import com.medtrack.data.local.entity.AppointmentEntity
import com.medtrack.data.local.entity.MedicationEntity
import com.medtrack.data.local.entity.MedicationLogEntity
import com.medtrack.data.local.entity.MedicationScheduleEntity
import com.medtrack.data.local.entity.NotificationEntity
import com.medtrack.data.local.entity.PatientEntity
import com.medtrack.data.local.entity.PatientMedicationEntity
import com.medtrack.data.local.entity.UserEntity
import com.medtrack.domain.repository.MedTrackRepository
import kotlinx.coroutines.flow.Flow

class LocalMedTrackRepository(
    private val usersDao: UsersDao,
    private val patientsDao: PatientsDao,
    private val treatmentPlanDao: TreatmentPlanDao,
    private val logsDao: LogsDao,
    private val notificationsDao: NotificationsDao,
    private val appointmentsDao: AppointmentsDao
) : MedTrackRepository {
    override suspend fun addUser(user: UserEntity): Long = usersDao.insertUser(user)

    override suspend fun getUserByEmail(email: String): UserEntity? = usersDao.getUserByEmail(email)

    override suspend fun addPatient(patient: PatientEntity): Long = patientsDao.insertPatient(patient)

    override fun observePatientsByUser(userId: Long): Flow<List<PatientEntity>> =
        patientsDao.observePatientsByUser(userId)

    override suspend fun addMedication(medication: MedicationEntity): Long =
        treatmentPlanDao.insertMedication(medication)

    override suspend fun addPatientMedication(plan: PatientMedicationEntity): Long =
        treatmentPlanDao.insertPatientMedication(plan)

    override fun observeActivePlans(patientId: Long): Flow<List<PatientMedicationEntity>> =
        treatmentPlanDao.observeActiveMedicationPlans(patientId)

    override suspend fun addSchedule(schedule: MedicationScheduleEntity): Long =
        treatmentPlanDao.insertSchedule(schedule)

    override fun observeSchedules(patientMedicationId: Long): Flow<List<MedicationScheduleEntity>> =
        treatmentPlanDao.observeSchedules(patientMedicationId)

    override suspend fun addLog(log: MedicationLogEntity): Long = logsDao.insertLog(log)

    override fun observeLogsByPatient(patientId: Long): Flow<List<MedicationLogEntity>> =
        logsDao.observeLogsByPatient(patientId)

    override suspend fun addNotification(notification: NotificationEntity): Long =
        notificationsDao.insertNotification(notification)

    override fun observeNotifications(userId: Long): Flow<List<NotificationEntity>> =
        notificationsDao.observeNotifications(userId)

    override suspend fun addAppointment(appointment: AppointmentEntity): Long =
        appointmentsDao.insertAppointment(appointment)

    override fun observeAppointments(patientId: Long): Flow<List<AppointmentEntity>> =
        appointmentsDao.observeAppointments(patientId)
}


