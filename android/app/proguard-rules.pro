# Mahin release shrinking (R8). Third-party AARs ship bundled rules for OkHttp,
# Retrofit (retrofit2.pro), and Room (room-runtime). This file adds app-specific
# keeps for Hilt, kotlinx.serialization JSON models, and Room entities in dev.mahin.

-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod, Exceptions
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Hilt / DI
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Retrofit suspend + generic signatures (supplements retrofit2 bundled rules)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# kotlinx.serialization (API DTOs in core modules)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep @kotlinx.serialization.Serializable class dev.mahin.** { *; }
-keepclassmembers class dev.mahin.** {
    *** Companion;
}
-if @kotlinx.serialization.Serializable class dev.mahin.**
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-keepclasseswithmembers class dev.mahin.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room (supplements room-runtime bundled rules)
-keep @androidx.room.Entity class dev.mahin.**
-keep @androidx.room.Dao interface dev.mahin.**
-keep class * extends androidx.room.RoomDatabase

-dontwarn org.bouncycastle.**
