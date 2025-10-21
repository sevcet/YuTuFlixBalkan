package com.ssait.yutuflixbalkan;

import java.util.List;

public class CategoryData {
    private String name;
    private List<Movie> items;

    public CategoryData(String name, List<Movie> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public List<Movie> getItems() {
        return items;
    }
}
