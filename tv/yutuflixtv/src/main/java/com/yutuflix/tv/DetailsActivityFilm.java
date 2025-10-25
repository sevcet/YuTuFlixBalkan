package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class DetailsActivityFilm extends Activity {

    private ImageView poster;
    private ImageButton favoriteButton;
    private TextView title, year, genre, desc;
    private Button playButton;

    private String videoId;
    private String movieTitle;
    private String movieImage;
    private String movieYear;
    private String movieGenre;
    private String movieDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_film_tv);

        initViews();
        loadMovieData();
        setupFavoriteButton();
        setupPlayButton();
    }

    private void initViews() {
        poster = findViewById(R.id.detailFilmImage);
        favoriteButton = findViewById(R.id.favoriteIconFilm);
        title = findViewById(R.id.detailFilmTitle);
        year = findViewById(R.id.detailFilmYear);
        genre = findViewById(R.id.detailFilmGenre);
        desc = findViewById(R.id.detailFilmDesc);
        playButton = findViewById(R.id.playFilmButton);

        // TV optimizacija - fokus
        favoriteButton.setFocusable(true);
        favoriteButton.setFocusableInTouchMode(true);
        playButton.setFocusable(true);
        playButton.setFocusableInTouchMode(true);
    }

    private void loadMovieData() {
        Intent intent = getIntent();
        movieTitle = intent.getStringExtra("title");
        movieYear = intent.getStringExtra("year");
        movieGenre = intent.getStringExtra("genre");
        movieDesc = intent.getStringExtra("description");
        movieImage = intent.getStringExtra("imageUrl");
        videoId = intent.getStringExtra("videoId");

        // Postavi podatke
        if (movieTitle != null) title.setText(movieTitle);
        if (movieYear != null) year.setText("Godina: " + movieYear);
        if (movieGenre != null) genre.setText("Žanr: " + movieGenre);
        if (movieDesc != null) desc.setText(movieDesc);

        // Učitaj sliku
        if (movieImage != null && !movieImage.isEmpty()) {
            Glide.with(this)
                    .load(movieImage)
                    .placeholder(R.drawable.placeholder)
                    .into(poster);
        }

        // Ažuriraj favorite ikonicu
        updateFavoriteIcon();
    }

    private void setupFavoriteButton() {
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemKey = getItemKey();
                if (FavoritesManager.isFavorite(DetailsActivityFilm.this, itemKey)) {
                    FavoritesManager.removeFavorite(DetailsActivityFilm.this, itemKey);
                    Toast.makeText(DetailsActivityFilm.this, "Uklonjeno iz omiljenih", Toast.LENGTH_SHORT).show();
                } else {
                    RecyclerItem item = new RecyclerItem(
                            movieTitle,
                            movieYear,
                            movieGenre,
                            movieDesc,
                            movieImage,
                            videoId,
                            "", // seasonsJson je prazan za filmove
                            false // isSeries = false za filmove
                    );
                    FavoritesManager.addFavorite(DetailsActivityFilm.this, item);
                    Toast.makeText(DetailsActivityFilm.this, "Dodato u omiljene", Toast.LENGTH_SHORT).show();
                }
                updateFavoriteIcon();
            }
        });

        // TV navigacija - focus listener
        favoriteButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    favoriteButton.setBackgroundResource(R.drawable.tv_button_focused);
                } else {
                    favoriteButton.setBackgroundResource(R.drawable.tv_button_background);
                }
            }
        });
    }

    private void setupPlayButton() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playFilm();
            }
        });

        // TV focus listener za play button
        playButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    playButton.setBackgroundResource(R.drawable.tv_button_focused);
                    playButton.setScaleX(1.05f);
                    playButton.setScaleY(1.05f);
                } else {
                    playButton.setBackgroundResource(R.drawable.tv_button_background);
                    playButton.setScaleX(1.0f);
                    playButton.setScaleY(1.0f);
                }
            }
        });
    }

    private String getItemKey() {
        // Za filmove koristimo videoId kao ključ
        return videoId;
    }

    private void playFilm() {
        if (videoId != null && !videoId.isEmpty()) {
            // Koristi YouTube embed URL za TV
            String youtubeUrl = "https://www.youtube.com/embed/" + videoId;
            Intent intent = new Intent(DetailsActivityFilm.this, PlayerActivity.class);
            intent.putExtra("videoUrl", youtubeUrl);
            intent.putExtra("videoTitle", movieTitle != null ? movieTitle : "Film");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Video nije dostupan", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteIcon() {
        String itemKey = getItemKey();
        if (itemKey != null && FavoritesManager.isFavorite(this, itemKey)) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
            favoriteButton.setColorFilter(Color.RED);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            favoriteButton.clearColorFilter();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFavoriteIcon();

        // Postavi fokus na play button
        playButton.requestFocus();
    }
}