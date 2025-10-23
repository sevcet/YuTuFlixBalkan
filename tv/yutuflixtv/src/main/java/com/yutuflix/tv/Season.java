package com.yutuflix.tv;

import java.util.List;

public class Season {
    private int number;
    private List<Episode> episodes;

    public Season(int number, List<Episode> episodes) {
        this.number = number;
        this.episodes = episodes;
    }

    public int getNumber() { return number; }
    public List<Episode> getEpisodes() { return episodes; }
}