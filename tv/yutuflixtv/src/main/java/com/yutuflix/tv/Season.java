package com.yutuflix.tv;

import java.util.List;

public class Season {
    private int number;
    private List<com.yutuflix.tv.Episode> episodes;

    public Season(int number, List<com.yutuflix.tv.Episode> episodes) {
        this.number = number;
        this.episodes = episodes;
    }

    public int getNumber() { return number; }
    public List<com.yutuflix.tv.Episode> getEpisodes() { return episodes; }
}

