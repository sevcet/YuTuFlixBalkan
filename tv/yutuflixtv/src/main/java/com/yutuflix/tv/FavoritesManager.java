package com.yutuflix.tv;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String PREF_NAME = "favorites_pref";
    private static final String KEY_FAVORITES = "favorites_list";

    // Dodaj u omiljene
    public static void addFavorite(Context context, RecyclerItem item) {
        if (item == null) return;
        List<RecyclerItem> favorites = getFavorites(context);
        favorites.add(item);
        saveFavorites(context, favorites);
    }

    // Ukloni iz omiljenih
    public static void removeFavorite(Context context, String id) {
        if (id == null) return;
        List<RecyclerItem> favorites = getFavorites(context);
        for (int i = 0; i < favorites.size(); i++) {
            String favId = getItemKey(favorites.get(i));
            if (id.equals(favId)) {
                favorites.remove(i);
                break;
            }
        }
        saveFavorites(context, favorites);
    }

    // Proveri da li je već u omiljenim
    public static boolean isFavorite(Context context, String id) {
        if (id == null) return false;
        List<RecyclerItem> favorites = getFavorites(context);
        for (RecyclerItem item : favorites) {
            String favId = getItemKey(item);
            if (id.equals(favId)) {
                return true;
            }
        }
        return false;
    }

    // Vrati sve omiljene
    public static List<RecyclerItem> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_FAVORITES, "[]");
        List<RecyclerItem> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                RecyclerItem item = RecyclerItem.fromJson(obj);
                if (item != null) list.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Snimi listu
    private static void saveFavorites(Context context, List<RecyclerItem> favorites) {
        JSONArray array = new JSONArray();
        for (RecyclerItem item : favorites) {
            array.put(item.toJson());
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FAVORITES, array.toString())
                .apply();
    }

    // Vrati jedinstveni ključ (za film videoId, za serije title)
    private static String getItemKey(RecyclerItem item) {
        if (item == null) return null;
        if (item.getVideoId() != null && !item.getVideoId().isEmpty()) {
            return item.getVideoId(); // filmovi
        } else {
            return item.getTitle();   // serije
        }
    }
}