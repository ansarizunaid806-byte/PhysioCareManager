# PhysioCare Manager — Android Studio Build Guide
## Complete Step-by-Step Visual Instructions

---

## 📋 BEFORE YOU START

### Install These First (if not already)

| Tool | Link | Notes |
|------|------|-------|
| **Android Studio** | https://developer.android.com/studio | Download "Android Studio Koala" or latest |
| **JDK 17** | Bundled with Android Studio | No separate install needed |

> 💡 Android Studio comes with JDK 17 built-in. You do NOT need to install Java separately.

---

## STEP 1: Download & Install Android Studio

1. Go to **https://developer.android.com/studio**
2. Click **"Download Android Studio"**
3. Accept the terms → Download the installer
4. Run the installer:
   - **Windows**: Run the `.exe` → Click Next → Next → Install
   - **Mac**: Open the `.dmg` → Drag to Applications
   - **Linux**: Extract `.tar.gz` → Run `studio.sh` from `bin/` folder

5. On first launch:
   - Click **"Next"** through the setup wizard
   - Select **"Standard"** install type
   - Click **"Finish"** → It downloads the Android SDK (~2-3 GB)
   - Wait for the download to complete (this is the SDK)

```
┌─────────────────────────────────────────────┐
│  Welcome to Android Studio                  │
│                                             │
│  ○ New Project        ← SKIP for now       │
│  ○ Open               ← WE'LL USE THIS     │
│  ○ Get from VCS                             │
│                                             │
└─────────────────────────────────────────────┘
```

---

## STEP 2: Copy the Project to Your Computer

Copy the entire `PhysioCareManager/` folder to a location you can easily find, like:

