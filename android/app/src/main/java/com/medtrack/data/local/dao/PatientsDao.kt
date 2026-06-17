package com.medtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medtrack.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPatient(patient: PatientEntity): Long

    @Query("SELECT * FROM patients WHERE patient_id = :patientId LIMIT 1")
    suspend fun getPatientById(patientId: Long): PatientEntity?

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    // Pentru pacient: își vede propriul profil/patient record
    @Query("""
        SELECT * 
        FROM patients 
        WHERE user_id = :userId 
        ORDER BY full_name
    """)
    fun observePatientsByUser(userId: Long): Flow<List<PatientEntity>>

    // Pentru caretaker: vede doar pacienții luați în grijă de el
    @Query("""
        SELECT * 
        FROM patients 
        WHERE caretaker_id = :caretakerId 
        ORDER BY full_name
    """)
    fun observePatientsByCaretaker(caretakerId: Long): Flow<List<PatientEntity>>

    // Pentru Add patient la caretaker:
    // apar doar pacienții care nu au încă îngrijitor
    @Query("""
        SELECT * 
        FROM patients 
        WHERE caretaker_id IS NULL
        ORDER BY full_name
    """)
    fun observeAvailablePatients(): Flow<List<PatientEntity>>

    // Când caretaker apasă Add:
    // îl poate lua doar dacă încă nu are caretaker
    @Query("""
        UPDATE patients
        SET caretaker_id = :caretakerId
        WHERE patient_id = :patientId
        AND caretaker_id IS NULL
    """)
    suspend fun assignCaretakerToPatient(
        patientId: Long,
        caretakerId: Long
    )
}