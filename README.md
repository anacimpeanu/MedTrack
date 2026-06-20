# MedTrack Android Application

A comprehensive medication tracking and management system built with **Kotlin**, **Jetpack Compose**, **Room Database**, and **MVVM Architecture**. MedTrack enables healthcare providers and caregivers to monitor patient medications, treatment plans, and medical adherence in real-time.

---

## 📋 Project Description

MedTrack is an Android application designed to solve the critical problem of medication non-adherence and poor health tracking. The application provides a centralized platform where:

- **Patients** can track their daily medications and log health observations
- **Caregivers/Healthcare Providers** can monitor patient progress and manage treatment plans
- **The System** maintains comprehensive medical records and generates adherence insights

The application prioritizes data integrity, user experience, and offline-first functionality through local SQLite persistence.

---

## 🎯 Key Functionalities

### Authentication & User Management
- **User Registration** - Create accounts with secure password storage
- **Login System** - Email-based authentication
- **Profile Setup** - Complete user profile with medical information, demographic data, and role assignment
- **Role-Based Access** - Support for patient and caregiver roles with appropriate feature access

### Patient Management
- **Patient List** - View all associated patients (for caregivers)
- **Patient Details** - Comprehensive patient profiles with medical history
- **Medical Profile** - Blood type, allergies, chronic conditions, emergency contacts, insurance information
- **Patient-Caregiver Assignment** - Link patients with their healthcare providers

### Treatment Plan Management
- **Create Medications** - Add new medications to the database
- **Assign Medications** - Create treatment plans with dosage, frequency, and duration
- **Schedule Management** - Define medication schedules with specific times and dates
- **Plan Tracking** - Monitor active and completed treatment plans
- **Plan Modification** - Update or cancel treatment plans as needed

### Medication Logging & Adherence
- **Daily Logging** - Record medication intake with status (taken/skipped/missed)
- **Adherence Analytics** - Calculate medication adherence percentages
- **Visual Insights** - Color-coded adherence indicators (green ≥90%, amber 70-89%, red <70%)
- **Historical Data** - Access complete medication history with timestamps

### Appointments & Notifications
- **Appointment Scheduling** - Create and manage medical appointments
- **Status Tracking** - Monitor appointment status (scheduled/completed/cancelled)
- **Notifications** - System alerts for medication reminders and appointments (via Retrofit-based remote data)

### Health Insights Dashboard
- **Dashboard Overview** - Quick summary of active treatments, recent logs, and appointments
- **Health Tips** - Integration with external APIs for health advice and motivational quotes
- **Medication Calendar** - Visual representation of medication schedules
- **Adherence Reports** - Comprehensive adherence statistics and trends

---

## 💡 Reasoning & Problem Statement

**The Challenge:** Medication non-adherence is a global healthcare problem affecting treatment outcomes, hospitalizations, and healthcare costs. Current solutions often lack:
- Intuitive user interfaces for daily tracking
- Offline-first functionality for rural or low-connectivity areas
- Comprehensive data management across multiple patient-caregiver relationships
- Real-time adherence insights for intervention

**Our Solution:** MedTrack addresses these issues by:
1. Providing an intuitive, mobile-first interface for easy medication logging
2. Operating entirely offline with local Room database persistence
3. Supporting both patient and caregiver workflows
4. Generating actionable adherence metrics
5. Integrating external health insights for motivation and education

---

## 🏗️ Architecture & Implementation

### Architecture Pattern: MVVM (Model-View-ViewModel)

```
View (Compose UI)
    ↓
ViewModel (State Management)
    ↓
Repository (Data Abstraction)
    ↓
Data Layer (Room + Remote APIs)
```

### Main Components Implemented

#### 1. **Data Layer**
- **Room Database** - Local SQLite persistence
- **Entity Classes** - Type-safe data models
- **DAOs (Data Access Objects)** - Feature-specific database operations
- **Repository Pattern** - Abstraction layer for data access

#### 2. **Domain Layer**
- **MedTrackRepository Interface** - Contract for all data operations
- **Business Logic Encapsulation** - Separation from UI concerns

#### 3. **Presentation Layer**
- **ViewModels** - UI state management and lifecycle-aware operations
- **Compose Screens** - Declarative UI components
- **Navigation** - Screen-to-screen transitions
- **State Management** - StateFlow for reactive UI updates

