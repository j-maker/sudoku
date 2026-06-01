# Reglas ProGuard para release (Play Store)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.google.android.ump.** { *; }

-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }

-keep class com.google.android.play.core.integrity.** { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
