package com.yutuflix.tv;

public class VideoItem {
    private String title;
    private String videoUrl;
    private String thumbnail;
    private String description;
    private String year;
    private String genre;
    private String type; // "film" ili "serija"

    public VideoItem(String title, String videoUrl, String thumbnail) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.thumbnail = thumbnail;
    }

    public VideoItem(String title, String videoUrl, String thumbnail, String description, String year, String genre, String type) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.thumbnail = thumbnail;
        this.description = description;
        this.year = year;
        this.genre = genre;
        this.type = type;
    }

    // Getters
    public String getTitle() { return title; }
    public String getVideoUrl() { return videoUrl; }
    public String getThumbnail() { return thumbnail; }
    public String getDescription() { return description; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getType() { return type; }

    // Setters
    public void setDescription(String description) { this.description = description; }
    public void setYear(String year) { this.year = year; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setType(String type) { this.type = type; }
}