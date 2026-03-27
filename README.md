# MedTrack

SQLite-first database foundation for medication tracking.

## Database entities (v1)

- `users`
- `patients`
- `medications`
- `patient_medications`
- `medication_schedule`
- `medication_logs`
- `notifications`
- `appointments`

## Project structure

- `db/schema.sql` - full schema for local reference
- `db/migrations/001_init.sql` - initial migration
- `db/seeds/001_seed_minimal.sql` - minimal seed data
- `db/tests/001_integrity_checks.sql` - SQL assertions for DB integrity and seeded baseline
- `scripts/test_db.ps1` - PowerShell runner for migration + seed + integrity checks
- `scripts/validate_schema.py` - optional validator that creates a local SQLite DB and applies migration + seed

## Quick start

Run migration + seed with `sqlite3` (creates `medtrack_dev.db` in project root):

```powershell
sqlite3 .\medtrack_dev.db ".read .\db\migrations\001_init.sql"
sqlite3 .\medtrack_dev.db ".read .\db\seeds\001_seed_minimal.sql"
sqlite3 .\medtrack_dev.db "SELECT COUNT(*) AS tables FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';"
```

Expected result for first setup:

- `tables = 8`

Optional: run the Python validator instead:

```powershell
python .\scripts\validate_schema.py
```

## Reset DB (PowerShell)

Use this when you want a clean local database:

```powershell
Remove-Item .\medtrack_dev.db -ErrorAction SilentlyContinue
sqlite3 .\medtrack_dev.db ".read .\db\migrations\001_init.sql"
sqlite3 .\medtrack_dev.db ".read .\db\seeds\001_seed_minimal.sql"
sqlite3 .\medtrack_dev.db "SELECT COUNT(*) AS tables FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';"
```

## Test DB (PowerShell)

Run full database test flow (rebuild DB, apply migration + seed, run integrity checks):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test_db.ps1
```

Optional: pass a custom sqlite executable path if `sqlite3` is not in `PATH`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test_db.ps1 -SqlitePath "C:\path\to\sqlite3.exe"
```

## Notes

- Foreign keys are enabled and enforced.
- IDs use `INTEGER PRIMARY KEY` (SQLite rowid-backed primary keys).
- Timestamps are stored as ISO-like text via `CURRENT_TIMESTAMP`.

## Android MVVM + Compose Data Layer

An Android bootstrap is available in `android/` with:

- Room entities for: `Users`, `Patients`, `Medications`, `Patient_Medications`, `Medication_Schedule`, `Medication_Logs`, `Notifications`, `Appointments`
- Split DAOs by feature (`Patients`, `Treatment Plan`, `Logs`, etc.)
- `MedTrackRepository` abstraction + local Room implementation
- Separate ViewModels/screens for `Patients`, `Treatment Plan`, and `Logs`
- Gradle Wrapper files included in `android/gradle/wrapper`

Open `android/` in Android Studio and sync Gradle.
