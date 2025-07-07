# ================================
# ✅ GENERAL CONFIGURATION
# ================================

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic

# Hata ayıklama bilgileri (opsiyonel)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Uygulama giriş noktası
-keep class com.mahmutalperenunal.kriptex.** { *; }

# ================================
# ✅ ANDROIDX & JETPACK
# ================================

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# ViewModel ve LiveData
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Biometric API
-keep class androidx.biometric.** { *; }

# ConstraintLayout
-keep class androidx.constraintlayout.** { *; }

# Navigation Component (SafeArgs)
-keep class androidx.navigation.** { *; }

# ================================
# ✅ JSON & SERIALIZATION
# ================================

# GSON
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# ================================
# ✅ FIREBASE
# ================================

# Firebase Analytics & Crashlytics
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Exception sınıfları (Crashlytics için)
-keep public class * extends java.lang.Exception

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }
-dontwarn com.google.firebase.storage.**

# ================================
# ✅ CRYPTO (AES şifreleme, Keystore)
# ================================

-keep class javax.crypto.** { *; }
-dontwarn javax.crypto.**
-keep class java.security.** { *; }
-dontwarn java.security.**
-keep class android.security.** { *; }
-dontwarn android.security.**

-keep class com.mahmutalperenunal.kriptex.util.EncryptionUtil { *; }

-keepclassmembers class com.mahmutalperenunal.kriptex.data.model.EncryptedText { <fields>; }

# ================================
# ✅ DYNAMIC FEATURES / REFLECTION
# ================================

-keep class **.databinding.*Binding { *; }
-keep class com.mahmutalperenunal.kriptex.databinding.** { *; }

# Material Components ve support
-keep class com.google.android.material.** { *; }

# ================================
# ✅ ROOT DETECTION (RootBeer)
# ================================

-keep class com.scottyab.rootbeer.** { *; }
-dontwarn com.scottyab.rootbeer.**

# ================================
# ✅ PLAY BILLING
# ================================

-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ================================
# ✅ PLAY INTEGRITY API
# ================================

-keep class com.google.android.play.integrity.** { *; }
-dontwarn com.google.android.play.integrity.**

# DEX optimizasyonunu sınırla (bazı crash'leri engeller)
-dontoptimize

# ================================
# ✅ ML KIT - BARCODE SCANNING
# ================================

-keep class com.google.mlkit.vision.barcode.** { *; }
-dontwarn com.google.mlkit.vision.barcode.**

# Vision common classes
-keep class com.google.mlkit.vision.common.** { *; }
-dontwarn com.google.mlkit.vision.common.**

-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-dontwarn com.google.android.gms.internal.mlkit_vision_barcode.**

# ZXing (QR Code Writer)
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**