package com.ssait.yutuflixbalkan;

public class Episode {
    private String title;
    private String imageUrl;
    private String videoId;

    public Episode(String title, String imageUrl, String videoId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.videoId = videoId;
    }

    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoId() { return videoId; }
}

