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
-keep,includedescriptorclasses class org.muslim.app.**$$serializer { *; }
-keepclassmembers class org.muslim.app.** {
    *** Companion;
}
-keepclasseswithmembers class org.muslim.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Room (entities are reflected by the generated DAO implementations) ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Hilt / Dagger (generated components) ---
-dontwarn dagger.hilt.**

# --- MapLibre GL Native (official consumer rules) ---
-dontwarn org.maplibre.android.**
-keep class org.maplibre.android.** { *; }
-keep class org.maplibre.geojson.** { *; }
-keep class org.maplibre.android.location.** { *; }
-keepclassmembers class org.maplibre.android.** {
    *** onMapReady(***);
    *** onStyleLoaded(***);
}
-keepattributes Exceptions, InnerClasses, Signature

# --- Glance widgets ---
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep class * extends androidx.glance.action.ActionCallback
