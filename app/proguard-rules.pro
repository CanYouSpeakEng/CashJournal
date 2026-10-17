# Proguard & R8 rules for CashJournal Production Hardening

# 1. Room Database & SQLite
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.migration.Migration
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# 2. Kotlinx Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keep class * implements kotlinx.serialization.KSerializer {
    *;
}

# 3. Domain Models, DataStore & Backup DTOs
-keep class app.cashjournal.domain.model.** { *; }
-keep class app.cashjournal.data.local.entity.** { *; }
-keep class app.cashjournal.data.preferences.** { *; }
-keep class app.cashjournal.data.backup.** { *; }

# 4. Networking (OkHttp)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 5. UI, Phosphor Icons & Compose
-keep class com.adamglin.phosphoricons.** { *; }
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 6. Biometrics & Android Architecture
-keep class androidx.biometric.** { *; }
-keep class androidx.fragment.app.** { *; }
-dontwarn java.sql.**
