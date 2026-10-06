# 🔧 Self-Maintenance Guide

## How to manage PhysioCare Manager completely on your own

---

## 📱 Making Changes From Your Phone

You DON'T need a computer! Here's how:

### To change the app name:
1. Go to your repo on GitHub
2. Open: `app/src/main/res/values/strings.xml`
3. Tap pencil → edit the name → commit
4. Wait 3-5 min for build → download new APK → install

### To change colors/theme:
1. Open: `app/src/main/java/com/physiocare/manager/ui/theme/Color.kt`
2. Edit the color values (hex codes)
3. Commit → wait for build → install

### To add a new feature:
You'd need to hire a Kotlin developer OR learn Kotlin basics.
Good resources:
- Kotlin official tutorial: https://kotlinlang.org/docs/tutorials/
- Android developers: https://developer.android.com/courses

### To change per-screen defaults:
Most settings are in the ViewModel files.
Example: Default reminder time → `SettingsViewModel.kt` line with `reminderHour = 20`

---

## 🏪 Publishing Updates to Play Store

### When you make changes:
1. Update code on GitHub (via phone)
2. Wait for build
3. Download the new **AAB** file
4. Go to Play Console → Production → Create new release
5. Upload new AAB → increment version code
6. Submit for review (usually approved in hours for updates)

### Version numbering:
In `app/build.gradle.kts`:
```
versionCode = 2          ← Increment by 1 each update
versionName = "1.2.0"    ← Change display version
```

---

## 💾 Backing Up Your Code

### Automatic:
- GitHub stores all versions forever
- You can view/revert to ANY past version

### Manual backup:
1. Go to your repo on GitHub
2. Click Code → Download ZIP
3. Save to Google Drive / your phone

---

## 🛡️ Security Best Practices

### For GitHub:
- ✅ Use strong password + 2-factor authentication
- ✅ Never share your Personal Access Tokens
- ✅ Regularly check who has access (Settings → Collaborators)

### For Play Store:
- ✅ Use a dedicated email for Play Console
- ✅ Enable 2FA on your Google account
- ✅ Keep your developer account info updated

### For the App:
- ✅ App lock is available for users (PIN)
- ✅ All data is local — no cloud breach risk
- ✅ Users can backup/restore anytime

---

## 🆘 When You Need Help

### Free resources:
- Android docs: https://developer.android.com/docs
- Kotlin docs: https://kotlinlang.org/docs/
- Stack Overflow: https://stackoverflow.com (search "Android + your error")
- GitHub Support: https://support.github.com
- Play Console Help: https://support.google.com/googleplay/android-developer

### Paid help (if needed):
- Fiverr/Upwork: Hire a Kotlin developer for ₹1000-5000/hour
- Local Android developers: Check LinkedIn/Indeed
- Physiotherapy forums: Other users may help with feature requests

---

## 📈 Growing Your App

### Month 1-3: Focus on users
- Get first 100 downloads
- Ask for reviews from happy users
- Fix any bugs reported

### Month 3-6: Focus on monetization
- Add premium features
- Start charging for advanced features
- Reach out to physiotherapy colleges

### Month 6-12: Focus on scale
- Build Android app with Flutter (for iOS version)
- Hire part-time support
- Consider B2B sales to hospital chains

---

## ✅ You've Got This!

You own a complete, working, professional Android app.
You have the code, the Play Store account, the users — everything.

Just take it one step at a time. 🚀
