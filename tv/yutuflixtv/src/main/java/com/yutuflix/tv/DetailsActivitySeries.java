package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yutuflix.tv.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetailsActivitySeries extends Activity {

    private ImageView poster;
    private ImageButton favoriteButton;
    private TextView title, year, genre, desc;
    private GridLayout seasonsContainer;

    private String firstVideoId = null;

    private String seriesTitle;
    private String seriesYear;
    private String seriesGenre;
    private String seriesDesc;
    private String seriesImage;
    private String seasonsJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_series_tv);

        initViews();
        loadSeriesData();
        setupFavoriteButton();
        setupEpisodes();
    }

    private void initViews() {
        poster = findViewById(R.id.detailSeriesImage);
        favoriteButton = findViewById(R.id.favoriteIconSeries);
        title = findViewById(R.id.detailSeriesTitle);
        year = findViewById(R.id.detailSeriesYear);
        genre = findViewById(R.id.detailSeriesGenre);
        desc = findViewById(R.id.detailSeriesDesc);
        seasonsContainer = findViewById(R.id.seasonsContainer);

        // TV optimizacija - fokus
        favoriteButton.setFocusable(true);
        favoriteButton.setFocusableInTouchMode(true);
    }

    private void loadSeriesData() {
        Intent intent = getIntent();
        seriesTitle = intent.getStringExtra("title");
        seriesYear = intent.getStringExtra("year");
        seriesGenre = intent.getStringExtra("genre");
        seriesDesc = intent.getStringExtra("description");
        seriesImage = intent.getStringExtra("imageUrl");
        seasonsJson = intent.getStringExtra("seasonsJson");

        // Postavi osnovne podatke
        if (seriesTitle != null) title.setText(seriesTitle);
        if (seriesYear != null) year.setText("Godina: " + seriesYear);
        if (seriesGenre != null) genre.setText("Žanr: " + seriesGenre);
        if (seriesDesc != null) desc.setText(seriesDesc);

        // Učitaj sliku
        if (seriesImage != null && !seriesImage.isEmpty()) {
            Glide.with(this)
                    .load(seriesImage)
                    .placeholder(R.drawable.placeholder)
                    .into(poster);
        }
    }

    private void setupFavoriteButton() {
        updateFavoriteIcon();

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FavoritesManager.isFavorite(DetailsActivitySeries.this, seriesTitle)) {
                    FavoritesManager.removeFavorite(DetailsActivitySeries.this, seriesTitle);
                    Toast.makeText(DetailsActivitySeries.this, "Uklonjeno iz omiljenih", Toast.LENGTH_SHORT).show();
                } else {
                    SeriesItem item = new SeriesItem(
                            seriesTitle,
                            seriesYear,
                            seriesGenre,
                            seriesDesc,
                            seriesImage,
                            seasonsJson
                    );
                    FavoritesManager.addFavorite(DetailsActivitySeries.this, item);
                    Toast.makeText(DetailsActivitySeries.this, "Dodato u omiljene", Toast.LENGTH_SHORT).show();
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

    private void setupEpisodes() {
        try {
            if (seasonsJson != null && !seasonsJson.isEmpty()) {
                JSONArray seasonsArray = new JSONArray(seasonsJson);

                for (int s = 0; s < seasonsArray.length(); s++) {
                    JSONObject seasonObj = seasonsArray.getJSONObject(s);
                    int seasonNumber = seasonObj.getInt("number");
                    JSONArray episodesArray = seasonObj.getJSONArray("episodes");

                    for (int e = 0; e < episodesArray.length(); e++) {
                        JSONObject episodeObj = episodesArray.getJSONObject(e);
                        String epTitle = episodeObj.getString("title");
                        String epImage = episodeObj.getString("imageUrl");
                        String epVideoId = episodeObj.getString("videoId");

                        if (firstVideoId == null) {
                            firstVideoId = epVideoId;
                        }

                        LinearLayout epBox = createEpisodeBox(seasonNumber, e + 1, epTitle, epImage, epVideoId);
                        seasonsContainer.addView(epBox);
                    }
                }
            } else {
                // Ako nema sezona, prikaži poruku
                TextView noEpisodesText = new TextView(this);
                noEpisodesText.setText("Nema dostupnih epizoda");
                noEpisodesText.setTextColor(Color.WHITE);
                noEpisodesText.setTextSize(16);
                noEpisodesText.setPadding(20, 20, 20, 20);
                seasonsContainer.addView(noEpisodesText);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Greška pri učitavanju epizoda", Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout createEpisodeBox(int seasonNumber, int episodeNumber, String epTitle, String epImage, String epVideoId) {
        LinearLayout epBox = new LinearLayout(this);
        epBox.setOrientation(LinearLayout.VERTICAL);

        // TV optimizacija - veći padding i fokus
        epBox.setPadding(16, 16, 16, 16);
        epBox.setFocusable(true);
        epBox.setFocusableInTouchMode(true);
        epBox.setBackgroundResource(R.drawable.tv_button_background);

        // Layout params za TV
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 0;
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        layoutParams.setMargins(8, 8, 8, 8);
        epBox.setLayoutParams(layoutParams);

        // Slika epizode - veća za TV
        ImageView epImg = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(400, 225);
        epImg.setLayoutParams(imageParams);
        epImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (epImage != null && !epImage.isEmpty()) {
            Glide.with(this)
                    .load(epImage)
                    .placeholder(R.drawable.placeholder)
                    .into(epImg);
        } else {
            epImg.setImageResource(R.drawable.placeholder);
        }

        // Naslov epizode - veći font za TV
        TextView epLabel = new TextView(this);
        epLabel.setText("S" + seasonNumber + "E" + episodeNumber + " - " + epTitle);
        epLabel.setTextSize(16);
        epLabel.setTextColor(Color.WHITE);
        epLabel.setMaxLines(2);
        epLabel.setPadding(0, 8, 0, 0);

        epBox.addView(epImg);
        epBox.addView(epLabel);

        // Klik listener
        final String videoId = epVideoId;
        final String fullEpisodeTitle = "S" + seasonNumber + "E" + episodeNumber + " - " + epTitle;

        epBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playEpisode(videoId, fullEpisodeTitle);
            }
        });

        // TV focus listener za highlight
        epBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    epBox.setBackgroundResource(R.drawable.tv_button_focused);
                    // Scale animacija za fokus
                    epBox.setScaleX(1.05f);
                    epBox.setScaleY(1.05f);
                } else {
                    epBox.setBackgroundResource(R.drawable.tv_button_background);
                    epBox.setScaleX(1.0f);
                    epBox.setScaleY(1.0f);
                }
            }
        });

        return epBox;
    }

    private void playEpisode(String videoId, String episodeTitle) {
        if (videoId != null && !videoId.isEmpty()) {
            String youtubeUrl = "https://www.youtube.com/embed/" + videoId;
            Intent intent = new Intent(DetailsActivitySeries.this, PlayerActivity.class);
            intent.putExtra("videoUrl", youtubeUrl);
            intent.putExtra("videoTitle", episodeTitle);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Epizoda nije dostupna", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteIcon() {
        if (seriesTitle != null && FavoritesManager.isFavorite(this, seriesTitle)) {
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

        // Fokus na prvi element
        if (seasonsContainer.getChildCount() > 0) {
            seasonsContainer.getChildAt(0).requestFocus();
        } else {
            favoriteButton.requestFocus();
        }
    }
}