- **Windows**: `C:\Users\YourName\PhysioCareManager\`
- **Mac**: `/Users/YourName/PhysioCareManager/`
- **Linux**: `/home/YourName/PhysioCareManager/`

```
PhysioCareManager/
├── app/
├── gradle/
├── build.gradle.kts      ← Project-level build file
├── settings.gradle.kts   ← Project settings
├── gradle.properties     ← Gradle config
├── build-release.sh      ← Terminal build script (optional)
├── INSTALL.md            ← Install guide
└── README.md             ← App documentation
```

---

## STEP 3: Open the Project in Android Studio

1. Launch **Android Studio**

2. On the welcome screen, click **"Open"**

```
┌─────────────────────────────────────────────┐
│                                             │
│          Welcome to Android Studio          │
│                                             │
│   ┌────────────┐  ┌────────────┐           │
│   │  📁 Open   │  │  ➕ New    │           │
│   │  ← CLICK   │  │  Project   │           │
│   │   THIS     │  │            │           │
│   └────────────┘  └────────────┘           │
│                                             │
└─────────────────────────────────────────────┘
```

3. Navigate to and **select the `PhysioCareManager` folder**
4. Click **"OK"**

5. Android Studio opens the project. You'll see:

```
┌─────────────────────────────────────────────┐
│  PhysioCareManager                    _ □ X │
├──────────┬──────────────────────────────────┤
│ Project  │                                  │
│          │    Gradle Sync in Progress...    │
│ 📁 app   │                                  │
│ 📁 Gradle│    ██████████░░░░░░ 60%         │
│          │    Downloading dependencies...   │
│          │                                  │
├──────────┤                                  │
│  ▼ app   │                                  │
│  📁 java │                                  │
│  📁 res  │                                  │
│  📄 M... │                                  │
│          │                                  │
└──────────┴──────────────────────────────────┘
```

---

## STEP 4: Wait for Gradle Sync (Important!)

At the bottom of the screen, you'll see a progress bar:

```
┌─────────────────────────────────────────────┐
│  Gradle sync in progress...  🔄             │
│  ████████████░░░░░░░░ Resolving dependencies│
│  ⏱ Estimated time: 2-3 minutes (first time) │
└─────────────────────────────────────────────┘
```

**What it's doing:**
- Downloads Gradle build tool
- Downloads all libraries (Room, Compose, Navigation, etc.)
- Validates the project configuration

**⏱ First time: 3-5 minutes. Subsequent opens: 10-30 seconds.**

✅ **Wait until you see:**
```
┌─────────────────────────────────────────────┐
│  ✅ Gradle sync finished.                   │
│  Build variant: debug                       │
└─────────────────────────────────────────────┘
```

### ⚠️ If Gradle Sync FAILS:

| Error | Fix |
|-------|-----|
| "SDK location not found" | File → Settings → Languages → Android SDK → Set path |
| "Could not resolve dependencies" | Check internet → File → Invalidate Caches → Restart |
| "JDK 17 required" | File → Settings → Build → Gradle → Gradle JDK → Select 17 |
| "Android Gradle Plugin too old" | Update Android Studio to latest version |

---

## STEP 5: Connect Your Phone OR Create Emulator

### Option A: Use Your Physical Phone (Recommended)

1. On your Android phone:
   - Go to **Settings → About Phone**
   - Tap **"Build Number"** 7 times → "You are now a developer!"
   - Go back to **Settings → Developer Options**
   - Enable **"USB Debugging"** ✅

2. Connect phone to computer via USB cable

3. On phone, tap **"Allow USB Debugging"** → Check "Always allow" → **OK**

4. In Android Studio, look at the top toolbar:
```
┌──────────────────────────────────────────────────┐
│  📱 Samsung SM-A536E  │  app  │  ▶ Run  │  🐛  │
│  ↑ Your phone shows   │       │  ↑ Click│       │
│    here               │       │   THIS │       │
└──────────────────────────────────────────────────┘
```

### Option B: Create an Emulator (Virtual Phone)

1. In Android Studio → **Tools → Device Manager**
2. Click **"Create Device"**
3. Choose a phone: **Pixel 6** → **Next**
4. Download system image: **API 34 (Android 14)** → **Next** → **Finish**
5. Click **▶** (play button) next to the device to start it

```
┌─────────────────────────────────────────────┐
│  Device Manager                             │
│                                             │
│  ┌────────────────────┐  ┌───────────────┐ │
│  │ Pixel 6            │  │ ➕ Create      │ │
│  │ API 34             │  │   Device       │ │
│  │ ▶ (running)        │  │  ← Click this  │ │
│  │                    │  │   if empty     │ │
│  └────────────────────┘  └───────────────┘ │
└─────────────────────────────────────────────┘
```

---

## STEP 6: Run the App!

1. Make sure your device/emulator is selected in the top toolbar

2. Click the **▶ Run** button (green play icon) or press **Shift + F10**

```
┌──────────────────────────────────────────────────┐
│  app  │  📱 Pixel 6 API 34  │   ▶ Run   │  🐛  │
│       │  ↑ Device selected   │  ↑ CLICK  │      │
│       │                      │   THIS!   │      │
└──────────────────────────────────────────────────┘
```

3. **What happens:**
   - Android Studio builds the app (~30-60 seconds first time)
   - The APK is installed on your device
   - The app automatically opens

4. **You'll see on your phone:**
```
┌─────────────────────────┐
│  PhysioCare Manager     │
│  Today's Dashboard      │
│                         │
│  ╔═══════════╗ ╔═══════╗│
│  ║ Sessions  ║ ║Billed ║│
│  ║    0      ║ ║  ₹0   ║│
│  ╚═══════════╝ ╚═══════╝│
│                         │
│  Today's Attendance     │
│  No active patients     │
│                         │
│                    [+]  │
│                         │
└─────────────────────────┘
```

🎉 **The app is running!**

---

## STEP 7: Generate a Signed Release APK

When you're ready to create the final installable APK:

1. **Menu bar → Build → Generate Signed Bundle / APK...**

```
┌─────────────────────────────────────────────┐
│  Generate Signed Bundle or APK              │
│                                             │
│  ┌─────────────────────┐  ┌──────────────┐ │
│  │ Android App Bundle  │  │    APK       │ │
│  │                     │  │  ← SELECT    │ │
│  │                     │  │    THIS      │ │
│  └─────────────────────┘  └──────────────┘ │
│                                             │
│                    [ Next > ]               │
└─────────────────────────────────────────────┘
```

2. **Select "APK"** → Click **Next**

3. **Create a new keystore:**

```
┌─────────────────────────────────────────────┐
│  New Key Store                              │
│                                             │
│  Key store path:                            │
│  ┌──────────────────────────┐ ┌──────────┐ │
│  │ app/release-keystore.jks │ │ Choose.. │ │
│  └──────────────────────────┘ └──────────┘ │
│                                             │
│  Password:        [••••••••]               │
│  Confirm:         [••••••••]               │
│                                             │
│  ── Key ──────────────────────────────────  │
│  Alias:             physiocare              │
│  Password:          [••••••••]              │
│  Confirm:           [••••••••]              │
│  Validity (years):  25                      │
│                                             │
│  ── Certificate ──────────────────────────  │
│  First and Last:  PhysioCare Manager        │
│  Organization:    PhysioCare                │
│  City:            Bengaluru                 │
│  State:           Karnataka                 │
│  Country:         IN                        │
│                                             │
│                    [ Next > ]               │
└─────────────────────────────────────────────┘
```

4. **Select build variant:**

```
┌─────────────────────────────────────────────┐
│  Destination APKs                           │
│                                             │
│  Build Variants:                            │
│  ☑ release       ← MUST be checked         │
│  □ debug                                    │
│                                             │
│  Signature Versions:                        │
│  ☑ V1                                      │
│  ☑ V2 (Full APK Signature)                 │
│                                             │
│                    [ Finish ]               │
└─────────────────────────────────────────────┘
```

5. Click **Finish** → Wait for build (~1-2 minutes)

6. A popup appears:

```
┌─────────────────────────────────────────────┐
│  ✅ Build Completed                         │
│                                             │
│  APK(s) generated successfully.             │
│                                             │
│  Location:                                  │
│  📁 app/release/app-release.apk    ← HERE  │
│                                             │
│  [ Show in Explorer ]  [      OK      ]    │
│                          ↑ Click to close   │
└─────────────────────────────────────────────┘
```

---

## STEP 8: Install the APK on Any Phone

### Method A: USB Cable
```bash
adb install app/release/app-release.apk
```

### Method B: Transfer File
1. Copy `app-release.apk` to your phone via:
   - USB cable → copy to Downloads folder
   - Email it to yourself
   - Google Drive / Dropbox
   - WhatsApp (send to yourself)

2. On the phone, **tap the .apk file**

3. If prompted: **Settings → Allow from this source**

4. Tap **Install** → Wait → **Open**

```
┌─────────────────────────┐
│                         │
│   📦 PhysioCare Manager │
│   v1.0.0                │
│                         │
│   This app will have    │
│   access to:            │
│   • Notifications       │
│   • Storage             │
│                         │
│   ┌──────────────────┐  │
│   │     INSTALL      │  │
│   └──────────────────┘  │
│                         │
└─────────────────────────┘
```

---

## 🎯 COMPLETE CHECKLIST

```
✅ Step 1: Install Android Studio
✅ Step 2: Copy PhysioCareManager/ folder to computer
✅ Step 3: Open → File → Open → select folder
✅ Step 4: Wait for Gradle sync (3-5 min first time)
✅ Step 5: Connect phone OR create emulator
✅ Step 6: Click ▶ Run → App launches on device!
✅ Step 7: Build → Generate Signed APK → Follow wizard
✅ Step 8: Transfer APK to phone → Install
```

---

## 🔧 COMMON ISSUES IN ANDROID STUDIO

### "Gradle project sync failed"
```
Fix: File → Invalidate Caches → Invalidate and Restart
```

### "No targets available" / Can't find device
```
Fix: Plug in phone → Allow USB debugging
     OR: Tools → Device Manager → Create emulator
```

### "Compilation error: Kotlin version mismatch"
```
Fix: File → Settings → Plugins → Update Kotlin plugin
     Then restart Android Studio
```

### App builds but crashes on launch
```
Fix: Check the Logcat tab (bottom of Android Studio)
     Look for the red error message
     Common fix: Clean → Build → Clean → Rebuild
     Menu → Build → Clean Project
     Menu → Build → Rebuild Project
```

### "local.properties not found"
```
Fix: Android Studio creates this automatically.
     If missing, create it manually:
     sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
     (Windows)
     sdk.dir=/Users/YourName/Library/Android/sdk
     (Mac)
     sdk.dir=/home/YourName/Android/Sdk
     (Linux)
```

---

## 📱 AFTER INSTALLING — FIRST USE

1. **Open the app** from your phone's app drawer
2. **Settings** (gear icon) → Enter your clinic name
3. **Patients screen** → Tap **+** to add your first patient
4. **Dashboard** → Mark today's attendance with **one tap**
5. **Patient Profile** → View calendar, record payments
6. **Bill PDF** → Generate & share via WhatsApp

**The app works 100% offline. No internet needed. All data stays on your phone.**
