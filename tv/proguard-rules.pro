# BestStreamsUG ProGuard / R8 rules
# Release build runs R8 (minify) + resource shrinking. Keep rules below protect the
# reflection-sensitive surfaces: Gson DTOs, Retrofit services, Hilt.

# Attributes required by Retrofit/Gson/Hilt/Compose reflection.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# ---------------------------------------------------------------------------
# Gson (reflection-based (de)serialization of REST payloads)
# ---------------------------------------------------------------------------
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Wire DTOs are read/written reflectively by Gson.
-keep class com.engineerfred.beststreamsug.data.remote.dto.** { <init>(...); <fields>; }

# ---------------------------------------------------------------------------
# Retrofit (service interfaces are invoked via dynamic proxies)
# keepclasses allow shrinking/obfuscation while preserving the contract.
# ---------------------------------------------------------------------------
-keep,allowshrinking,allowobfuscation interface com.engineerfred.beststreamsug.data.remote.api.** { *; }

# ---------------------------------------------------------------------------
# Hilt / Dagger generated components + entry points are looked up at runtime.
# ---------------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.EntryPoint class * { *; }

# ViewModels (@HiltViewModel) are constructed reflectively by Hilt.
-keepclasseswithmembers @dagger.hilt.android.lifecycle.HiltViewModel class * {
    <init>(...);
}
-keep @dagger.hilt.android.lifecycle.HiltViewModel class com.engineerfred.beststreamsug.presentation.**ViewModel { *; }

# ---------------------------------------------------------------------------
# Miscellaneous safety
# ---------------------------------------------------------------------------
# Navigation Compose routes / SavedStateHandle keys are strings; keep ViewModel state.
-keepattributes SourceFile, LineNumberTable

# Coil, Media3, Compose, OkHttp all ship official consumer ProGuard rules.