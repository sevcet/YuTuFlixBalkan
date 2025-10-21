package com.ssait.yutuflixbalkan;

public class CategoryHolder {
    private static CategoryData currentCategory;

    public static void setCurrentCategory(CategoryData category) {
        currentCategory = category;
    }

    public static CategoryData getCurrentCategory() {
        return currentCategory;
    }
}

