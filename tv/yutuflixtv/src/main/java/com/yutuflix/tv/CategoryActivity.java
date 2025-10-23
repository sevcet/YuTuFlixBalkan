package com.yutuflix.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends Activity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<Movie> movieList = new ArrayList<>();
    private MovieAdapter movieAdapter;

    // BUTTON VARIJABLE
    private Button btnHome, btnSearch, btnDomaciFilmovi, btnDomaceSerije, btnAkcija;
    private Button btnKomedija, btnHoror, btnSciFi, btnRomansa;

    private String currentCategoryName;
    private String currentXmlUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_videos);

        initViews();
        setupButtonNavigation();
        setupRecyclerView();

        currentCategoryName = getIntent().getStringExtra("categoryName");
        currentXmlUrl = getIntent().getStringExtra("xmlUrl");

        if (currentXmlUrl != null) {
            new LoadXmlTask().execute(currentXmlUrl);
        } else {
            Toast.makeText(this, "Error: No XML URL", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // INIT BUTTONS
        btnHome = findViewById(R.id.btnHome);
        btnSearch = findViewById(R.id.btnSearch);
        btnDomaciFilmovi = findViewById(R.id.btnDomaciFilmovi);
        btnDomaceSerije = findViewById(R.id.btnDomaceSerije);
        btnAkcija = findViewById(R.id.btnAkcija);
        btnKomedija = findViewById(R.id.btnKomedija);
        btnHoror = findViewById(R.id.btnHoror);
        btnSciFi = findViewById(R.id.btnSciFi);
        btnRomansa = findViewById(R.id.btnRomansa);
    }

    private void setupRecyclerView() {
        // GridLayoutManager sa 5 kolona za TV
        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(layoutManager);

        movieAdapter = new MovieAdapter(this, movieList, new MovieAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                if ("serija".equalsIgnoreCase(movie.getType()) || "series".equalsIgnoreCase(movie.getType())) {
                    openSeriesDetails(movie);
                } else {
                    openFilmDetails(movie);
                }
            }
        });
        recyclerView.setAdapter(movieAdapter);
    }

    private void setupButtonNavigation() {
        // HOME BUTTON
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // SEARCH BUTTON
        btnSearch.setOnClickListener(v -> {
            String testVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
            Intent intent = new Intent(CategoryActivity.this, PlayerActivity.class);
            intent.putExtra("videoUrl", testVideoUrl);
            intent.putExtra("videoTitle", "Search Test Video");
            startActivity(intent);
        });

        // CATEGORY BUTTONS
        btnDomaciFilmovi.setOnClickListener(v -> openCategory("Domaci Filmovi", "https://sevcet.github.io/exyuflix/domaci_filmovi.xml"));
        btnDomaceSerije.setOnClickListener(v -> openCategory("Domace Serije", "https://sevcet.github.io/exyuflix/domace_serije.xml"));
        btnAkcija.setOnClickListener(v -> openCategory("Akcija", "https://sevcet.github.io/exyuflix/akcija.xml"));
        btnKomedija.setOnClickListener(v -> openCategory("Komedija", "https://sevcet.github.io/exyuflix/komedija.xml"));
        btnHoror.setOnClickListener(v -> openCategory("Horor", "https://sevcet.github.io/exyuflix/horor.xml"));
        btnSciFi.setOnClickListener(v -> openCategory("Sci-Fi", "https://sevcet.github.io/exyuflix/sci_fi.xml"));
        btnRomansa.setOnClickListener(v -> openCategory("Romansa", "https://sevcet.github.io/exyuflix/romansa.xml"));
    }

    private void openCategory(String categoryName, String xmlUrl) {
        if (categoryName.equals(currentCategoryName)) {
            new LoadXmlTask().execute(xmlUrl);
        } else {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", categoryName);
            intent.putExtra("xmlUrl", xmlUrl);
            startActivity(intent);
            finish();
        }
    }

    private void openFilmDetails(Movie movie) {
        try {
            Intent intent = new Intent(CategoryActivity.this, DetailsActivityFilm.class);
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("year", movie.getYear());
            intent.putExtra("genre", movie.getGenre());
            intent.putExtra("description", movie.getDescription());
            intent.putExtra("imageUrl", movie.getImageUrl());
            intent.putExtra("videoId", movie.getVideoId());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Greška pri otvaranju filma", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openSeriesDetails(Movie movie) {
        try {
            Intent intent = new Intent(CategoryActivity.this, DetailsActivitySeries.class);
            // PROSLEĐUJEMO XML URL KATEGORIJE I NASLOV SERIJE
            intent.putExtra("xmlUrl", currentXmlUrl);
            intent.putExtra("seriesTitle", movie.getTitle());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Greška pri otvaranju serije", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private class LoadXmlTask extends AsyncTask<String, Void, List<Movie>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected List<Movie> doInBackground(String... urls) {
            List<Movie> movies = new ArrayList<>();

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(inputStream, "UTF-8");

                int eventType = parser.getEventType();
                String currentTag = null;

                String title = "", year = "", genre = "", type = "film", description = "", imageUrl = "", videoId = "";
                String seasonsJson = ""; // Za serije

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();
                        if ("movie".equals(currentTag)) {
                            // Resetuj podatke
                            title = ""; year = ""; genre = ""; type = "film";
                            description = ""; imageUrl = ""; videoId = "";
                            seasonsJson = "";
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (currentTag != null) {
                            String text = parser.getText();
                            switch (currentTag) {
                                case "title": title = text; break;
                                case "year": year = text; break;
                                case "genre": genre = text; break;
                                case "type": type = text; break;
                                case "description": description = text; break;
                                case "imageUrl": imageUrl = text; break;
                                case "videoId": videoId = text; break;
                                // Parsiranje seasonsJson ako postoji
                                case "seasonsJson": seasonsJson = text; break;
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if ("movie".equals(parser.getName())) {
                            // Kraj filma - dodaj u listu
                            if (!title.isEmpty()) {
                                Movie movie = new Movie(title, year, genre, type, description, imageUrl, videoId, null, seasonsJson);
                                movies.add(movie);
                            }
                        }
                        currentTag = null;
                    }
                    eventType = parser.next();
                }

                inputStream.close();
                return movies;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (result != null && !result.isEmpty()) {
                movieList.clear();
                movieList.addAll(result);
                movieAdapter.notifyDataSetChanged();

                Toast.makeText(CategoryActivity.this, "Učitano " + movieList.size() + " video sadržaja", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CategoryActivity.this, "Nema video sadržaja u ovoj kategoriji", Toast.LENGTH_LONG).show();
            }
        }
    }
}