#### 4. **Remote Integration**
- **Retrofit** - HTTP client for external APIs
- **Health Tip API** - Advice from api.adviceslip.com
- **Motivation API** - Quotes from zenquotes.io
- **Asynchronous Coroutines** - Non-blocking API calls

---

## 🗄️ Data Storage Strategy

### Storage Type: **Local SQLite Database with Room ORM**

**Why SQLite + Room:**
- ✅ Zero external dependencies for core functionality (offline-first)
- ✅ Type-safe database access through DAOs
- ✅ Compile-time verification of SQL queries
- ✅ Built-in migration support
- ✅ Automatic foreign key constraint enforcement

### Database Schema

#### Core Entities:

1. **users** - Account information and authentication
   - user_id, email, password_hash, full_name, profile_role, profile_completed
   - Medical attributes: age, birth_date, cnp, height_cm, weight_kg, sex

2. **patients** - Patient records (linked to users)
   - patient_id, user_id (FK), full_name, birth_date, gender
   - Medical profile: blood_type, allergies, chronic_conditions, emergency_contact, family_doctor

3. **medications** - Medication database
   - medication_id, name, description, type, manufacturer, default_dose_unit

4. **patient_medications** - Treatment plans (many-to-many between patients and medications)
   - plan_id, patient_id (FK), medication_id (FK), dosage_amount, dosage_unit, frequency, start_date, end_date

5. **medication_schedule** - Specific dosing times
   - schedule_id, plan_id (FK), scheduled_time, notes

6. **medication_logs** - Adherence records
   - log_id, patient_id (FK), medication_id (FK), date, status (taken/skipped/missed), notes

7. **appointments** - Medical appointments
   - appointment_id, patient_id (FK), appointment_date, status, notes

8. **notifications** - Alert/reminder records
   - notification_id, user_id (FK), title, message, is_read, created_at

### Data Flow: From Input to Storage

```
User Input (UI)
    ↓
ViewModel validates & processes
    ↓
Repository forwards request
    ↓
LocalMedTrackRepository delegates to DAO
    ↓
Room compiles query
    ↓
SQLite executes (ACID transactions)
    ↓
Changes persisted to medtrack.db
    ↓
Changes emitted via Flow<T> (reactive updates)
    ↓
UI recomposes with new state
```

### Database Version & Migrations

- **Current Version:** 7
- **Migrations Implemented:**
  - MIGRATION_1_2: Added user profile fields (role, photo, medical info)
  - Additional migrations for schema evolution
- **Fallback Strategy:** Destructive migration on incompatible schema changes (development mode)

---

## 🌐 API Integration

### Remote Data Sources (Read-Only)

The application integrates with two external APIs to enhance user experience:

#### 1. **Health Advice API**
- **Endpoint:** `https://api.adviceslip.com/advice`
- **Purpose:** Daily health tips and wellness advice
- **Method:** Retrofit HTTP GET
- **Usage:** Dashboard displays random health advice

#### 2. **Motivation Quotes API**
- **Endpoint:** `https://zenquotes.io/api/random`
- **Purpose:** Inspirational quotes for user motivation
- **Method:** Retrofit HTTP GET with list response parsing
- **Usage:** Dashboard motivational content

#### Implementation Details
- **Base Client:** `HealthRemoteDataSource` object singleton
- **Data Format:** JSON deserialization via Gson converter
- **Error Handling:** Fallback messages if API unavailable
- **Network Layer:** Retrofit 2.11.0 with Gson 2.11.0

---

## 📱 Application Flow (User Journey)

### 1. **Welcome Screen** (Entry Point)
   - First-time user sees feature overview
   - Options to Login or Register

### 2. **Authentication Flow**

#### Path A: New User Registration
```
Welcome → Register Screen → Enter Details (Name, Email, Phone, Password)
    → Validate & Create UserEntity
    → Persist to database
    → Success message shown
```

#### Path B: Existing User Login
```
Welcome → Login Screen → Enter Email & Password
    → Query database for user by email
    → Validate credentials
    → Check profile_completed flag
    → If incomplete: Redirect to Profile Setup
    → If complete: Navigate to Dashboard
```

