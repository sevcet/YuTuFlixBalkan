package com.yutuflix.tv;

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
    private List<Season> seasons;
    private String seasonsJson;
    private String availableCaptions;
    private String audioLanguage;

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
        this.availableCaptions = "";
        this.audioLanguage = "";
    }

    public Movie(String title, String year, String genre, String type,
                 String description, String imageUrl, String videoId,
                 List<Season> seasons, String seasonsJson,
                 String availableCaptions, String audioLanguage) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.type = type;
        this.description = description;
        this.imageUrl = imageUrl;
        this.videoId = videoId;
        this.seasons = seasons;
        this.seasonsJson = seasonsJson;
        this.availableCaptions = availableCaptions;
        this.audioLanguage = audioLanguage;
    }

    // GETTERI
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoId() { return videoId; }
    public List<Season> getSeasons() { return seasons; }
    public String getSeasonsJson() { return seasonsJson; }
    public String getAvailableCaptions() { return availableCaptions; }
    public String getAudioLanguage() { return audioLanguage; }

    // SETTERI
    public void setAvailableCaptions(String availableCaptions) {
        this.availableCaptions = availableCaptions;
    }

    public void setAudioLanguage(String audioLanguage) {
        this.audioLanguage = audioLanguage;
    }

    // POMOĆNE METODE ZA JEZIKE
    public boolean hasPreferredLanguage(List<String> preferredLanguages) {
        // Proveri audio jezik
        if (audioLanguage != null && !audioLanguage.isEmpty()) {
            if (preferredLanguages.contains(audioLanguage)) {
                return true;
            }
        }

        // Proveri dostupne titlove
        if (availableCaptions != null && !availableCaptions.isEmpty()) {
            String[] captions = availableCaptions.split(",");
            for (String caption : captions) {
                String cleanCaption = caption.trim();
                if (preferredLanguages.contains(cleanCaption)) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<String> getAvailableCaptionList() {
        List<String> captionList = new java.util.ArrayList<>();
        if (availableCaptions != null && !availableCaptions.isEmpty()) {
            String[] captions = availableCaptions.split(",");
            for (String caption : captions) {
                captionList.add(caption.trim());
            }
        }
        return captionList;
    }

    // PARCELABLE
    protected Movie(Parcel in) {
        title = in.readString();
        year = in.readString();
        genre = in.readString();
        type = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        videoId = in.readString();
        seasonsJson = in.readString();
        availableCaptions = in.readString();
        audioLanguage = in.readString();
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
        dest.writeString(availableCaptions);
        dest.writeString(audioLanguage);
    }

    @Override
    public int describeContents() { return 0; }

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

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", genre='" + genre + '\'' +
                ", type='" + type + '\'' +
                ", videoId='" + videoId + '\'' +
                ", availableCaptions='" + availableCaptions + '\'' +
                ", audioLanguage='" + audioLanguage + '\'' +
                '}';
    }
}