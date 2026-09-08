# ==============================================================================
# BestStreams UG - Production ProGuard / R8 Rules
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. General & Stacktrace Preservation (Critical for Crash Reporting)
# ------------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions
-renamesourcefileattribute SourceFile

# Keep Kotlin enums methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ------------------------------------------------------------------------------
# 2. Data Transfer Objects (DTOs) & Domain Models (Gson & Retrofit)
# ------------------------------------------------------------------------------
# Keep all DTOs and their fields for Gson reflection
-keep class com.engineerfred.beststreamsug.data.remote.dto.** { *; }
-keepclassmembers class com.engineerfred.beststreamsug.data.remote.dto.** { *; }

# Keep domain models
-keep class com.engineerfred.beststreamsug.domain.model.** { *; }
-keepclassmembers class com.engineerfred.beststreamsug.domain.model.** { *; }

# Keep data repository classes and entities
-keep class com.engineerfred.beststreamsug.data.repository.** { *; }
-keepclassmembers class com.engineerfred.beststreamsug.data.repository.** { *; }

# Keep common result/error models
-keep class com.engineerfred.beststreamsug.core.common.** { *; }
-keepclassmembers class com.engineerfred.beststreamsug.core.common.** { *; }

# Keep all classes and fields annotated with Gson's @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * {
    @com.google.gson.annotations.Expose <fields>;
}
-keep class com.google.gson.** { *; }

# ------------------------------------------------------------------------------
# 3. Retrofit & OkHttp
# ------------------------------------------------------------------------------
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.engineerfred.beststreamsug.data.remote.api.** { *; }

# ------------------------------------------------------------------------------
# 4. Google Cast Framework & MediaRouter (Critical for Cast connection)
# ------------------------------------------------------------------------------
-keep class com.google.android.gms.cast.** { *; }
-keep class com.google.android.gms.cast.framework.** { *; }
-keep class androidx.mediarouter.** { *; }
-keepclassmembers class androidx.mediarouter.** { *; }

# Keep CastOptionsProvider referenced dynamically by AndroidManifest.xml
-keep class com.engineerfred.beststreamsug.mobile.core.cast.CastOptionsProvider {
    public <init>();
    *;
}

# ------------------------------------------------------------------------------
# 5. Media3 & ExoPlayer (Video/Audio Playback Engine)
# ------------------------------------------------------------------------------
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.extractor.** { *; }
-keep class androidx.media3.datasource.** { *; }
-keepclassmembers class androidx.media3.exoplayer.** { *; }
-dontwarn androidx.media3.**

# ------------------------------------------------------------------------------
# 6. Dagger / Hilt Dependency Injection
# ------------------------------------------------------------------------------
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class dagger.hilt.** { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent
-keep class * implements dagger.hilt.internal.ComponentEntryPoint
-keep class * implements dagger.hilt.internal.TestSingletonComponent
-keep class * implements dagger.hilt.internal.Preconditions
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <methods>;
}

# ------------------------------------------------------------------------------
# 7. WorkManager & Hilt Worker (Background Notifications Sync)
# ------------------------------------------------------------------------------
-keep class androidx.work.** { *; }
-keep class androidx.hilt.work.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.engineerfred.beststreamsug.mobile.core.notification.NewContentNotificationWorker {
    public <init>(...);
    *;
}

# ------------------------------------------------------------------------------
# 8. Coil (Image Loading)
# ------------------------------------------------------------------------------
-keep class coil.** { *; }
-keep class coil3.** { *; }
-dontwarn coil.**
-dontwarn coil3.**

# ------------------------------------------------------------------------------
# 9. Jetpack Compose & State Saver
# ------------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * implements androidx.compose.runtime.saveable.Saver { *; }
-keepclassmembers class * implements androidx.compose.runtime.saveable.SaverScope { *; }

# ------------------------------------------------------------------------------
# 10. Kotlin Coroutines & Reflection
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**