package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class DetailsActivitySeries extends Activity {

    private ImageView poster;
    private ImageButton favoriteButton;
    private TextView title, year, genre, desc;
    private GridLayout seasonsContainer;

    private String xmlUrl;
    private String seriesTitle;
    private SeriesData currentSeriesData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_series_tv);

        initViews();
        loadIntentData();

        // Učitaj podatke iz XML-a
        if (xmlUrl != null) {
            new LoadSeriesDataTask().execute(xmlUrl);
        } else {
            Toast.makeText(this, "Greška: Nedostaje XML URL", Toast.LENGTH_SHORT).show();
            finish();
        }
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

    private void loadIntentData() {
        Intent intent = getIntent();
        xmlUrl = intent.getStringExtra("xmlUrl");
        seriesTitle = intent.getStringExtra("seriesTitle");

        // Postavi naslov ako je prosleđen
        if (seriesTitle != null) {
            title.setText(seriesTitle);
        }
    }

    private class LoadSeriesDataTask extends AsyncTask<String, Void, SeriesData> {
        @Override
        protected SeriesData doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                InputStream inputStream = url.openStream();

                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();

                SeriesXMLHandler handler = new SeriesXMLHandler(seriesTitle);
                parser.parse(new InputSource(inputStream), handler);

                return handler.getSeriesData();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(SeriesData seriesData) {
            if (seriesData != null) {
                currentSeriesData = seriesData;
                updateUIWithSeriesData(seriesData);
                setupFavoriteButton();
            } else {
                Toast.makeText(DetailsActivitySeries.this,
                        "Greška pri učitavanju podataka serije", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void updateUIWithSeriesData(SeriesData seriesData) {
        title.setText(seriesData.getTitle());
        year.setText("Godina: " + seriesData.getYear());
        genre.setText("Žanr: " + seriesData.getGenre());
        desc.setText(seriesData.getDescription());

        // Učitaj sliku
        if (seriesData.getImageUrl() != null && !seriesData.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(seriesData.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(poster);
        }

        // Postavi sezone i epizode
        setupEpisodes(seriesData.getSeasonsJson());
    }

    private void setupEpisodes(String seasonsJson) {
        try {
            // Očisti prethodne epizode
            seasonsContainer.removeAllViews();

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

                        LinearLayout epBox = createEpisodeBox(seasonNumber, e + 1, epTitle, epImage, epVideoId);
                        seasonsContainer.addView(epBox);
                    }
                }
            } else {
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

        epBox.setPadding(16, 16, 16, 16);
        epBox.setFocusable(true);
        epBox.setFocusableInTouchMode(true);
        epBox.setBackgroundResource(R.drawable.tv_button_background);

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 0;
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        layoutParams.setMargins(8, 8, 8, 8);
        epBox.setLayoutParams(layoutParams);

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

        TextView epLabel = new TextView(this);
        epLabel.setText("S" + seasonNumber + "E" + episodeNumber + " - " + epTitle);
        epLabel.setTextSize(16);
        epLabel.setTextColor(Color.WHITE);
        epLabel.setMaxLines(2);
        epLabel.setPadding(0, 8, 0, 0);

        epBox.addView(epImg);
        epBox.addView(epLabel);

        final String videoId = epVideoId;
        final String fullEpisodeTitle = "S" + seasonNumber + "E" + episodeNumber + " - " + epTitle;

        epBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playEpisode(videoId, fullEpisodeTitle);
            }
        });

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

    private void setupFavoriteButton() {
        updateFavoriteIcon();

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentTitle = title.getText().toString();
                if (FavoritesManager.isFavorite(DetailsActivitySeries.this, currentTitle)) {
                    FavoritesManager.removeFavorite(DetailsActivitySeries.this, currentTitle);
                    Toast.makeText(DetailsActivitySeries.this, "Uklonjeno iz omiljenih", Toast.LENGTH_SHORT).show();
                } else {
                    // Kreiraj SeriesItem sa trenutnim podacima
                    SeriesItem item = new SeriesItem(
                            currentTitle,
                            currentSeriesData != null ? currentSeriesData.getYear() : "",
                            currentSeriesData != null ? currentSeriesData.getGenre() : "",
                            currentSeriesData != null ? currentSeriesData.getDescription() : "",
                            currentSeriesData != null ? currentSeriesData.getImageUrl() : "",
                            currentSeriesData != null ? currentSeriesData.getSeasonsJson() : ""
                    );
                    FavoritesManager.addFavorite(DetailsActivitySeries.this, item);
                    Toast.makeText(DetailsActivitySeries.this, "Dodato u omiljene", Toast.LENGTH_SHORT).show();
                }
                updateFavoriteIcon();
            }
        });

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

    private void updateFavoriteIcon() {
        String currentTitle = title.getText().toString();
        if (currentTitle != null && FavoritesManager.isFavorite(this, currentTitle)) {
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

        if (seasonsContainer.getChildCount() > 0) {
            seasonsContainer.getChildAt(0).requestFocus();
        } else {
            favoriteButton.requestFocus();
        }
    }
}

