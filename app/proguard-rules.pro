# --- Project Specific Rules ---

# Keep data models used for JSON serialization (Gson)
# Renaming these will break API parsing
-keep class com.deysdeveloper.mutualfundapp.domain.model.** { *; }
-keep class com.deysdeveloper.mutualfundapp.data.local.entity.** { *; }

# --- Library Specific Rules ---

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-dontwarn kotlinx.coroutines.**

# Kotlin Serialization (if used)
-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
