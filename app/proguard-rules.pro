# --- kotlinx.serialization (official rules) ---
# Keep serializers and their descriptors for all @Serializable classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class org.example.islamicapp.**$$serializer { *; }
-keepclassmembers class org.example.islamicapp.** {
    *** Companion;
}
-keepclasseswithmembers class org.example.islamicapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Room (entities are reflected by the generated DAO implementations) ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Hilt / Dagger (generated components) ---
-dontwarn dagger.hilt.**

# --- Glance widgets ---
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep class * extends androidx.glance.action.ActionCallback
