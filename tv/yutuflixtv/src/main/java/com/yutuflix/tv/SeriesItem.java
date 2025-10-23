package com.yutuflix.tv;

import org.json.JSONException;
import org.json.JSONObject;

public class SeriesItem {
    private String title;
    private String year;
    private String genre;
    private String description;
    private String imageUrl;
    private String seasonsJson;

    public SeriesItem(String title, String year, String genre, String description,
                      String imageUrl, String seasonsJson) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.description = description;
        this.imageUrl = imageUrl;
        this.seasonsJson = seasonsJson;
    }

    // Getters
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getSeasonsJson() { return seasonsJson; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setYear(String year) { this.year = year; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSeasonsJson(String seasonsJson) { this.seasonsJson = seasonsJson; }

    // Konvertuj u JSON
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("title", title);
            obj.put("year", year);
            obj.put("genre", genre);
            obj.put("description", description);
            obj.put("imageUrl", imageUrl);
            obj.put("seasonsJson", seasonsJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    // Kreiraj iz JSON
    public static SeriesItem fromJson(JSONObject obj) {
        try {
            return new SeriesItem(
                    obj.optString("title", ""),
                    obj.optString("year", ""),
                    obj.optString("genre", ""),
                    obj.optString("description", ""),
                    obj.optString("imageUrl", ""),
                    obj.optString("seasonsJson", "")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}