// POMOĆNE KLASE

class SeriesData {
    private String title;
    private String year;
    private String genre;
    private String description;
    private String imageUrl;
    private String seasonsJson;

    public SeriesData(String title, String year, String genre, String description,
                      String imageUrl, String seasonsJson) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.description = description;
        this.imageUrl = imageUrl;
        this.seasonsJson = seasonsJson;
    }

    // Getters
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getSeasonsJson() { return seasonsJson; }
}

class SeriesXMLHandler extends DefaultHandler {
    private SeriesData seriesData;
    private String currentValue;
    private String targetSeriesTitle;
    private boolean foundSeries = false;
    private StringBuilder seasonsJsonBuilder;
    private boolean inSeasonsJson = false;
    private String currentElement;

    public SeriesXMLHandler(String targetSeriesTitle) {
        this.targetSeriesTitle = targetSeriesTitle;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        currentElement = qName;
        if (qName.equalsIgnoreCase("item")) {
            // Resetuj za novi item
            seriesData = new SeriesData("", "", "", "", "", "");
        } else if (qName.equalsIgnoreCase("seasonsjson")) {
            inSeasonsJson = true;
            seasonsJsonBuilder = new StringBuilder();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        currentValue = new String(ch, start, length);
        if (inSeasonsJson && seasonsJsonBuilder != null) {
            seasonsJsonBuilder.append(currentValue);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (foundSeries) return;

        if (seriesData != null) {
            if (qName.equalsIgnoreCase("title") && !inSeasonsJson) {
                seriesData = new SeriesData(currentValue, "", "", "", "", "");
            } else if (qName.equalsIgnoreCase("year") && !inSeasonsJson) {
                seriesData = new SeriesData(seriesData.getTitle(), currentValue, "", "", "", "");
            } else if (qName.equalsIgnoreCase("genre") && !inSeasonsJson) {
                seriesData = new SeriesData(seriesData.getTitle(), seriesData.getYear(), currentValue, "", "", "");
            } else if (qName.equalsIgnoreCase("description") && !inSeasonsJson) {
                seriesData = new SeriesData(seriesData.getTitle(), seriesData.getYear(),
                        seriesData.getGenre(), currentValue, "", "");
            } else if (qName.equalsIgnoreCase("imageurl") && !inSeasonsJson) {
                seriesData = new SeriesData(seriesData.getTitle(), seriesData.getYear(),
                        seriesData.getGenre(), seriesData.getDescription(), currentValue, "");
            } else if (qName.equalsIgnoreCase("seasonsjson")) {
                inSeasonsJson = false;
                if (seasonsJsonBuilder != null) {
                    SeriesData finalData = new SeriesData(seriesData.getTitle(), seriesData.getYear(),
                            seriesData.getGenre(), seriesData.getDescription(),
                            seriesData.getImageUrl(), seasonsJsonBuilder.toString());

                    // Proveri da li je ovo tražena serija
                    if (targetSeriesTitle != null &&
                            targetSeriesTitle.equalsIgnoreCase(finalData.getTitle())) {
                        seriesData = finalData;
                        foundSeries = true;
                    }
                }
            }
        }
    }

    public SeriesData getSeriesData() {
        return foundSeries ? seriesData : null;
    }
}