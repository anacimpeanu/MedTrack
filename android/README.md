# MedTrack Android (MVVM + Compose + Room)

Acest folder contine un bootstrap Android pentru Data Layer in arhitectura MVVM:

- `Model` -> entitati Room + repository
- `ViewModel` -> stare UI expusa cu `StateFlow`
- `View` -> ecran Compose minimal

## Ce include

- Room database locala pe entitatile:
  - `users`
  - `patients`
  - `medications`
  - `patient_medications`
  - `medication_schedule`
  - `medication_logs`
  - `notifications`
  - `appointments`
- DAO cu operatii de baza si `Flow`
- DAO-uri separate pe feature (`UsersDao`, `PatientsDao`, `TreatmentPlanDao`, `LogsDao`, `NotificationsDao`, `AppointmentsDao`)
- `MedTrackRepository` + implementare locala
- ViewModel + ecrane separate pentru:
  - `Patients`
  - `Treatment Plan`
  - `Logs`
- Gradle Wrapper inclus (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`)

## Rulare locala (Android Studio)

1. Deschide folderul `android` ca proiect Android.
2. Sync Gradle.
3. Ruleaza aplicatia pe emulator/device.

## Test rapid (CLI)

Daca ai Android SDK configurat:

```powershell
cd C:\Users\Admin\Desktop\MedTrack\android
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
```