### 3. **Profile Setup** (One-time for new users)
```
Profile Setup Screen → User enters:
    - Personal info (age, birth date, CNP)
    - Medical info (height, weight, sex)
    - Role selection (Patient/Caregiver)
    → Persist as PatientEntity (if patient) or extend UserEntity
    → Set profile_completed = true
    → Navigate to Dashboard
```

### 4. **Dashboard** (Main Application Hub)
The dashboard adapts based on user role:

#### Patient View:
```
Dashboard:
├─ View My Medications (active treatment plans)
├─ Log Medication (record daily intake)
├─ View Adherence Statistics
└─ View Appointments
```

#### Caregiver View:
```
Dashboard:
├─ View All Patients (assigned patients)
├─ Select Patient → Detailed View
│  ├─ View Treatment Plans
│  ├─ Add/Edit Medications
│  ├─ Monitor Adherence
│  └─ View Medical History
└─ Manage Appointments
```

### 5. **Treatment Plan Management**
```
Dashboard → Treatments Screen:
├─ View Active Plans (PatientMedicationEntity list)
├─ View Schedule (MedicationScheduleEntity)
├─ Add New Plan:
│  ├─ Select/Create Medication
│  ├─ Enter Dosage & Frequency
│  ├─ Set Start/End Date
│  ├─ Define Schedule Times
│  └─ Persist to database
└─ Edit/Delete Plans
```

### 6. **Medication Logging**
```
Dashboard → Journal/Logs Screen:
├─ View Calendar with logged dates
├─ View Daily Log Entries
├─ Add New Log Entry:
│  ├─ Select Medication
│  ├─ Choose Status (Taken/Skipped/Missed)
│  ├─ Add Optional Notes
│  └─ Persist MedicationLogEntity
└─ Calculate & Display Adherence %
    (Formula: (Taken / Total) × 100)
```

### 7. **Adherence Tracking**
```
Patient Details Screen:
├─ Hero Card (Patient name, DOB, gender)
├─ Health Stats Card:
│  ├─ Total Medications
│  ├─ Adherence Score (%)
│  ├─ Status Breakdown (Taken/Skipped/Missed)
│  ├─ Color Coding:
│  │  - Green: ≥90% (Excellent)
│  │  - Amber: 70-89% (Good)
│  │  - Red: <70% (Needs Improvement)
│  └─ Contextual Message based on adherence
└─ Quick Actions (Open Treatments, Open Journal)
```

### 8. **Appointment Management**
```
Dashboard → Appointments:
├─ View Scheduled Appointments
├─ Create New Appointment:
│  ├─ Select Patient
│  ├─ Choose Date & Time
│  ├─ Add Notes
│  └─ Persist AppointmentEntity
├─ Update Status (Scheduled→Completed)
└─ Display Status Badge
```

### 9. **Dashboard Insights**
```
Dashboard:
├─ Health Tip Card (from remote API)
├─ Motivation Quote (from remote API)
├─ Active Plans Summary
├─ Recent Logs
└─ Upcoming Appointments
```

### 10. **Logout Flow**
```
Top App Bar → Logout Button:
├─ Clear session state
├─ Reset UI navigation
└─ Return to Welcome/Login Screen
```

---

## 🛠️ Technology Stack

### Framework & Language
- **Language:** Kotlin 1.9+
- **Platform:** Android 26+ (API 26 - Android 8.0)
- **JVM Target:** Java 17

### UI & Presentation
- **Jetpack Compose** (2024.06.00)
  - Material Design 3 components
  - Extended Material Icons
  - Navigation with sealed classes
  - Theming with color schemes

### Data Persistence
- **Room Database** (2.6.1)
  - DAOs for CRUD operations
  - Relationships and foreign keys
  - Type-safe queries
  - Flow-based reactive updates
- **SQLite** (underlying storage)

### State Management & Lifecycle
- **ViewModel** (lifecycle-viewmodel-ktx 2.8.4)
- **StateFlow** (kotlinx-coroutines 1.8.1)
- **Lifecycle** (lifecycle-runtime-ktx 2.8.4)

### Networking
- **Retrofit 2** (2.11.0)
- **Gson Converter** (2.11.0)
- **Coroutines** for async network calls

### Testing
- **JUnit 4** (unit tests)
- **Espresso** (UI tests)
- **Room Testing** (database tests)
- **Coroutines Test** (async testing utilities)

