PRAGMA foreign_keys = ON;

-- Users of the app (caregivers/accounts).
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    phone TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (instr(email, '@') > 1)
);

-- Keep updated_at in sync for user profile updates.
CREATE TRIGGER IF NOT EXISTS trg_users_updated_at
AFTER UPDATE ON users
FOR EACH ROW
WHEN NEW.updated_at = OLD.updated_at
BEGIN
    UPDATE users
    SET updated_at = CURRENT_TIMESTAMP
    WHERE user_id = OLD.user_id;
END;

-- Patients monitored by a user.
CREATE TABLE IF NOT EXISTS patients (
    patient_id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    full_name TEXT NOT NULL,
    birth_date TEXT,
    gender TEXT CHECK (gender IN ('male', 'female', 'other', 'unspecified')),
    blood_type TEXT CHECK (blood_type IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-')),
    allergies TEXT,
    chronic_conditions TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_patients_user_id
    ON patients(user_id);

-- Medication catalog shared by all users/patients.
CREATE TABLE IF NOT EXISTS medications (
    medication_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    type TEXT,
    manufacturer TEXT,
    default_dose_unit TEXT,
    UNIQUE (name, manufacturer)
);

-- Active or historical treatment assignment for a patient.
CREATE TABLE IF NOT EXISTS patient_medications (
    patient_medication_id INTEGER PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    medication_id INTEGER NOT NULL,
    dosage_amount REAL NOT NULL CHECK (dosage_amount > 0),
    dosage_unit TEXT NOT NULL,
    frequency TEXT NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT,
    instructions TEXT,
    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
    CHECK (end_date IS NULL OR end_date >= start_date),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (medication_id) REFERENCES medications(medication_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE (patient_id, medication_id, start_date)
);

CREATE INDEX IF NOT EXISTS idx_patient_medications_patient_id
    ON patient_medications(patient_id);

CREATE INDEX IF NOT EXISTS idx_patient_medications_medication_id
    ON patient_medications(medication_id);

CREATE INDEX IF NOT EXISTS idx_patient_medications_is_active
    ON patient_medications(is_active);

-- One treatment can have multiple intake moments.
CREATE TABLE IF NOT EXISTS medication_schedule (
    schedule_id INTEGER PRIMARY KEY,
    patient_medication_id INTEGER NOT NULL,
    intake_time TEXT NOT NULL,
    days_of_week TEXT NOT NULL,
    reminder_enabled INTEGER NOT NULL DEFAULT 1 CHECK (reminder_enabled IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_medication_id) REFERENCES patient_medications(patient_medication_id) ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE (patient_medication_id, intake_time, days_of_week)
);

CREATE INDEX IF NOT EXISTS idx_medication_schedule_patient_medication_id
    ON medication_schedule(patient_medication_id);

-- Adherence log for each scheduled intake.
CREATE TABLE IF NOT EXISTS medication_logs (
    log_id INTEGER PRIMARY KEY,
    schedule_id INTEGER NOT NULL,
    patient_id INTEGER NOT NULL,
    taken_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL CHECK (status IN ('taken', 'missed', 'skipped')),
    notes TEXT,
    FOREIGN KEY (schedule_id) REFERENCES medication_schedule(schedule_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_medication_logs_schedule_taken_at
    ON medication_logs(schedule_id, taken_at);

CREATE INDEX IF NOT EXISTS idx_medication_logs_patient_taken_at
    ON medication_logs(patient_id, taken_at);

-- Notifications sent to users for medication reminders.
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    patient_id INTEGER,
    schedule_id INTEGER,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    sent_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TEXT,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'sent', 'read', 'failed')),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON UPDATE CASCADE ON DELETE SET NULL,
    FOREIGN KEY (schedule_id) REFERENCES medication_schedule(schedule_id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_status_sent_at
    ON notifications(user_id, status, sent_at);

CREATE INDEX IF NOT EXISTS idx_notifications_patient_id
    ON notifications(patient_id);

-- Patient medical visits and checkups.
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INTEGER PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    doctor_name TEXT NOT NULL,
    specialty TEXT,
    appointment_date TEXT NOT NULL,
    location TEXT,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_appointments_patient_date
    ON appointments(patient_id, appointment_date);

