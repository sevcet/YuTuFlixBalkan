package com.yutuflix.tv;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LanguageManager {
    private Context context;
    private SharedPreferences prefs;

    public static final String[] SUPPORTED_LANGUAGES = {
            "en", "es", "pt", "fr", "de", "zh", "ja", "ko", "ar", "hi",
            "it", "tr", "pl", "nl", "th", "id", "vi", "uk", "ro", "el",
            "cs", "hu", "sv", "no", "da", "fi", "ms", "tl", "he", "bn",
            "ta", "te", "mr", "pa", "si", "sr", "hr", "bs", "sl"
    };

    public static final HashMap<String, String> LANGUAGE_NAMES = new HashMap<String, String>() {{
        put("en", "English");
        put("es", "Spanish");
        put("pt", "Portuguese (Brazilian)");
        put("fr", "French");
        put("de", "German");
        put("zh", "Chinese (Simplified)");
        put("ja", "Japanese");
        put("ko", "Korean");
        put("ar", "Arabic");
        put("hi", "Hindi");
        put("it", "Italian");
        put("tr", "Turkish");
        put("pl", "Polish");
        put("nl", "Dutch");
        put("th", "Thai");
        put("id", "Indonesian");
        put("vi", "Vietnamese");
        put("uk", "Ukrainian");
        put("ro", "Romanian");
        put("el", "Greek");
        put("cs", "Czech");
        put("hu", "Hungarian");
        put("sv", "Swedish");
        put("no", "Norwegian");
        put("da", "Danish");
        put("fi", "Finnish");
        put("ms", "Malay");
        put("tl", "Filipino / Tagalog");
        put("he", "Hebrew");
        put("bn", "Bengali");
        put("ta", "Tamil");
        put("te", "Telugu");
        put("mr", "Marathi");
        put("pa", "Punjabi");
        put("si", "Sinhala");
        put("sr", "Serbian");
        put("hr", "Croatian");
        put("bs", "Bosnian");
        put("sl", "Slovenian");
    }};

    public LanguageManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
    }

    public void setAppLanguage(String languageCode) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_language", languageCode);
        editor.apply();

        updateAppLanguage(languageCode);
    }

    public String getCurrentLanguage() {
        return prefs.getString("app_language", getSystemLanguage());
    }

    public String getSystemLanguage() {
        Locale systemLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            systemLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            systemLocale = Resources.getSystem().getConfiguration().locale;
        }

        String lang = systemLocale.getLanguage();
        // Proveri da li je podržan jezik
        for (String supportedLang : SUPPORTED_LANGUAGES) {
            if (supportedLang.equals(lang)) {
                return lang;
            }
        }
        return "en"; // default English
    }

    public void updateAppLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public List<String> getFavoriteLanguages() {
        String favorites = prefs.getString("favorite_languages", "en");
        return Arrays.asList(favorites.split(","));
    }

    public void setFavoriteLanguages(List<String> languages) {
        StringBuilder sb = new StringBuilder();
        for (String lang : languages) {
            if (sb.length() > 0) sb.append(",");
            sb.append(lang);
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("favorite_languages", sb.toString());
        editor.apply();
    }

    public void addFavoriteLanguage(String languageCode) {
        List<String> favorites = new ArrayList<>(getFavoriteLanguages());
        if (!favorites.contains(languageCode)) {
            favorites.add(languageCode);
            setFavoriteLanguages(favorites);
        }
    }

    public void removeFavoriteLanguage(String languageCode) {
        List<String> favorites = new ArrayList<>(getFavoriteLanguages());
        favorites.remove(languageCode);
        setFavoriteLanguages(favorites);
    }

    public String[] getSupportedLanguageNames() {
        String[] names = new String[SUPPORTED_LANGUAGES.length];
        for (int i = 0; i < SUPPORTED_LANGUAGES.length; i++) {
            names[i] = LANGUAGE_NAMES.get(SUPPORTED_LANGUAGES[i]);
        }
        return names;
    }

    public String[] getSupportedLanguageCodes() {
        return SUPPORTED_LANGUAGES;
    }

    public String getLanguageName(String code) {
        return LANGUAGE_NAMES.getOrDefault(code, code);
    }
}
