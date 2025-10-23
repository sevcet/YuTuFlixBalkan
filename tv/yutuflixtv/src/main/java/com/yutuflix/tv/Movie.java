package com.yutuflix.tv;

import java.util.List;

public class Movie {
    private String title;
    private String year;
    private String genre;
    private String type;
    private String description;
    private String imageUrl;
    private String videoId;
    private List<Season> seasons;
    private String seasonsJson;

    public Movie(String title, String year, String genre, String type, String description,
                 String imageUrl, String videoId, List<Season> seasons, String seasonsJson) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.type = type;
        this.description = description;
        this.imageUrl = imageUrl;
        this.videoId = videoId;
        this.seasons = seasons;
        this.seasonsJson = seasonsJson;
    }

    // Getters
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoId() { return videoId; }
    public List<Season> getSeasons() { return seasons; }
    public String getSeasonsJson() { return seasonsJson; }
}