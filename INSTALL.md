# ============================================================
# PhysioCare Manager - Quick Install Guide
# ============================================================
#
# PREREQUISITES (install once):
#   1. JDK 17  →  https://adoptium.net/
#   2. Android Studio  →  https://developer.android.com/studio
#       (installs Android SDK automatically)
#
# ============================================================
# METHOD 1: One-Command Build (Terminal)
# ============================================================
#
#   cd PhysioCareManager
#   chmod +x build-release.sh generate-keystore.sh
#
#   # Step A: Generate signing key (one time only)
#   ./generate-keystore.sh
#
#   # Step B: Build the APK
#   ./build-release.sh
#
#   # Or for a debug build (no signing needed):
#   ./build-release.sh debug
#
#   # Step C: Install on phone
#   adb install app/build/outputs/apk/release/app-release.apk
#
# ============================================================
# METHOD 2: Android Studio (Visual)
# ============================================================
#
#   1. Open Android Studio
#   2. File → Open → select "PhysioCareManager" folder
#   3. Wait for Gradle sync (2-5 min first time)
#   4. Menu → Build → Generate Signed Bundle / APK
#   5. Select "APK" → Next
#   6. Create new keystore:
#      - Key store path: app/release-keystore.jks
#      - Password: (choose one)
#      - Alias: physiocare
#      - Key password: (same as above)
#      - Validity: 10000 days
#      - Name: PhysioCare Manager
#   7. Next → Select "release" → Finish
#   8. APK is at: app/release/app-release.apk
#
# ============================================================
# METHOD 3: Debug Build (Quickest - for testing)
# ============================================================
#
#   cd PhysioCareManager
#   ./gradlew assembleDebug
#   adb install app/build/outputs/apk/debug/app-debug.apk
#
#   Or just open in Android Studio and hit ▶ Run
#
# ============================================================
# AFTER INSTALLING:
# ============================================================
#
#   • Open "PhysioCare Manager" from app drawer
#   • Go to Settings → Set your clinic name
#   • Add first patient via Patients screen → FAB (+)
#   • Mark attendance from Dashboard → one tap!
#   • Generate bills → share via WhatsApp
#
# ============================================================
# TROUBLESHOOTING:
# ============================================================
#
#   "SDK location not found"
#   → Create local.properties with: sdk.dir=/path/to/sdk
#
#   "Could not find JDK 17"
#   → File → Settings → Build → Gradle → Gradle JDK → 17
#
#   "Phone not detected by adb"
#   → Enable USB Debugging in Developer Options
#   → Install OEM USB drivers
#
#   "Gradle sync failed"
#   → File → Invalidate Caches → Restart
#
# ============================================================
