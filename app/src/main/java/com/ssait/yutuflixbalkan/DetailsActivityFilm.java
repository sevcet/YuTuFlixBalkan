package com.ssait.yutuflixbalkan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DetailsActivityFilm extends AppCompatActivity {

    private ImageView poster;
    private ImageButton favoriteButton;
    private TextView title, year, genre, desc;
    private Button playFilmButton;

    private String videoId;
    private String movieTitle;
    private String movieImage;
    private String movieYear;
    private String movieGenre;
    private String movieDesc;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_film);

        // View reference
        poster = findViewById(R.id.detailFilmImage);
        title = findViewById(R.id.detailFilmTitle);
        year = findViewById(R.id.detailFilmYear);
        genre = findViewById(R.id.detailFilmGenre);
        desc = findViewById(R.id.detailFilmDesc);
        playFilmButton = findViewById(R.id.playFilmButton);
        favoriteButton = findViewById(R.id.favoriteIconFilm);

        // Preuzmi podatke iz Intent-a
        Intent i = getIntent();
        movieTitle = i.getStringExtra("title");
        movieYear = i.getStringExtra("year");
        movieGenre = i.getStringExtra("genre");
        movieDesc = i.getStringExtra("description");
        movieImage = i.getStringExtra("imageUrl");
        videoId = i.getStringExtra("videoId");

        // Postavi tekstove i sliku
        if (movieTitle != null) title.setText(movieTitle);
        year.setText("Godina: " + (movieYear != null ? movieYear : ""));
        if (movieGenre != null) genre.setText("Žanr: " + movieGenre);
        if (movieDesc != null) desc.setText(movieDesc);

        if (movieImage != null && !movieImage.isEmpty()) {
            Glide.with(this).load(movieImage).placeholder(R.drawable.placeholder).into(poster);
        } else {
            poster.setImageResource(R.drawable.placeholder);
        }

        // init favorite icon prema trenutnom stanju
        updateFavoriteIcon();

        // klik na srce - toggle favorite
        favoriteButton.setOnClickListener(v -> {
            if (videoId == null) return;

            if (FavoritesManager.isFavorite(this, videoId)) {
                FavoritesManager.removeFavorite(this, videoId);
            } else {
                RecyclerItem item = new RecyclerItem(
                        movieTitle != null ? movieTitle : "",
                        movieYear != null ? movieYear : "",
                        movieGenre != null ? movieGenre : "",
                        movieDesc != null ? movieDesc : "",
                        movieImage != null ? movieImage : "",
                        videoId,
                        false,
                        null
                );
                FavoritesManager.addFavorite(this, item);
            }
            updateFavoriteIcon();
        });

        // klik na Pusti film
        playFilmButton.setOnClickListener(v -> playFilm());

        // GestureDetector za swipe - POJEDOSTAVLJENA VERZIJA
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();

                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                // SWIPE DESNO: leva -> desna strana ekrana
                                // VRATI SE NAZAD
                                finish();
                                return true;
                            } else {
                                // SWIPE LEVO: desna -> leva strana ekrana
                                // POKRENI FILM
                                playFilm();
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector != null) {
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    // Dodajemo i onDispatchTouchEvent za bolje hvatanje gestova
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void playFilm() {
        if (videoId == null || videoId.isEmpty()) return;
        Intent intent = new Intent(DetailsActivityFilm.this, PlayerActivity.class);
        intent.putExtra("videoId", videoId);
        startActivity(intent);
    }

    private void updateFavoriteIcon() {
        if (videoId == null) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            favoriteButton.clearColorFilter();
            return;
        }

        boolean fav = false;
        try {
            fav = FavoritesManager.isFavorite(this, videoId);
        } catch (Exception ignored) {}

        if (fav) {
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
    }
}
