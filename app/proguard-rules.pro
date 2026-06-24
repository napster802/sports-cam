# Room
-keep class androidx.room.** { *; }

# Hilt / Dagger generated code is kept automatically by the Hilt Gradle plugin.

# RootEncoder (RTMP) uses reflection for some codec configuration.
-keep class com.pedro.** { *; }
-dontwarn com.pedro.**

# Firebase models accessed via reflection during (de)serialization.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.sportcasterpro.app.core.data.remote.model.** { *; }
-keep class com.sportcasterpro.app.feature.**.data.remote.model.** { *; }
