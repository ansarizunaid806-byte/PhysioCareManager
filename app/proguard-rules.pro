# ProGuard rules for PhysioCare Manager
-keepclassmembers class com.physiocare.manager.data.local.entity.** { *; }
-keep class com.physiocare.manager.data.local.entity.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
