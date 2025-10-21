package com.ssait.yutuflixbalkan;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Pronađi view-ove
        recyclerView = findViewById(R.id.favoritesRecyclerView);
        btnBack = findViewById(R.id.btnBackFavorites);

        // Postavi layout manager za RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Učitaj sve favorite iz SharedPreferences
        List<RecyclerItem> favorites = FavoritesManager.getFavorites(this);

        // Postavi adapter
        adapter = new FavoritesAdapter(this, favorites);
        recyclerView.setAdapter(adapter);

        // Dugme za nazad
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
}