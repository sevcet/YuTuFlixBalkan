package com.yutuflix.tv;

import org.json.JSONException;
import org.json.JSONObject;

public class RecyclerItem {
    private String title;
    private String year;
    private String genre;
    private String description;
    private String imageUrl;
    private String videoId;
    private String seasonsJson;
    private boolean isSeries;

    public RecyclerItem(String title, String year, String genre, String description,
                        String imageUrl, String videoId, String seasonsJson, boolean isSeries) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.description = description;
        this.imageUrl = imageUrl;
        this.videoId = videoId;
        this.seasonsJson = seasonsJson;
        this.isSeries = isSeries;
    }

    // Getters
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoId() { return videoId; }
    public String getSeasonsJson() { return seasonsJson; }
    public boolean isSeries() { return isSeries; }

    // Konvertuj u JSON
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("title", title);
            obj.put("year", year);
            obj.put("genre", genre);
            obj.put("description", description);
            obj.put("imageUrl", imageUrl);
            obj.put("videoId", videoId);
            obj.put("seasonsJson", seasonsJson);
            obj.put("isSeries", isSeries);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    // Kreiraj iz JSON
    public static RecyclerItem fromJson(JSONObject obj) {
        try {
            String title = obj.optString("title", "");
            String year = obj.optString("year", "");
            String genre = obj.optString("genre", "");
            String description = obj.optString("description", "");
            String imageUrl = obj.optString("imageUrl", "");
            String videoId = obj.optString("videoId", "");
            String seasonsJson = obj.optString("seasonsJson", "");
            boolean isSeries = obj.optBoolean("isSeries", false);

            return new RecyclerItem(title, year, genre, description, imageUrl, videoId, seasonsJson, isSeries);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}