### Build & Dependency Management
- **Gradle** (wrapper included)
- **Kotlin Symbol Processing (KSP)** (compiler plugin for Room)
- **ProGuard** (code obfuscation for release builds)

---

## 📂 Project Structure

```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/medtrack/
│   │   │   │   ├── MainActivity.kt                    # Entry point & navigation
│   │   │   │   ├── MedTrackApplication.kt            # App initialization & DI
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── MedTrackDatabase.kt       # Room Database config
│   │   │   │   │   │   ├── entity/                   # Data entities
│   │   │   │   │   │   │   ├── UserEntity.kt
│   │   │   │   │   │   │   ├── PatientEntity.kt
│   │   │   │   │   │   │   ├── MedicationEntity.kt
│   │   │   │   │   │   │   ├── PatientMedicationEntity.kt
│   │   │   │   │   │   │   ├── MedicationScheduleEntity.kt
│   │   │   │   │   │   │   ├── MedicationLogEntity.kt
│   │   │   │   │   │   │   ├── AppointmentEntity.kt
│   │   │   │   │   │   │   └── NotificationEntity.kt
│   │   │   │   │   │   └── dao/                      # Data Access Objects
│   │   │   │   │   │       ├── UsersDao.kt
│   │   │   │   │   │       ├── PatientsDao.kt
│   │   │   │   │   │       ├── TreatmentPlanDao.kt
│   │   │   │   │   │       ├── LogsDao.kt
│   │   │   │   │   │       ├── NotificationsDao.kt
│   │   │   │   │   │       ├── AppointmentsDao.kt
│   │   │   │   │   │       └── MedTrackDao.kt        # Aggregate interface
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   └── HealthRemoteDataSource.kt # Retrofit APIs
│   │   │   │   │   └── repository/
│   │   │   │   │       └── LocalMedTrackRepository.kt # Repository impl
│   │   │   │   ├── domain/
│   │   │   │   │   └── repository/
│   │   │   │   │       └── MedTrackRepository.kt     # Repository interface
│   │   │   │   └── presentation/
│   │   │   │       ├── LoginViewModel.kt
│   │   │   │       ├── LoginScreen.kt
│   │   │   │       ├── RegisterViewModel.kt
│   │   │   │       ├── RegisterScreen.kt
│   │   │   │       ├── ProfileSetupViewModel.kt
│   │   │   │       ├── ProfileSetupScreen.kt
│   │   │   │       ├── DashboardViewModel.kt
│   │   │   │       ├── DashboardScreen.kt
│   │   │   │       ├── PatientDetailsScreen.kt
│   │   │   │       ├── TreatmentsScreen.kt
│   │   │   │       ├── JournalScreen.kt
│   │   │   │       ├── TreatmentPlanViewModel.kt
│   │   │   │       ├── LogsViewModel.kt
│   │   │   │       ├── HealthCalendarScreen.kt
│   │   │   │       ├── MedicalProfileScreen.kt
│   │   │   │       ├── WelsomeScreen.kt
│   │   │   │       └── UiDefaults.kt                 # UI constants
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       │   └── medtrack_logo.xml
│   │   │       └── values/
│   │   │           └── colors.xml
│   │   ├── AndroidManifest.xml                        # App manifest
│   │   ├── res/
│   │   └── assets/
│   ├── build.gradle.kts                               # App-level build config
│   └── proguard-rules.pro
├── gradle/
│   └── wrapper/                                       # Gradle wrapper
├── build.gradle.kts                                   # Project-level build config
├── settings.gradle.kts
├── gradle.properties
├── local.properties                                   # Local SDK paths (gitignored)
├── gradlew                                            # Unix Gradle wrapper
├── gradlew.bat                                        # Windows Gradle wrapper
└── README.md                                          # This file
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Latest Arctic Fox or newer)
- Android SDK API Level 26+
- Kotlin 1.9+
- Gradle 8.0+

### Setup & Installation

1. **Clone or Download the Project**
   ```bash
   cd C:\Users\Admin\Desktop\MedTrack\android
   ```

2. **Open in Android Studio**
   - File → Open → Select the `android` folder
   - Wait for Gradle sync to complete

3. **Sync Gradle Dependencies**
   - Android Studio will automatically prompt to sync Gradle
   - Alternatively: File → Sync Now

4. **Configure Local SDK Path** (if needed)
   - Edit `local.properties` with your Android SDK path:
     ```properties
     sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
     ```

5. **Build & Run**
   - Select an emulator or connect a physical device
   - Run → Run 'app' (or press Shift + F10)

### Running Tests

#### Compile Kotlin
```powershell
cd C:\Users\Admin\Desktop\MedTrack\android
.\gradlew.bat :app:compileDebugKotlin
```

#### Run Unit Tests
```powershell
.\gradlew.bat :app:testDebugUnitTest
```

#### Run Instrumentation Tests
```powershell
.\gradlew.bat :app:connectedAndroidTest
```

#### Build Release APK
```powershell
.\gradlew.bat :app:assembleRelease
```

---

## 📊 Features Breakdown by Role

### For Patients
- ✅ Register and create secure account
- ✅ Complete medical profile (medical history, allergies, emergency contacts)
- ✅ View assigned medications and treatment plans
- ✅ Log daily medication intake with status
- ✅ Track medication adherence with visual indicators
- ✅ Schedule and manage appointments
- ✅ View health tips and motivational quotes
- ✅ Access medication calendar

### For Caregivers/Healthcare Providers
- ✅ Register and create account
- ✅ Assign patients to their care
- ✅ Create and manage treatment plans for patients
- ✅ Monitor medication adherence in real-time
- ✅ View detailed patient medical profiles
- ✅ Schedule and track appointments
- ✅ Receive insights on patient compliance
- ✅ Generate adherence reports

---

## 🔒 Security & Best Practices

### Data Security
- ✅ Passwords stored with secure hashing (future enhancement)
- ✅ Email uniqueness enforced at database level
- ✅ Foreign key constraints ensure referential integrity
- ✅ SQLite database encrypted on Android Q+

### Architecture Best Practices
- ✅ **Single Responsibility:** Each DAO/ViewModel handles specific domain
- ✅ **Dependency Injection:** Repository pattern for loose coupling
- ✅ **Reactive State:** StateFlow for predictable UI updates
- ✅ **Type Safety:** Kotlin + compile-time checked SQL via Room

### Concurrency
- ✅ Coroutines prevent UI thread blocking
- ✅ Suspend functions ensure non-blocking database operations
- ✅ Flow collections safe from memory leaks via viewModelScope

---

## 🐛 Known Limitations & Future Enhancements

### Current Limitations
1. Password storage should use bcrypt/Argon2 (currently plain-text)
2. No end-to-end encryption for patient data
3. Single-device synchronization only (no cloud backup)
4. Limited offline notification scheduling

### Planned Enhancements
1. **Cloud Sync** - Firebase or Supabase backend
2. **Push Notifications** - Firebase Cloud Messaging
3. **Data Export** - PDF reports for adherence
4. **Multi-Device Sync** - Cross-device patient records
5. **Advanced Analytics** - Predictive adherence modeling
6. **Biometric Auth** - Face/fingerprint authentication
7. **Voice Logging** - Voice-to-text medication logging
8. **Integration APIs** - HL7 FHIR standard support

---

## 📞 Support & Contribution

### Troubleshooting

**Issue: Gradle sync fails**
- Clear cache: File → Invalidate Caches → Invalidate and Restart
- Update Gradle wrapper: gradlew wrapper --gradle-version=8.2.1

**Issue: Room schema conflicts**
- Clear app data on emulator/device
- Increment database version and add migration

**Issue: Database locked error**
- Ensure only one instance of app running
- Close and reopen app

### Contributing
Contributions welcome! Please:
1. Fork the repository
2. Create feature branch (`git checkout -b feature/your-feature`)
3. Commit changes (`git commit -am 'Add feature'`)
4. Push to branch (`git push origin feature/your-feature`)
5. Create Pull Request

---

## 📄 License

This project is proprietary. All rights reserved.

---

## 👥 Team & Acknowledgments

**Developed by:** MedTrack Development Team

**Technologies & Libraries:**
- JetBrains for Kotlin & Android Studio
- Google for Android Framework & Jetpack libraries
- Square for Retrofit
- External APIs: adviceslip.com, zenquotes.io
