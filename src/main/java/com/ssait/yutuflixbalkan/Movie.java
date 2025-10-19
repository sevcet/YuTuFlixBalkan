package com.ssait.yutuflixbalkan;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Movie implements Parcelable {

    private String title;
    private String year;
    private String genre;
    private String type;
    private String description;
    private String imageUrl;
    private String videoId;
    private List<Season> seasons; // za serije
    private String seasonsJson;

    public Movie(String title, String year, String genre, String type,
                 String description, String imageUrl, String videoId,
                 List<Season> seasons, String seasonsJson) {
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

    // --- GETTERI ---
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoId() { return videoId; }
    public List<Season> getSeasons() { return seasons; }
    public String getSeasonsJson() { return seasonsJson; }

    // --- PARCELABLE IMPLEMENTACIJA ---
    protected Movie(Parcel in) {
        title = in.readString();
        year = in.readString();
        genre = in.readString();
        type = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        videoId = in.readString();
        seasonsJson = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(year);
        dest.writeString(genre);
        dest.writeString(type);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(videoId);
        dest.writeString(seasonsJson);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}

