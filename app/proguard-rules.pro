# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*

# --- Room Database Rules ---
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.example.core.database.** { *; }

# --- Kotlinx Serialization Rules ---
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keep class *$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
    *** \$serializer;
}

# --- Moshi Parser Rules ---
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keep class *JsonAdapter { *; }
-keep class *$$JsonAdapter { *; }

# --- Retrofit Rules ---
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# --- Jetpack Compose, ViewModels & State Keep Rules ---
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    *** *;
}
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModelProvider$Factory { *; }
-keep class com.example.features.**.presentation.viewmodel.** { *; }

