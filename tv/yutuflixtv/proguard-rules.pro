# ===========================
#   YuTuFlix TV - ProGuard
# ===========================

# --- Glavne Android klase i entry point-ovi ---
-keep class com.yutuflix.tv.** { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# --- Glide (sprečava crne slike i crash pri keširanju) ---
-keep public class com.bumptech.glide.** { *; }
-keep public interface com.bumptech.glide.** { *; }
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep class * extends com.bumptech.glide.module.LibraryGlideModule { *; }

# --- Jsoup (sprečava probleme pri parsiranju XML/HTML) ---
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# --- Leanback (Android TV UI elementi) ---
-keep class androidx.leanback.** { *; }

# --- RecyclerView i Adapteri ---
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView$Adapter {
    public <init>(...);
}

# --- WebView / Browser / Chrome Custom Tabs ---
-keep class androidx.browser.** { *; }

# --- Materijalne komponente i UI animacije ---
-keep class com.google.android.material.** { *; }

# --- Google Play Services (ako koristiš oglase) ---
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# --- Zadrži anotacije i interne klase ---
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# --- Logovi i debugging (isključi u release verziji) ---
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# --- Spriječi obfuskaciju stringova ako imaš ručno parsiranje ---
-keepclassmembers class * {
    @org.jsoup.* <fields>;
}

# --- Spreči R8 da izbaci refleksivne klase (npr. kod Glide-a i Kotlin-a) ---
-dontwarn javax.annotation.**
-dontwarn kotlin.**
-dontwarn org.intellij.lang.annotations.**

# --- Dodatna stabilnost za AndroidX biblioteke ---
-dontwarn androidx.fragment.**
-dontwarn androidx.recyclerview.**
-dontwarn androidx.leanback.**
-dontwarn androidx.browser.**
-dontwarn androidx.core.**
