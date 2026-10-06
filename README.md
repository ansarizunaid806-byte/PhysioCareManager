# PhysioCare Manager

A complete, production-ready, offline-first Android app for solo physiotherapists to manage patient records, session attendance, and payments.

## 📱 Features

### Core Features
- **Patient Management** — Add/edit/delete patients with diagnosis, referred-by, per-session charge, home-visit support
- **Attendance Tracking** — One-tap marking (Present/Absent/Cancelled) from the dashboard
- **Billing & Payments** — Automatic billing for attended sessions only, partial/full/advance payments
- **Monthly Calendar** — Color-coded session calendar per patient (Green=Present, Red=Absent, Grey=None)
- **Dues Tracking** — See all patients with pending amounts, sorted by highest due
- **PDF Bills** — Generate & share via WhatsApp with one tap
- **Monthly Reports** — Per-patient breakdown with CSV export
- **Backup & Restore** — Full data backup to JSON, restore anytime
- **App Lock** — PIN-based security for patient data privacy
- **Dark Mode** — System/Light/Dark theme support

### Key Business Logic
- Only **Present** sessions are billed (Absent/Cancelled = ₹0)
- Outstanding = Total Billed − Total Paid
- Payments auto-mark oldest due sessions as paid
- Per-patient session rates (different for each patient)
- Per-session charge override support (discounts, home-visit charges)
- Indian number formatting (₹1,20,000)

## 🏗️ Architecture

```
MVVM + Repository Pattern

┌─────────────────────────────────────────────────┐
│                    UI Layer                      │
│  (Jetpack Compose + Material 3 + Navigation)    │
├─────────────────────────────────────────────────┤
│                 ViewModel Layer                   │
│  (StateFlow → UI State, Business Logic)          │
├─────────────────────────────────────────────────┤
│                Repository Layer                   │
│  (Data abstraction, offline-first logic)         │
├─────────────────────────────────────────────────┤
│                 Data Layer                        │
│  Room Database (SQLite) + DAOs + Entities        │
│  DataStore (Preferences/Settings)                │
└─────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
PhysioCareManager/
├── app/
│   └── src/main/
│       ├── java/com/physiocare/manager/
│       │   ├── PhysioCareApp.kt              # Application class
│       │   ├── MainActivity.kt               # Single activity
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── AppDatabase.kt        # Room database
│       │   │   │   ├── dao/                  # Data Access Objects
│       │   │   │   │   ├── PatientDao.kt
│       │   │   │   │   ├── SessionDao.kt
│       │   │   │   │   └── PaymentDao.kt
│       │   │   │   └── entity/               # Room entities
│       │   │   │       ├── PatientEntity.kt
│       │   │   │       ├── SessionEntity.kt
│       │   │   │       └── PaymentEntity.kt
│       │   │   └── repository/               # Repositories
│       │   │       ├── PatientRepository.kt
│       │   │       ├── SessionRepository.kt
│       │   │       └── PaymentRepository.kt
│       │   ├── di/
│       │   │   └── AppContainer.kt           # Manual DI
│       │   ├── ui/
│       │   │   ├── navigation/               # Nav graph + routes
│       │   │   ├── theme/                    # Material 3 theme
│       │   │   ├── components/               # Reusable components
│       │   │   └── screens/                  # App screens
│       │   │       ├── dashboard/
│       │   │       ├── patients/
│       │   │       ├── patientprofile/
│       │   │       ├── attendance/
│       │   │       ├── payment/
│       │   │       ├── dues/
│       │   │       ├── reports/
│       │   │       ├── billing/
│       │   │       └── settings/
│       │   ├── viewmodel/                    # ViewModels
│       │   └── util/                         # Utilities
│       │       ├── DateUtils.kt
│       │       ├── PdfGenerator.kt
│       │       ├── CsvExporter.kt
│       │       └── BackupManager.kt
│       ├── res/
│       │   ├── values/
│       │   └── xml/file_paths.xml
│       └── AndroidManifest.xml
├── build.gradle.kts                          # Project-level
├── app/build.gradle.kts                      # App-level
├── settings.gradle.kts
└── gradle.properties
```

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + Repository) |
| Database | Room (SQLite) |
| Navigation | Jetpack Navigation Compose |
| Preferences | DataStore |
| Notifications | WorkManager + NotificationManager |
| PDF | Android PDF Document API |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Build & Run
1. Open Android Studio → File → Open → Select `PhysioCareManager/` folder
2. Wait for Gradle sync to complete
3. Connect a device or start an emulator (API 26+)
4. Click Run ▶️

### Build APK
```bash
cd PhysioCareManager
./gradlew assembleRelease
```

## 📱 Screens

| # | Screen | Description |
|---|--------|-------------|
| 1 | **Dashboard** | Today's patients, quick attendance, monthly stats |
| 2 | **Patients List** | Search, filter (Active/Completed/With Dues) |
| 3 | **Add/Edit Patient** | Full patient details form |
| 4 | **Patient Profile** | Calendar, session history, payments, balance |
| 5 | **Mark Attendance** | Full session history with edit/delete |
| 6 | **Record Payment** | Amount, mode, auto-marks due sessions |
| 7 | **Dues Screen** | All patients with pending amounts |
| 8 | **Monthly Report** | Per-patient breakdown + CSV export |
| 9 | **Bill Statement** | PDF bill, share via WhatsApp |
| 10 | **Settings** | Clinic name, reminders, theme, backup |

## ✅ Acceptance Criteria

- [x] Adding a patient with per-session charge takes under 30 seconds
- [x] Marking attendance takes one tap
- [x] Totals always exclude absent sessions
- [x] Partial payment correctly reduces due balance
- [x] PDF bill lists attended sessions, rate, total, paid, balance
- [x] Shares via WhatsApp
- [x] App works fully offline
- [x] Data survives restarts (Room persistence)

## 📊 Data Model

### Patient
- ID, name, mobile, age, gender
- Condition/diagnosis, referred by
- Start date, status (Active/Completed/Dropped)
- Per-session charge (₹), planned sessions
- Home visit flag & charge
- Notes

### Session
- ID, patient ID, date
- Status (Present/Absent/Cancelled)
- Charge (defaults to patient rate, editable)
- Payment status (Paid/Due)
- Optional: note, pain level, home-visit flag

### Payment
- ID, patient ID, amount (₹), date
- Mode (Cash/UPI/Card/Other)
- Note

## 🔐 Privacy
- All data stored locally on device
- Optional PIN-based app lock
- No internet required for any core feature
- Backup/restore controlled by user

## 📄 License
MIT License — Free to use, modify, and distribute.
