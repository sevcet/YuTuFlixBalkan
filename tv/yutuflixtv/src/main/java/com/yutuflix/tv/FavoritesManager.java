package com.yutuflix.tv;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String PREF_NAME = "tv_favorites_pref";
    private static final String KEY_FAVORITES = "tv_favorites_list";

    // Dodaj u omiljene
    public static void addFavorite(Context context, SeriesItem item) {
        if (item == null) return;
        List<SeriesItem> favorites = getFavorites(context);
        favorites.add(item);
        saveFavorites(context, favorites);
    }

    // Ukloni iz omiljenih
    public static void removeFavorite(Context context, String title) {
        if (title == null) return;
        List<SeriesItem> favorites = getFavorites(context);
        for (int i = 0; i < favorites.size(); i++) {
            if (title.equals(favorites.get(i).getTitle())) {
                favorites.remove(i);
                break;
            }
        }
        saveFavorites(context, favorites);
    }

    // Proveri da li je već u omiljenim
    public static boolean isFavorite(Context context, String title) {
        if (title == null) return false;
        List<SeriesItem> favorites = getFavorites(context);
        for (SeriesItem item : favorites) {
            if (title.equals(item.getTitle())) {
                return true;
            }
        }
        return false;
    }

    // Vrati sve omiljene
    public static List<SeriesItem> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_FAVORITES, "[]");
        List<SeriesItem> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeriesItem item = SeriesItem.fromJson(obj);
                if (item != null) list.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Snimi listu
    private static void saveFavorites(Context context, List<SeriesItem> favorites) {
        JSONArray array = new JSONArray();
        for (SeriesItem item : favorites) {
            array.put(item.toJson());
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FAVORITES, array.toString())
                .apply();
    }
}