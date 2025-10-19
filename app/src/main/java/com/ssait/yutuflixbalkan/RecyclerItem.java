package com.ssait.yutuflixbalkan;

import org.json.JSONObject;

/**
 * Model za video/seriju koji sadrži metode za serijalizaciju u JSON i obrnut proces.
 */
public class RecyclerItem {
    private String title;
    private String year;
    private String genre;
    private String description;
    private String imageUrl;
    private String videoId;      // za filmove
    private boolean isSeries;    // true ako je serija
    private String seasonsJson;  // za serije (JSON niz sezona/epizoda)

    public RecyclerItem(String title, String year, String genre, String description,
                        String imageUrl, String videoId, boolean isSeries, String seasonsJson) {
        this.title = title != null ? title : "";
        this.year = year != null ? year : "";
        this.genre = genre != null ? genre : "";
        this.description = description != null ? description : "";
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.videoId = videoId;
        this.isSeries = isSeries;
        this.seasonsJson = seasonsJson;
    }

    // --- Getteri ---
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoId() { return videoId; }
    public boolean isSeries() { return isSeries; }
    public String getSeasonsJson() { return seasonsJson; }

    // --- JSON serijalizacija ---
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("title", title);
            obj.put("year", year);
            obj.put("genre", genre);
            obj.put("description", description);
            obj.put("imageUrl", imageUrl);
            // videoId i seasonsJson mogu biti null -> stavimo JSONObject.NULL
            if (videoId != null) obj.put("videoId", videoId); else obj.put("videoId", JSONObject.NULL);
            obj.put("isSeries", isSeries);
            if (seasonsJson != null) obj.put("seasonsJson", seasonsJson); else obj.put("seasonsJson", JSONObject.NULL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Kreira RecyclerItem iz JSONObject-a. Očekuje oblik koji proizvodi toJson().
     */
    public static RecyclerItem fromJson(JSONObject obj) {
        if (obj == null) return null;
        try {
            String title = obj.optString("title", "");
            String year = obj.optString("year", "");
            String genre = obj.optString("genre", "");
            String description = obj.optString("description", "");
            String imageUrl = obj.optString("imageUrl", "");

            String videoId = null;
            if (obj.has("videoId") && !obj.isNull("videoId")) {
                String v = obj.optString("videoId", "");
                if (!v.isEmpty()) videoId = v;
            }

            boolean isSeries = obj.optBoolean("isSeries", false);

            String seasonsJson = null;
            if (obj.has("seasonsJson") && !obj.isNull("seasonsJson")) {
                String s = obj.optString("seasonsJson", "");
                if (!s.isEmpty()) seasonsJson = s;
            }

            return new RecyclerItem(title, year, genre, description, imageUrl, videoId, isSeries, seasonsJson);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- equals/hashCode (koristi videoId kad postoji, inače title+isSeries) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecyclerItem)) return false;
        RecyclerItem other = (RecyclerItem) o;

        if (videoId != null && other.videoId != null) {
            return videoId.equals(other.videoId);
        }
        // fallback
        return this.isSeries == other.isSeries &&
                this.title.equals(other.title);
    }

    @Override
    public int hashCode() {
        if (videoId != null) return videoId.hashCode();
        int result = title.hashCode();
        result = 31 * result + (isSeries ? 1 : 0);
        return result;
    }
}

