package com.medtrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Database(
    entities = [
        UserEntity::class,
        PatientEntity::class,
        MedicationEntity::class,
        PatientMedicationEntity::class,
        MedicationScheduleEntity::class,
        MedicationLogEntity::class,
        NotificationEntity::class,
        AppointmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MedTrackDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao
    abstract fun patientsDao(): PatientsDao
    abstract fun treatmentPlanDao(): TreatmentPlanDao
    abstract fun logsDao(): LogsDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun appointmentsDao(): AppointmentsDao

    companion object {
        @Volatile
        private var INSTANCE: MedTrackDatabase? = null

        fun getInstance(context: Context): MedTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MedTrackDatabase::class.java,
                    "medtrack.db"
                ).fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed a local owner account used by the demo ViewModel flow.
                            db.execSQL(
                                """
                                INSERT INTO users (user_id, full_name, email, password_hash, phone, created_at, updated_at)
                                VALUES (1, 'Demo User', 'demo@medtrack.local', 'local_seed_hash', '+40123456789', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                INSERT INTO patients (patient_id, user_id, full_name, birth_date, gender, blood_type, allergies, chronic_conditions, created_at)
                                VALUES (1, 1, 'Demo Patient', '1980-05-12', 'unspecified', 'O+', NULL, NULL, CURRENT_TIMESTAMP)
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                INSERT INTO medications (medication_id, name, description, type, manufacturer, default_dose_unit)
                                VALUES (1, 'Atorvastatin', 'Seed medication', 'tablet', 'Generic Pharma', 'mg')
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                INSERT INTO patient_medications (patient_medication_id, patient_id, medication_id, dosage_amount, dosage_unit, frequency, start_date, end_date, instructions, is_active)
                                VALUES (1, 1, 1, 20.0, 'mg', 'once_daily', '2026-03-25', NULL, 'Take after dinner', 1)
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                INSERT INTO medication_schedule (schedule_id, patient_medication_id, intake_time, days_of_week, reminder_enabled, created_at)
                                VALUES (1, 1, '20:00', '1,2,3,4,5,6,0', 1, CURRENT_TIMESTAMP)
                                """.trimIndent()
                            )
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}



