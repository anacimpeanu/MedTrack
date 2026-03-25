PRAGMA foreign_keys = ON;
BEGIN TRANSACTION;

INSERT INTO users (full_name, email, password_hash, phone)
VALUES ('Demo User', 'demo@medtrack.local', 'demo_hash_replace_me', '+40123456789');

INSERT INTO patients (user_id, full_name, birth_date, gender, blood_type, allergies, chronic_conditions)
VALUES (1, 'John Doe', '1980-05-12', 'male', 'O+', 'Penicillin', 'Hypertension');

INSERT INTO medications (name, description, type, manufacturer, default_dose_unit)
VALUES ('Atorvastatin', 'Cholesterol-lowering medication', 'tablet', 'Generic Pharma', 'mg');

INSERT INTO patient_medications (
    patient_id,
    medication_id,
    dosage_amount,
    dosage_unit,
    frequency,
    start_date,
    instructions,
    is_active
)
VALUES (1, 1, 20, 'mg', 'once_daily', '2026-03-25', 'Take after dinner', 1);

INSERT INTO medication_schedule (patient_medication_id, intake_time, days_of_week, reminder_enabled)
VALUES (1, '20:00', '1,2,3,4,5,6,0', 1);

INSERT INTO medication_logs (schedule_id, patient_id, taken_at, status, notes)
VALUES (1, 1, CURRENT_TIMESTAMP, 'taken', 'Initial seeded log entry');

INSERT INTO notifications (user_id, patient_id, schedule_id, title, message, status)
VALUES (1, 1, 1, 'Medication Reminder', 'Time to take Atorvastatin', 'sent');

INSERT INTO appointments (patient_id, doctor_name, specialty, appointment_date, location, notes)
VALUES (1, 'Dr. Ana Popescu', 'Cardiology', '2026-04-10 09:30:00', 'Central Clinic', 'Routine checkup');

COMMIT;

