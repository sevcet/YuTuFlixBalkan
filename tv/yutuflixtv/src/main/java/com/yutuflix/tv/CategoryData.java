package com.yutuflix.tv;

import java.util.List;

public class CategoryData {
    private String categoryName;
    private List<Movie> movies;

    public CategoryData(String categoryName, List<Movie> movies) {
        this.categoryName = categoryName;
        this.movies = movies;
    }

    public String getCategoryName() { return categoryName; }
    public List<Movie> getMovies() { return movies; }
}