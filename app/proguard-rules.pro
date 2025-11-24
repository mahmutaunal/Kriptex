-verbose
-renamesourcefileattribute A

# ==========================
# 📌 ANDROID CORE
# ==========================

-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# ==========================
# 🏛️ ROOM (DAO + Entity + Database)
# ==========================

-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

-keep class * extends androidx.room.RoomDatabase

-dontwarn androidx.room.**

# ==========================
# 🧬 Gson
# ==========================

-keep class com.mahmutalperenunal.kriptex.data.model.** { *; }
-dontwarn com.google.gson.**

# ==========================
# 🔐 Crypto (AES / Util)
# ==========================

-keep class com.mahmutalperenunal.kriptex.util.EncryptionUtil { *; }

-keepclassmembers class com.mahmutalperenunal.kriptex.data.model.EncryptedText { <fields>; }

# ==========================
# 📌 Biometrics
# ==========================

-keep class androidx.biometric.** { *; }

# ==========================
# 🧾 Crashlytics
# ==========================

-keep public class * extends java.lang.Exception
-dontwarn com.google.firebase.**

# ==========================
# 💳 Play Billing / Play Integrity
# ==========================

-dontwarn com.android.billingclient.**
-keep class com.google.android.play.integrity.** { *; }

# ==========================
# 🔍 Root Detection (RootBeer)
# ==========================

-keep class com.scottyab.rootbeer.**

# ==========================
# 🔎 ML Kit
# ==========================

-keep class com.google.mlkit.vision.barcode.** { *; }
-dontwarn com.google.mlkit.vision.barcode.**