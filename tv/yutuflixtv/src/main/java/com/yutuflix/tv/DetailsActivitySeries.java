package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetailsActivitySeries extends Activity {

    private ImageView poster;
    private TextView title, year, genre, desc;
    private LinearLayout seasonsContainer;

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
        setupEpisodes();
    }

    private void initViews() {
        poster = findViewById(R.id.detailSeriesImage);
        title = findViewById(R.id.detailSeriesTitle);
        year = findViewById(R.id.detailSeriesYear);
        genre = findViewById(R.id.detailSeriesGenre);
        desc = findViewById(R.id.detailSeriesDesc);
        seasonsContainer = findViewById(R.id.seasonsContainer);
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

    private void setupEpisodes() {
        try {
            if (seasonsJson != null && !seasonsJson.isEmpty()) {
                JSONArray seasonsArray = new JSONArray(seasonsJson);

                for (int s = 0; s < seasonsArray.length(); s++) {
                    JSONObject seasonObj = seasonsArray.getJSONObject(s);
                    int seasonNumber = seasonObj.getInt("number");

                    // Kreiraj layout za sezonu
                    LinearLayout seasonLayout = createSeasonLayout(seasonNumber);

                    JSONArray episodesArray = seasonObj.getJSONArray("episodes");
                    LinearLayout episodesRow = null;

                    for (int e = 0; e < episodesArray.length(); e++) {
                        JSONObject episodeObj = episodesArray.getJSONObject(e);
                        String epTitle = episodeObj.getString("title");
                        String epImage = episodeObj.getString("imageUrl");
                        String epVideoId = episodeObj.getString("videoId");

                        if (firstVideoId == null) {
                            firstVideoId = epVideoId;
                        }

                        // Kreiraj novi red svake 4 epizode
                        if (e % 4 == 0) {
                            episodesRow = new LinearLayout(this);
                            episodesRow.setOrientation(LinearLayout.HORIZONTAL);
                            episodesRow.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                            seasonLayout.addView(episodesRow);
                        }

                        // Kreiraj epizodu
                        if (episodesRow != null) {
                            View episodeView = createEpisodeView(seasonNumber, e + 1, epTitle, epImage, epVideoId);
                            episodesRow.addView(episodeView);
                        }
                    }

                    seasonsContainer.addView(seasonLayout);
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

    private LinearLayout createSeasonLayout(int seasonNumber) {
        LinearLayout seasonLayout = new LinearLayout(this);
        seasonLayout.setOrientation(LinearLayout.VERTICAL);
        seasonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        seasonLayout.setPadding(0, 20, 0, 10);

        // Naslov sezone
        TextView seasonTitle = new TextView(this);
        seasonTitle.setText("Sezona " + seasonNumber);
        seasonTitle.setTextColor(Color.WHITE);
        seasonTitle.setTextSize(18);
        seasonTitle.setTypeface(null, Typeface.BOLD); // POPRAVLJENA LINIJA
        seasonTitle.setPadding(0, 0, 0, 10);
        seasonLayout.addView(seasonTitle);

        return seasonLayout;
    }

    private View createEpisodeView(int seasonNumber, int episodeNumber, String epTitle, String epImage, String epVideoId) {
        LinearLayout episodeLayout = new LinearLayout(this);
        episodeLayout.setOrientation(LinearLayout.VERTICAL);
        episodeLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        episodeLayout.setPadding(10, 10, 10, 10);
        episodeLayout.setBackgroundResource(R.drawable.tv_button_background);

        // TV fokus
        episodeLayout.setFocusable(true);
        episodeLayout.setFocusableInTouchMode(true);

        // Slika epizode
        ImageView episodeImage = new ImageView(this);
        episodeImage.setLayoutParams(new LinearLayout.LayoutParams(
                200,
                120
        ));
        episodeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (epImage != null && !epImage.isEmpty()) {
            Glide.with(this)
                    .load(epImage)
                    .placeholder(R.drawable.placeholder)
                    .into(episodeImage);
        } else {
            episodeImage.setImageResource(R.drawable.placeholder);
        }

        // Naslov epizode
        TextView episodeTitle = new TextView(this);
        episodeTitle.setText("S" + seasonNumber + "E" + episodeNumber);
        episodeTitle.setTextColor(Color.WHITE);
        episodeTitle.setTextSize(12);
        episodeTitle.setPadding(0, 5, 0, 0);
        episodeTitle.setMaxLines(2);
        episodeTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);

        // Dodaj u layout
        episodeLayout.addView(episodeImage);
        episodeLayout.addView(episodeTitle);

        // Klik listener
        final String videoId = epVideoId;
        episodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playEpisode(videoId, "S" + seasonNumber + "E" + episodeNumber + " - " + epTitle);
            }
        });

        return episodeLayout;
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
}