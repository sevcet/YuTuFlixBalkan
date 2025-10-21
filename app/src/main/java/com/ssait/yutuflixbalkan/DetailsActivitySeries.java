package com.ssait.yutuflixbalkan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetailsActivitySeries extends AppCompatActivity {

    private ImageView poster;
    private ImageButton favoriteButton;
    private TextView title, year, genre, desc;
    private GridLayout seasonsContainer;

    private GestureDetector gestureDetector;
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
        setContentView(R.layout.activity_details_series);

        poster = findViewById(R.id.detailSeriesImage);
        title = findViewById(R.id.detailSeriesTitle);
        year = findViewById(R.id.detailSeriesYear);
        genre = findViewById(R.id.detailSeriesGenre);
        desc = findViewById(R.id.detailSeriesDesc);
        seasonsContainer = findViewById(R.id.seasonsContainer);
        favoriteButton = findViewById(R.id.favoriteIconSeries);

        Intent i = getIntent();
        seriesTitle = i.getStringExtra("title");
        seriesYear = i.getStringExtra("year");
        seriesGenre = i.getStringExtra("genre");
        seriesDesc = i.getStringExtra("description");
        seriesImage = i.getStringExtra("imageUrl");
        seasonsJson = i.getStringExtra("seasonsJson");

        // fallback ako nije prosleđen title (da ne padne na null)
        if (seriesTitle == null) {
            seriesTitle = "";
        }

        title.setText(seriesTitle);
        year.setText("Godina: " + (seriesYear != null ? seriesYear : ""));
        genre.setText("Žanr: " + (seriesGenre != null ? seriesGenre : ""));
        desc.setText(seriesDesc != null ? seriesDesc : "");

        Glide.with(this).load(seriesImage).placeholder(R.drawable.placeholder).into(poster);

        // inicijalno stanje srca
        updateFavoriteIcon();

        // klik na srce
        favoriteButton.setOnClickListener(v -> {
            if (FavoritesManager.isFavorite(this, seriesTitle)) {
                FavoritesManager.removeFavorite(this, seriesTitle);
            } else {
                RecyclerItem item = new RecyclerItem(
                        seriesTitle,
                        seriesYear,
                        seriesGenre,
                        seriesDesc,
                        seriesImage,
                        null,
                        true,
                        seasonsJson
                );
                FavoritesManager.addFavorite(this, item);
            }
            updateFavoriteIcon();
        });

        // dodavanje epizoda u grid
        try {
            if (seasonsJson != null) {
                JSONArray sArray = new JSONArray(seasonsJson);
                for (int s = 0; s < sArray.length(); s++) {
                    JSONObject sObj = sArray.getJSONObject(s);
                    int number = sObj.getInt("number");

                    JSONArray eArray = sObj.getJSONArray("episodes");
                    for (int e = 0; e < eArray.length(); e++) {
                        JSONObject ep = eArray.getJSONObject(e);
                        String epTitle = ep.getString("title");
                        String epImage = ep.getString("imageUrl");
                        String epVideoId = ep.getString("videoId");

                        if (firstVideoId == null) {
                            firstVideoId = epVideoId;
                        }

                        LinearLayout epBox = new LinearLayout(this);
                        epBox.setOrientation(LinearLayout.VERTICAL);
                        epBox.setPadding(8, 8, 8, 8);

                        ImageView epImg = new ImageView(this);
                        epImg.setLayoutParams(new LinearLayout.LayoutParams(500, 280));
                        epImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Glide.with(this).load(epImage).placeholder(R.drawable.placeholder).into(epImg);

                        TextView epLabel = new TextView(this);
                        epLabel.setText("S" + number + "E" + (e + 1) + " - " + epTitle);
                        epLabel.setTextSize(14);
                        epLabel.setTextColor(Color.WHITE);

                        epBox.addView(epImg);
                        epBox.addView(epLabel);

                        final String vid = epVideoId;
                        epImg.setOnClickListener(v -> {
                            Intent intent = new Intent(DetailsActivitySeries.this, PlayerActivity.class);
                            intent.putExtra("videoId", vid);
                            startActivity(intent);
                        });

                        seasonsContainer.addView(epBox);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // gesture swipe
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            finish(); // swipe desno - nazad
                        } else {
                            if (firstVideoId != null) {
                                Intent intent = new Intent(DetailsActivitySeries.this, PlayerActivity.class);
                                intent.putExtra("videoId", firstVideoId);
                                startActivity(intent);
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
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
    }
}
