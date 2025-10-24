package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
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

        // DEBUG: Proveri šta stiže
        Log.d("SERIES_DEBUG", "Title: " + seriesTitle);
        Log.d("SERIES_DEBUG", "SeasonsJson received: " + (seasonsJson != null ? "YES" : "NO"));
        if (seasonsJson != null && seasonsJson.length() > 0) {
            Log.d("SERIES_DEBUG", "SeasonsJson length: " + seasonsJson.length());
            Log.d("SERIES_DEBUG", "First 100 chars: " + seasonsJson.substring(0, Math.min(seasonsJson.length(), 100)));
        }

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
            seasonsContainer.removeAllViews(); // Očisti prethodni sadržaj

            if (seasonsJson != null && !seasonsJson.isEmpty()) {
                // Očisti JSON string
                String cleanedJson = seasonsJson.trim();
                Log.d("SERIES_DEBUG", "Cleaned JSON length: " + cleanedJson.length());

                // Proveri da li JSON počinje i završava se sa uglastim zagradama
                if (!cleanedJson.startsWith("[") || !cleanedJson.endsWith("]")) {
                    Toast.makeText(this, "Neispravan JSON format", Toast.LENGTH_LONG).show();
                    Log.e("SERIES_ERROR", "Invalid JSON format");
                    showNoEpisodesMessage("Neispravan format podataka");
                    return;
                }

                JSONArray seasonsArray = new JSONArray(cleanedJson);
                Log.d("SERIES_DEBUG", "Number of seasons: " + seasonsArray.length());

                if (seasonsArray.length() == 0) {
                    showNoEpisodesMessage("Nema sezona u seriji");
                    return;
                }

                int totalEpisodes = 0;
                int maxEpisodes = 500; // POVEĆAN LIMIT NA 500

                for (int s = 0; s < seasonsArray.length(); s++) {
                    JSONObject seasonObj = seasonsArray.getJSONObject(s);
                    int seasonNumber = seasonObj.getInt("number");
                    JSONArray episodesArray = seasonObj.getJSONArray("episodes");
                    Log.d("SERIES_DEBUG", "Season " + seasonNumber + " has " + episodesArray.length() + " episodes");

                    if (episodesArray.length() == 0) {
                        continue; // Preskoči prazne sezone
                    }

                    // Dodaj naslov sezone
                    addSeasonTitle("Sezona " + seasonNumber);

                    // Kreiraj horizontalni scroll container za epizode
                    HorizontalScrollView horizontalScrollView = createHorizontalScrollView();
                    LinearLayout episodesLayout = createEpisodesLayout();

                    for (int e = 0; e < episodesArray.length(); e++) {
                        // Proveri limit od 500 epizoda
                        if (totalEpisodes >= maxEpisodes) {
                            Log.d("SERIES_DEBUG", "Dosegnut limit od " + maxEpisodes + " epizoda");
                            break;
                        }

                        JSONObject episodeObj = episodesArray.getJSONObject(e);
                        String epTitle = episodeObj.getString("title");
                        String epImage = episodeObj.getString("imageUrl");
                        String epVideoId = episodeObj.getString("videoId");

                        Log.d("SERIES_DEBUG", "Episode " + (e+1) + ": " + epTitle);

                        if (firstVideoId == null) {
                            firstVideoId = epVideoId;
                        }

                        LinearLayout epBox = createEpisodeBox(seasonNumber, e + 1, epTitle, epImage, epVideoId);
                        episodesLayout.addView(epBox);

                        totalEpisodes++;
                    }

                    horizontalScrollView.addView(episodesLayout);
                    seasonsContainer.addView(horizontalScrollView);

                    // Proveri da li je dosegnut limit
                    if (totalEpisodes >= maxEpisodes) {
                        // Dodaj poruku o ograničenju
                        TextView limitMessage = new TextView(this);
                        limitMessage.setText("Prikazano " + maxEpisodes + " od ukupno epizoda");
                        limitMessage.setTextColor(Color.GRAY);
                        limitMessage.setTextSize(12);
                        limitMessage.setPadding(20, 10, 20, 20);
                        seasonsContainer.addView(limitMessage);
                        break;
                    }
                }

                Log.d("SERIES_DEBUG", "Ukupno prikazano epizoda: " + totalEpisodes);

            } else {
                showNoEpisodesMessage("Nema dostupnih epizoda");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SERIES_ERROR", "Error loading episodes: " + e.getMessage(), e);
            Toast.makeText(this, "Greška pri učitavanju epizoda: " + e.getMessage(), Toast.LENGTH_LONG).show();
            showNoEpisodesMessage("Greška: " + e.getMessage());
        }
    }

    private void addSeasonTitle(String title) {
        TextView seasonTitle = new TextView(this);
        seasonTitle.setText(title);
        seasonTitle.setTextColor(Color.WHITE);
        seasonTitle.setTextSize(16);
        seasonTitle.setTypeface(null, Typeface.BOLD);
        seasonTitle.setPadding(16, 16, 16, 8);
        seasonsContainer.addView(seasonTitle);
    }

    private HorizontalScrollView createHorizontalScrollView() {
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        scrollView.setPadding(8, 0, 8, 16);
        return scrollView;
    }

    private LinearLayout createEpisodesLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return layout;
    }

    private void showNoEpisodesMessage(String message) {
        TextView noEpisodesText = new TextView(this);
        noEpisodesText.setText(message);
        noEpisodesText.setTextColor(Color.WHITE);
        noEpisodesText.setTextSize(14);
        noEpisodesText.setPadding(20, 20, 20, 20);
        seasonsContainer.addView(noEpisodesText);
    }

    private LinearLayout createEpisodeBox(int seasonNumber, int episodeNumber, String epTitle, String epImage, String epVideoId) {
        LinearLayout epBox = new LinearLayout(this);
        epBox.setOrientation(LinearLayout.VERTICAL);

        // TV optimizacija - veći padding i fokus
        epBox.setPadding(12, 12, 12, 12);
        epBox.setFocusable(true);
        epBox.setFocusableInTouchMode(true);
        epBox.setBackgroundResource(R.drawable.tv_button_background);

        // Layout params za TV
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                220, // fiksna širina za TV
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(6, 6, 6, 6);
        epBox.setLayoutParams(layoutParams);

        // Slika epizode - 16:9 aspect ratio
        ImageView epImg = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(180, 101);
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

        // Naslov epizode
        TextView epLabel = new TextView(this);
        epLabel.setText("S" + seasonNumber + "E" + episodeNumber + "\n" + epTitle);
        epLabel.setTextSize(12);
        epLabel.setTextColor(Color.WHITE);
        epLabel.setMaxLines(3);
        epLabel.setPadding(0, 6, 0, 0);

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
        if (seasonsContainer.getChildCount() > 1) {
            // Prvo dete je naslov, drugo je HorizontalScrollView
            View secondChild = seasonsContainer.getChildAt(1);
            if (secondChild instanceof HorizontalScrollView) {
                HorizontalScrollView scrollView = (HorizontalScrollView) secondChild;
                LinearLayout episodesLayout = (LinearLayout) scrollView.getChildAt(0);
                if (episodesLayout.getChildCount() > 0) {
                    episodesLayout.getChildAt(0).requestFocus();
                }
            }
        } else {
            favoriteButton.requestFocus();
        }
    }
}