package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class DetailsActivityFilm extends Activity {

    private ImageView poster;
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
        setupPlayButton();
    }

    private void initViews() {
        poster = findViewById(R.id.detailFilmImage);
        title = findViewById(R.id.detailFilmTitle);
        year = findViewById(R.id.detailFilmYear);
        genre = findViewById(R.id.detailFilmGenre);
        desc = findViewById(R.id.detailFilmDesc);
        playButton = findViewById(R.id.playFilmButton);
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
    }

    private void setupPlayButton() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playFilm();
            }
        });

        // Fokus za TV
        playButton.setFocusable(true);
        playButton.setFocusableInTouchMode(true);
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
}