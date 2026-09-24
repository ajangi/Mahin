# Keep Hilt / Kotlin metadata.
-keepattributes *Annotation*, InnerClasses, Signature, Exception
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn org.bouncycastle.**
