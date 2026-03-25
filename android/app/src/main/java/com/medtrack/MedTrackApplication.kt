package com.medtrack

import android.app.Application
import com.medtrack.data.local.MedTrackDatabase
import com.medtrack.data.repository.LocalMedTrackRepository
import com.medtrack.domain.repository.MedTrackRepository

class MedTrackApplication : Application() {
    lateinit var repository: MedTrackRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = MedTrackDatabase.getInstance(this)
        repository = LocalMedTrackRepository(
            usersDao = database.usersDao(),
            patientsDao = database.patientsDao(),
            treatmentPlanDao = database.treatmentPlanDao(),
            logsDao = database.logsDao(),
            notificationsDao = database.notificationsDao(),
            appointmentsDao = database.appointmentsDao()
        )
    }
}


