# ArrowZen release ProGuard/R8 rules.
# Keep Room entities and DAOs safe from aggressive shrinking.
-keep class com.yugalify.arrowzen.data.database.** { *; }

# Keep kotlinx.serialization models used for local puzzle JSON.
-keepattributes *Annotation*, InnerClasses
-keep class com.yugalify.arrowzen.data.model.** { *; }
-keepclassmembers class com.yugalify.arrowzen.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.yugalify.arrowzen.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# General Kotlin metadata
-keepattributes Signature
-keepattributes *Annotation*
