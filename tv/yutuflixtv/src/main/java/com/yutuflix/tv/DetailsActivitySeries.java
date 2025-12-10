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

        Log.d("SERIES_DEBUG", "Title: " + seriesTitle);
        Log.d("SERIES_DEBUG", "SeasonsJson received: " + (seasonsJson != null));

        if (seriesTitle != null) title.setText(seriesTitle);
        if (seriesYear != null) year.setText("Godina: " + seriesYear);
        if (seriesGenre != null) genre.setText("Žanr: " + seriesGenre);
        if (seriesDesc != null) desc.setText(seriesDesc);

        if (seriesImage != null && !seriesImage.isEmpty()) {
            Glide.with(this)
                    .load(seriesImage)
                    .placeholder(R.drawable.placeholder)
                    .into(poster);
        }
    }

    private void setupFavoriteButton() {
        updateFavoriteIcon();

        favoriteButton.setOnClickListener(v -> {
            String itemKey = getItemKey();
            if (FavoritesManager.isFavorite(DetailsActivitySeries.this, itemKey)) {
                FavoritesManager.removeFavorite(DetailsActivitySeries.this, itemKey);
                Toast.makeText(DetailsActivitySeries.this,
                        "Uklonjeno iz omiljenih", Toast.LENGTH_SHORT).show();
            } else {
                RecyclerItem item = new RecyclerItem(
                        seriesTitle,
                        seriesYear,
                        seriesGenre,
                        seriesDesc,
                        seriesImage,
                        "",
                        seasonsJson,
                        true
                );
                FavoritesManager.addFavorite(DetailsActivitySeries.this, item);
                Toast.makeText(DetailsActivitySeries.this,
                        "Dodato u omiljene", Toast.LENGTH_SHORT).show();
            }
            updateFavoriteIcon();
        });

        favoriteButton.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                favoriteButton.setBackgroundResource(R.drawable.tv_button_focused);
            } else {
                favoriteButton.setBackgroundResource(R.drawable.tv_button_background);
            }
        });
    }

    private String getItemKey() {
        return seriesTitle;
    }

    private void setupEpisodes() {
        try {
            seasonsContainer.removeAllViews();

            if (seasonsJson == null || seasonsJson.isEmpty()) {
                showNoEpisodesMessage("Nema dostupnih epizoda");
                return;
            }

            JSONArray seasonsArray = new JSONArray(seasonsJson);

            for (int s = 0; s < seasonsArray.length(); s++) {

                JSONObject seasonObj = seasonsArray.getJSONObject(s);
                int seasonNumber = seasonObj.getInt("number");
                JSONArray episodesArray = seasonObj.getJSONArray("episodes");

                if (episodesArray.length() == 0) continue;

                addSeasonTitle("Sezona " + seasonNumber);

                HorizontalScrollView scrollView = createHorizontalScrollView();
                LinearLayout episodeLayout = createEpisodesLayout();

                for (int e = 0; e < episodesArray.length(); e++) {

                    JSONObject ep = episodesArray.getJSONObject(e);

                    String epTitle = ep.getString("title");
                    String epImage = ep.getString("imageUrl");
                    String epVideoId = ep.getString("videoId");

                    if (firstVideoId == null) firstVideoId = epVideoId;

                    LinearLayout box = createEpisodeBox(seasonNumber, e + 1, epTitle, epImage, epVideoId);
                    episodeLayout.addView(box);
                }

                scrollView.addView(episodeLayout);
                seasonsContainer.addView(scrollView);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showNoEpisodesMessage("Greška pri učitavanju epizoda");
        }
    }

    private void addSeasonTitle(String t) {
        TextView seasonTitle = new TextView(this);
        seasonTitle.setText(t);
        seasonTitle.setTextColor(Color.WHITE);
        seasonTitle.setTextSize(16);
        seasonTitle.setTypeface(null, Typeface.BOLD);
        seasonTitle.setPadding(16, 16, 16, 8);
        seasonsContainer.addView(seasonTitle);
    }

    private HorizontalScrollView createHorizontalScrollView() {
        HorizontalScrollView sv = new HorizontalScrollView(this);
        sv.setPadding(8, 0, 8, 16);
        return sv;
    }

    private LinearLayout createEpisodesLayout() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        return ll;
    }

    private void showNoEpisodesMessage(String msg) {
        TextView t = new TextView(this);
        t.setText(msg);
        t.setTextColor(Color.WHITE);
        t.setPadding(20, 20, 20, 20);
        seasonsContainer.addView(t);
    }

    private LinearLayout createEpisodeBox(int seasonNum, int epNum, String epTitle, String epImage, String epVideoId) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(12, 12, 12, 12);
        box.setFocusable(true);
        box.setBackgroundResource(R.drawable.tv_button_background);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(6, 6, 6, 6);
        box.setLayoutParams(lp);

        ImageView epImg = new ImageView(this);
        epImg.setLayoutParams(new LinearLayout.LayoutParams(180, 101));
        epImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (epImage != null && !epImage.isEmpty()) {
            Glide.with(this).load(epImage).placeholder(R.drawable.placeholder).into(epImg);
        } else epImg.setImageResource(R.drawable.placeholder);

        TextView label = new TextView(this);
        label.setText("S" + seasonNum + "E" + epNum + "\n" + epTitle);
        label.setTextColor(Color.WHITE);
        label.setTextSize(12);

        box.addView(epImg);
        box.addView(label);

        final String videoId = epVideoId;
        final String episodeTitle = label.getText().toString();

        box.setOnClickListener(v -> playEpisode(videoId, episodeTitle));

        box.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                box.setBackgroundResource(R.drawable.tv_button_focused);
                box.setScaleX(1.05f);
                box.setScaleY(1.05f);
            } else {
                box.setBackgroundResource(R.drawable.tv_button_background);
                box.setScaleX(1.0f);
                box.setScaleY(1.0f);
            }
        });

        return box;
    }

    private void playEpisode(String videoId, String episodeTitle) {
        if (videoId != null && !videoId.isEmpty()) {

            Intent intent = new Intent(DetailsActivitySeries.this, PlayerActivity.class);
            intent.putExtra("videoId", videoId);
            intent.putExtra("videoTitle", episodeTitle);
            startActivity(intent);

        } else {
            Toast.makeText(this, "Epizoda nije dostupna", Toast.LENGTH_SHORT).show();
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

        if (seasonsContainer.getChildCount() > 1) {
            View v = seasonsContainer.getChildAt(1);
            if (v instanceof HorizontalScrollView) {
                LinearLayout ll = (LinearLayout) ((HorizontalScrollView) v).getChildAt(0);
                if (ll.getChildCount() > 0) ll.getChildAt(0).requestFocus();
            }
        } else {
            favoriteButton.requestFocus();
        }
    }
}
