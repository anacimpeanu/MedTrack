package com.medtrack.data.local.dao

import androidx.room.Dao

// Compatibility aggregate while feature-specific DAOs are used directly.
@Dao
interface MedTrackDao :
    UsersDao,
    PatientsDao,
    TreatmentPlanDao,
    LogsDao,
    NotificationsDao,
    AppointmentsDao


