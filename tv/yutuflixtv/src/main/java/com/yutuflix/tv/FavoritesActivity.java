package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoritesActivity extends Activity {

    private RecyclerView favoritesRecyclerView;
    private LinearLayout emptyLayout;
    private Button btnBack;
    private FavoritesAdapter favoritesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_tv);

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyLayout = findViewById(R.id.emptyLayout);
        btnBack = findViewById(R.id.btnBack);

        setupRecyclerView();
        loadFavorites();

        btnBack.setOnClickListener(v -> finish());

        // TV focus
        btnBack.setFocusable(true);
        btnBack.setFocusableInTouchMode(true);
    }

    private void setupRecyclerView() {
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesAdapter = new FavoritesAdapter(this, new FavoritesAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(RecyclerItem item) {
                openItemDetails(item);
            }

            @Override
            public void onRemoveFavorite(RecyclerItem item) {
                FavoritesManager.removeFavorite(FavoritesActivity.this, getItemKey(item));
                loadFavorites();
            }
        });
        favoritesRecyclerView.setAdapter(favoritesAdapter);
    }

    private void loadFavorites() {
        List<RecyclerItem> favorites = FavoritesManager.getFavorites(this);

        if (favorites.isEmpty()) {
            favoritesRecyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            favoritesRecyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
            favoritesAdapter.updateFavorites(favorites);
        }
    }

    private void openItemDetails(RecyclerItem item) {
        if (item.isSeries()) {
            Intent intent = new Intent(this, DetailsActivitySeries.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("year", item.getYear());
            intent.putExtra("genre", item.getGenre());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("imageUrl", item.getImageUrl());
            intent.putExtra("seasonsJson", item.getSeasonsJson());
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, DetailsActivityFilm.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("year", item.getYear());
            intent.putExtra("genre", item.getGenre());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("imageUrl", item.getImageUrl());
            intent.putExtra("videoId", item.getVideoId());
            startActivity(intent);
        }
    }

    private String getItemKey(RecyclerItem item) {
        if (item == null) return null;
        if (item.getVideoId() != null && !item.getVideoId().isEmpty()) {
            return item.getVideoId(); // filmovi
        } else {
            return item.getTitle();   // serije
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
        btnBack.requestFocus();
    }
}