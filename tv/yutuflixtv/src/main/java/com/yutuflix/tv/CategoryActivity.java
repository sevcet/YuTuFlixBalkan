package com.yutuflix.tv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
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
    private List<Movie> allMovies = new ArrayList<>();
    private MovieAdapter movieAdapter;

    private Button btnHome, btnSearch, btnDomaciFilmovi, btnDomaceSerije, btnAkcija;
    private Button btnKomedija, btnHoror, btnSciFi, btnRomansa, btnMisterija, btnDokumentarni;
    private Button btnAnimirani, btnFavorites, btnAbout, btnPrivacy;

    private EditText searchBar;
    private LinearLayout searchContainer;
    private boolean isSearchActive = false;
    private boolean isShowingSearchResults = false;

    private String currentCategoryName;
    private String currentXmlUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_videos);

        initViews();
        setupButtonNavigation();
        setupSearchBar();
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

        searchBar = findViewById(R.id.searchBar);
        searchContainer = findViewById(R.id.searchContainer);

        btnHome = findViewById(R.id.btnHome);
        btnSearch = findViewById(R.id.btnSearch);
        btnDomaciFilmovi = findViewById(R.id.btnDomaciFilmovi);
        btnDomaceSerije = findViewById(R.id.btnDomaceSerije);
        btnAkcija = findViewById(R.id.btnAkcija);
        btnKomedija = findViewById(R.id.btnKomedija);
        btnHoror = findViewById(R.id.btnHoror);
        btnSciFi = findViewById(R.id.btnSciFi);
        btnRomansa = findViewById(R.id.btnRomansa);
        btnMisterija = findViewById(R.id.btnMisterija);
        btnDokumentarni = findViewById(R.id.btnDokumentarni);
        btnAnimirani = findViewById(R.id.btnAnimirani);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnAbout = findViewById(R.id.btnAbout);
        btnPrivacy = findViewById(R.id.btnPrivacy);
    }

    private void setupSearchBar() {
        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        String query = searchBar.getText().toString().trim();
                        if (!query.isEmpty()) {
                            performSearch(query);
                        } else {
                            Toast.makeText(CategoryActivity.this, "Unesite termin za pretragu", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                        hideSearch();
                        return true;
                    }
                }
                return false;
            }
        });

        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchBar.setBackgroundResource(R.drawable.tv_button_focused);
                    showKeyboard();
                } else {
                    searchBar.setBackgroundResource(R.drawable.tv_edittext_background);
                }
            }
        });
    }

    private void setupRecyclerView() {
        int spanCount = calculateNumberOfColumns();
        Log.d("GRID_DEBUG", "Calculated span count: " + spanCount);

        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
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

    private int calculateNumberOfColumns() {
        // Fiksno 5 kolona za sve TV uredjaje
        return 5;
    }

    private void setupButtonNavigation() {
        btnHome.setOnClickListener(v -> {
            hideSearch();
            Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnSearch.setOnClickListener(v -> {
            if (isSearchActive) {
                hideSearch();
            } else {
                showSearch();
            }
        });

        btnFavorites.setOnClickListener(v -> {
            hideSearch();
            Intent intent = new Intent(CategoryActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        btnAbout.setOnClickListener(v -> {
            hideSearch();
            showAboutDialog();
        });

        btnPrivacy.setOnClickListener(v -> {
            hideSearch();
            Intent intent = new Intent(CategoryActivity.this, PrivacyActivity.class);
            startActivity(intent);
        });

        btnDomaciFilmovi.setOnClickListener(v -> {
            hideSearch();
            openCategory("Domaci Filmovi", "https://sevcet.github.io/exyuflix/domaci_filmovi.xml");
        });

        btnDomaceSerije.setOnClickListener(v -> {
            hideSearch();
            openCategory("Domace Serije", "https://sevcet.github.io/exyuflix/domace_serije.xml");
        });

        btnAkcija.setOnClickListener(v -> {
            hideSearch();
            openCategory("Akcija", "https://sevcet.github.io/exyuflix/akcija.xml");
        });

        btnKomedija.setOnClickListener(v -> {
            hideSearch();
            openCategory("Komedija", "https://sevcet.github.io/exyuflix/komedija.xml");
        });

        btnHoror.setOnClickListener(v -> {
            hideSearch();
            openCategory("Horor", "https://sevcet.github.io/exyuflix/horor.xml");
        });

        btnSciFi.setOnClickListener(v -> {
            hideSearch();
            openCategory("Sci-Fi", "https://sevcet.github.io/exyuflix/sci_fi.xml");
        });

        btnRomansa.setOnClickListener(v -> {
            hideSearch();
            openCategory("Romansa", "https://sevcet.github.io/exyuflix/romansa.xml");
        });

        btnMisterija.setOnClickListener(v -> {
            hideSearch();
            openCategory("Misterija", "https://sevcet.github.io/exyuflix/misterija.xml");
        });

        btnDokumentarni.setOnClickListener(v -> {
            hideSearch();
            openCategory("Dokumentarni", "https://sevcet.github.io/exyuflix/dokumentarni.xml");
        });

        btnAnimirani.setOnClickListener(v -> {
            hideSearch();
            openCategory("Animirani", "https://sevcet.github.io/exyuflix/animirani.xml");
        });

        setupButtonFocusListeners();
    }

    private void showAboutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("O aplikaciji");
        builder.setMessage("Tehnički Preglednik Sadržaja\n\n" +
                "Ova aplikacija funkcioniše kao video agregator koji koristi YouTube embed API za prikaz sadržaja. Svi metapodaci (metadata) se dinamički učitavaju sa eksternih XML izvora.\n\n" +
                "🔄 Tehnička Arhitektura:\n" +
                "• Metadata: GitHub Pages XML feedovi\n" +
                "• Video streaming: YouTube official embed player\n" +
                "• Lokalno skladištenje: Omiljeni sadržaji\n\n" +
                "📺 Način Rada:\n" +
                "Aplikacija ne hostira nikakav video sadržaj. Svi video zapisi se reprodukuju direktno sa YouTube servera putem službenog embed sistema, uz poštovanje autorskih prava i uslova korišćenja.\n\n" +
                "⚖️ Pravni Disclaimer:\n" +
                "Ova aplikacija je tehnološki preglednik i ne poseduje niti distribuira video sadržaje. Svi autorski materijali pripadaju njihovim vlasnicima. Korišćenje aplikacije podrazumeva saglasnost sa YouTube uslovima korišćenja.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void setupButtonFocusListeners() {
        View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.tv_button_focused);
                    v.setScaleX(1.05f);
                    v.setScaleY(1.05f);
                } else {
                    v.setBackgroundResource(R.drawable.tv_button_background);
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }
            }
        };

        btnHome.setOnFocusChangeListener(focusListener);
        btnSearch.setOnFocusChangeListener(focusListener);
        btnDomaciFilmovi.setOnFocusChangeListener(focusListener);
        btnDomaceSerije.setOnFocusChangeListener(focusListener);
        btnAkcija.setOnFocusChangeListener(focusListener);
        btnKomedija.setOnFocusChangeListener(focusListener);
        btnHoror.setOnFocusChangeListener(focusListener);
        btnSciFi.setOnFocusChangeListener(focusListener);
        btnRomansa.setOnFocusChangeListener(focusListener);
        btnMisterija.setOnFocusChangeListener(focusListener);
        btnDokumentarni.setOnFocusChangeListener(focusListener);
        btnAnimirani.setOnFocusChangeListener(focusListener);
        btnFavorites.setOnFocusChangeListener(focusListener);
        btnAbout.setOnFocusChangeListener(focusListener);
        btnPrivacy.setOnFocusChangeListener(focusListener);
    }

    private void showSearch() {
        isSearchActive = true;
        searchContainer.setVisibility(View.VISIBLE);
        searchBar.postDelayed(() -> {
            searchBar.requestFocus();
            searchBar.selectAll();
        }, 100);
    }

    private void hideSearch() {
        isSearchActive = false;
        searchContainer.setVisibility(View.GONE);
        searchBar.setText("");
        hideKeyboard();

        btnHome.postDelayed(() -> {
            btnHome.requestFocus();
        }, 100);

        if (isShowingSearchResults) {
            isShowingSearchResults = false;
            if (currentXmlUrl != null) {
                new LoadXmlTask().execute(currentXmlUrl);
            }
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }

    private void performSearch(String query) {
        List<Movie> searchResults = new ArrayList<>();
        for (Movie movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    movie.getGenre().toLowerCase().contains(query.toLowerCase()) ||
                    (movie.getYear() != null && movie.getYear().toLowerCase().contains(query.toLowerCase()))) {
                searchResults.add(movie);
            }
        }

        if (searchResults.isEmpty()) {
            Toast.makeText(this, "Nema rezultata za: " + query, Toast.LENGTH_SHORT).show();
        } else {
            movieList.clear();
            movieList.addAll(searchResults);
            movieAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Pronađeno " + searchResults.size() + " rezultata", Toast.LENGTH_SHORT).show();
            isShowingSearchResults = true;
            hideSearchUIOnly();
        }
    }

    private void hideSearchUIOnly() {
        isSearchActive = false;
        searchContainer.setVisibility(View.GONE);
        searchBar.setText("");
        hideKeyboard();

        btnHome.postDelayed(() -> {
            btnHome.requestFocus();
        }, 100);
    }

    private void openCategory(String categoryName, String xmlUrl) {
        if (categoryName.equals(currentCategoryName)) {
            hideSearch();
            new LoadXmlTask().execute(xmlUrl);
        } else {
            hideSearch();
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
            Log.d("CATEGORY_DEBUG", "=== OPENING SERIES ===");
            Log.d("CATEGORY_DEBUG", "Title: " + movie.getTitle());
            Log.d("CATEGORY_DEBUG", "Type: " + movie.getType());
            Log.d("CATEGORY_DEBUG", "SeasonsJson: " + (movie.getSeasonsJson() != null ? "NOT NULL" : "NULL"));
            if (movie.getSeasonsJson() != null) {
                Log.d("CATEGORY_DEBUG", "SeasonsJson length: " + movie.getSeasonsJson().length());
                if (movie.getSeasonsJson().length() > 0) {
                    Log.d("CATEGORY_DEBUG", "First 100 chars: " + movie.getSeasonsJson().substring(0, Math.min(movie.getSeasonsJson().length(), 100)));
                } else {
                    Log.d("CATEGORY_DEBUG", "SeasonsJson is EMPTY STRING!");
                }
            }

            Intent intent = new Intent(CategoryActivity.this, DetailsActivitySeries.class);
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("year", movie.getYear());
            intent.putExtra("genre", movie.getGenre());
            intent.putExtra("description", movie.getDescription());
            intent.putExtra("imageUrl", movie.getImageUrl());
            intent.putExtra("seasonsJson", movie.getSeasonsJson());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Greška pri otvaranju serije", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearchActive) {
                hideSearch();
                return true;
            } else if (isShowingSearchResults) {
                isShowingSearchResults = false;
                if (currentXmlUrl != null) {
                    new LoadXmlTask().execute(currentXmlUrl);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class LoadXmlTask extends AsyncTask<String, Void, List<Movie>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected List<Movie> doInBackground(String... urls) {
            List<Movie> moviesList = new ArrayList<>();

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
                String seasonsJson = "";

                // Varijable za parsiranje seasons sekcije
                boolean inSeasonsSection = false;
                boolean inSeason = false;
                boolean inEpisode = false;

                List<JSONObject> seasonsList = new ArrayList<>();
                JSONObject currentSeason = null;
                List<JSONObject> currentEpisodes = null;
                int currentSeasonNumber = 0;
                String episodeTitle = "", episodeImageUrl = "", episodeVideoId = "";

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();

                        if ("movie".equals(currentTag)) {
                            // Resetuj podatke za novi film
                            title = ""; year = ""; genre = ""; type = "film";
                            description = ""; imageUrl = ""; videoId = "";
                            seasonsJson = "";
                            inSeasonsSection = false;
                            inSeason = false;
                            inEpisode = false;
                            seasonsList.clear();
                            currentSeason = null;
                            currentEpisodes = null;
                            currentSeasonNumber = 0;
                            episodeTitle = ""; episodeImageUrl = ""; episodeVideoId = "";

                        } else if ("seasons".equals(currentTag)) {
                            inSeasonsSection = true;
                            seasonsList.clear();

                        } else if ("season".equals(currentTag)) {
                            if (inSeasonsSection) {
                                inSeason = true;
                                currentSeasonNumber = Integer.parseInt(parser.getAttributeValue(null, "number"));
                                currentEpisodes = new ArrayList<>();
                                currentSeason = new JSONObject();
                            }

                        } else if ("episode".equals(currentTag)) {
                            if (inSeason) {
                                inEpisode = true;
                                episodeTitle = "";
                                episodeImageUrl = "";
                                episodeVideoId = "";
                            }
                        }

                    } else if (eventType == XmlPullParser.TEXT) {
                        if (currentTag != null) {
                            String text = parser.getText().trim();

                            if (inEpisode) {
                                // Čitanje podataka epizode
                                switch (currentTag) {
                                    case "title":
                                        episodeTitle = text;
                                        break;
                                    case "imageUrl":
                                        episodeImageUrl = text;
                                        break;
                                    case "videoId":
                                        episodeVideoId = text;
                                        break;
                                }
                            } else if (!inSeasonsSection) {
                                // Čitanje osnovnih podataka filma (van seasons sekcije)
                                switch (currentTag) {
                                    case "title":
                                        title = text;
                                        break;
                                    case "year":
                                        year = text;
                                        break;
                                    case "genre":
                                        genre = text;
                                        break;
                                    case "type":
                                        type = text;
                                        break;
                                    case "description":
                                        description = text;
                                        break;
                                    case "imageUrl":
                                        imageUrl = text;
                                        break;
                                    case "videoId":
                                        videoId = text;
                                        break;
                                }
                            }
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {
                        String tagName = parser.getName();

                        if ("episode".equals(tagName)) {
                            if (inEpisode && currentEpisodes != null) {
                                // Završi epizodu samo ako ima validne podatke
                                if (!episodeVideoId.isEmpty()) {
                                    try {
                                        JSONObject episodeObj = new JSONObject();
                                        episodeObj.put("title", episodeTitle);
                                        episodeObj.put("imageUrl", episodeImageUrl);
                                        episodeObj.put("videoId", episodeVideoId);
                                        currentEpisodes.add(episodeObj);
                                    } catch (Exception e) {
                                        Log.e("EPISODE_ERROR", "Error creating episode JSON", e);
                                    }
                                }
                                inEpisode = false;
                            }

                        } else if ("season".equals(tagName)) {
                            if (inSeason && currentSeason != null && currentEpisodes != null && !currentEpisodes.isEmpty()) {
                                try {
                                    currentSeason.put("number", currentSeasonNumber);
                                    currentSeason.put("episodes", new JSONArray(currentEpisodes));
                                    seasonsList.add(currentSeason);
                                } catch (Exception e) {
                                    Log.e("SEASON_ERROR", "Error creating season JSON", e);
                                }
                            }
                            inSeason = false;
                            currentSeason = null;
                            currentEpisodes = null;

                        } else if ("seasons".equals(tagName)) {
                            // Završi seasons sekciju i kreiraj seasonsJson
                            if (!seasonsList.isEmpty()) {
                                try {
                                    JSONArray seasonsArray = new JSONArray(seasonsList);
                                    seasonsJson = seasonsArray.toString();
                                    Log.d("SEASONS_JSON", "Generated seasonsJson for " + title + ": " + seasonsJson.length() + " chars");
                                } catch (Exception e) {
                                    Log.e("SEASONS_ERROR", "Error creating seasons JSON", e);
                                }
                            }
                            inSeasonsSection = false;
                            seasonsList.clear();

                        } else if ("movie".equals(tagName)) {
                            // Kraj filma - dodaj u listu
                            if (!title.isEmpty()) {
                                // Ako ima seasons podatke, postavi kao seriju
                                if (!seasonsJson.isEmpty() || !seasonsList.isEmpty()) {
                                    type = "serija";

                                    // Ako nije postavljen seasonsJson iz seasons sekcije, kreiraj ga
                                    if (seasonsJson.isEmpty() && !seasonsList.isEmpty()) {
                                        try {
                                            JSONArray seasonsArray = new JSONArray(seasonsList);
                                            seasonsJson = seasonsArray.toString();
                                        } catch (Exception e) {
                                            Log.e("MOVIE_ERROR", "Error creating final seasons JSON", e);
                                        }
                                    }
                                }

                                Movie movie = new Movie(title, year, genre, type, description, imageUrl, videoId, null, seasonsJson);
                                moviesList.add(movie);

                                Log.d("MOVIE_LOADED", "Loaded: " + title + " | Type: " + type +
                                        " | Seasons: " + (!seasonsJson.isEmpty() ? seasonsJson.length() + " chars" : "none"));
                            }

                            // Resetuj seasons podatke za sledeći film
                            seasonsJson = "";
                            seasonsList.clear();
                        }

                        currentTag = null;
                    }
                    eventType = parser.next();
                }

                inputStream.close();
                return moviesList;

            } catch (Exception e) {
                Log.e("CATEGORY_LOAD", "Error loading from URL: " + urls[0], e);
                return moviesList;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (result != null && !result.isEmpty()) {
                movieList.clear();
                movieList.addAll(result);

                allMovies.clear();
                allMovies.addAll(result);

                movieAdapter.notifyDataSetChanged();

                // DEBUG: Proveri sve serije
                int seriesCount = 0;
                for (Movie movie : result) {
                    if ("serija".equalsIgnoreCase(movie.getType()) || "series".equalsIgnoreCase(movie.getType())) {
                        seriesCount++;
                        Log.d("POST_DEBUG", "Series: " + movie.getTitle() + " - SeasonsJson length: " +
                                (movie.getSeasonsJson() != null ? movie.getSeasonsJson().length() : "NULL"));
                    }
                }
                Log.d("POST_DEBUG", "Total series found: " + seriesCount);

                Toast.makeText(CategoryActivity.this, "Učitano " + movieList.size() + " video sadržaja", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CategoryActivity.this, "Nema video sadržaja u ovoj kategoriji", Toast.LENGTH_LONG).show();
            }
        }
    }
}