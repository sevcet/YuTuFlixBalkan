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

    private Button btnHome, btnSearch, btnLanguage, btnAction, btnComedy;
    private Button btnDrama, btnHorror, btnThriller, btnSciFi, btnRomance;
    private Button btnAnimation, btnCrime, btnTVSeries, btnClassic, btnDocumentary;
    private Button btnFavorites, btnAbout, btnPrivacy;

    private EditText searchBar;
    private LinearLayout searchContainer;
    private boolean isSearchActive = false;
    private boolean isShowingSearchResults = false;

    private String currentCategoryName;
    private String[] currentXmlUrls;
    private LanguageManager languageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_videos);

        // INICIJALIZUJ LANGUAGE MANAGER
        languageManager = new LanguageManager(this);

        initViews();
        setupButtonNavigation();
        setupSearchBar();
        setupRecyclerView();

        currentCategoryName = getIntent().getStringExtra("categoryName");
        currentXmlUrls = getIntent().getStringArrayExtra("xmlUrls");

        if (currentXmlUrls != null && currentXmlUrls.length > 0) {
            new LoadXmlTask().execute(currentXmlUrls);
        } else {
            Toast.makeText(this, "Error: No XML URLs", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        searchBar = findViewById(R.id.searchBar);
        searchContainer = findViewById(R.id.searchContainer);

        // INIT BUTTONS - GLOBALNI ŽANROVI
        btnHome = findViewById(R.id.btnHome);
        btnSearch = findViewById(R.id.btnSearch);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnAction = findViewById(R.id.btnAction);
        btnComedy = findViewById(R.id.btnComedy);
        btnDrama = findViewById(R.id.btnDrama);
        btnHorror = findViewById(R.id.btnHorror);
        btnThriller = findViewById(R.id.btnThriller);
        btnSciFi = findViewById(R.id.btnSciFi);
        btnRomance = findViewById(R.id.btnRomance);
        btnAnimation = findViewById(R.id.btnAnimation);
        btnCrime = findViewById(R.id.btnCrime);
        btnTVSeries = findViewById(R.id.btnTVSeries);
        btnClassic = findViewById(R.id.btnClassic);
        btnDocumentary = findViewById(R.id.btnDocumentary);
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
                            Toast.makeText(CategoryActivity.this, "Enter search term", Toast.LENGTH_SHORT).show();
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

        // LANGUAGE BUTTON - otvara meni za izbor jezika
        btnLanguage.setOnClickListener(v -> {
            hideSearch();
            showLanguageSelectionDialog();
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

        // CATEGORY BUTTONS - OTVARAJU CATEGORY ACTIVITY SA VIŠE XML FAJLOVA
        btnAction.setOnClickListener(v -> {
            hideSearch();
            openCategory("Action & Adventure", new String[]{
                    "https://sevcet.github.io/exyuflix/action1.xml",
                    "https://sevcet.github.io/exyuflix/action2.xml",
                    "https://sevcet.github.io/exyuflix/action3.xml",
                    "https://sevcet.github.io/exyuflix/action4.xml"
            });
        });

        btnComedy.setOnClickListener(v -> {
            hideSearch();
            openCategory("Comedy", new String[]{
                    "https://sevcet.github.io/exyuflix/comedy1.xml",
                    "https://sevcet.github.io/exyuflix/comedy2.xml",
                    "https://sevcet.github.io/exyuflix/comedy3.xml"
            });
        });

        btnDrama.setOnClickListener(v -> {
            hideSearch();
            openCategory("Drama", new String[]{
                    "https://sevcet.github.io/exyuflix/drama1.xml",
                    "https://sevcet.github.io/exyuflix/drama2.xml"
            });
        });

        btnHorror.setOnClickListener(v -> {
            hideSearch();
            openCategory("Horror", new String[]{
                    "https://sevcet.github.io/exyuflix/horror1.xml",
                    "https://sevcet.github.io/exyuflix/horror2.xml"
            });
        });

        btnThriller.setOnClickListener(v -> {
            hideSearch();
            openCategory("Thriller", new String[]{
                    "https://sevcet.github.io/exyuflix/thriller1.xml",
                    "https://sevcet.github.io/exyuflix/thriller2.xml"
            });
        });

        btnSciFi.setOnClickListener(v -> {
            hideSearch();
            openCategory("Sci-Fi & Fantasy", new String[]{
                    "https://sevcet.github.io/exyuflix/scifi1.xml",
                    "https://sevcet.github.io/exyuflix/scifi2.xml",
                    "https://sevcet.github.io/exyuflix/scifi3.xml"
            });
        });

        btnRomance.setOnClickListener(v -> {
            hideSearch();
            openCategory("Romance", new String[]{
                    "https://sevcet.github.io/exyuflix/romance1.xml",
                    "https://sevcet.github.io/exyuflix/romance2.xml"
            });
        });

        btnAnimation.setOnClickListener(v -> {
            hideSearch();
            openCategory("Animation & Family", new String[]{
                    "https://sevcet.github.io/exyuflix/animation1.xml",
                    "https://sevcet.github.io/exyuflix/animation2.xml"
            });
        });

        btnCrime.setOnClickListener(v -> {
            hideSearch();
            openCategory("Crime & Mystery", new String[]{
                    "https://sevcet.github.io/exyuflix/crime1.xml",
                    "https://sevcet.github.io/exyuflix/crime2.xml"
            });
        });

        btnTVSeries.setOnClickListener(v -> {
            hideSearch();
            openCategory("TV Series", new String[]{
                    "https://sevcet.github.io/exyuflix/series1.xml",
                    "https://sevcet.github.io/exyuflix/series2.xml",
                    "https://sevcet.github.io/exyuflix/series3.xml"
            });
        });

        btnClassic.setOnClickListener(v -> {
            hideSearch();
            openCategory("Classic Movies", new String[]{
                    "https://sevcet.github.io/exyuflix/classic1.xml",
                    "https://sevcet.github.io/exyuflix/classic2.xml"
            });
        });

        btnDocumentary.setOnClickListener(v -> {
            hideSearch();
            openCategory("Documentary", new String[]{
                    "https://sevcet.github.io/exyuflix/documentary1.xml",
                    "https://sevcet.github.io/exyuflix/documentary2.xml"
            });
        });

        setupButtonFocusListeners();
    }

    private void showLanguageSelectionDialog() {
        final List<String> selectedLanguages = new ArrayList<>(languageManager.getFavoriteLanguages());

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Favorite Languages");
        builder.setMessage("Choose languages for content filtering");

        // Pripremi podatke za dialog
        final String[] languageNames = languageManager.getSupportedLanguageNames();
        final String[] languageCodes = languageManager.getSupportedLanguageCodes();

        boolean[] checkedItems = new boolean[languageNames.length];
        for (int i = 0; i < languageCodes.length; i++) {
            checkedItems[i] = selectedLanguages.contains(languageCodes[i]);
        }

        builder.setMultiChoiceItems(languageNames, checkedItems, (dialog, which, isChecked) -> {
            String langCode = languageCodes[which];
            if (isChecked) {
                selectedLanguages.add(langCode);
            } else {
                selectedLanguages.remove(langCode);
            }
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            // Sačuvaj omiljene jezike
            languageManager.setFavoriteLanguages(selectedLanguages);

            // Ponovo učitaj filmove sa novim filterom
            if (currentXmlUrls != null) {
                new LoadXmlTask().execute(currentXmlUrls);
            }
            Toast.makeText(CategoryActivity.this, "Languages saved: " + selectedLanguages.size() + " selected", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);

        builder.setNeutralButton("Select All", (dialog, which) -> {
            // Automatski selektuj sve jezike
            selectedLanguages.clear();
            for (String code : languageCodes) {
                selectedLanguages.add(code);
            }

            // Sačuvaj i ponovo učitaj
            languageManager.setFavoriteLanguages(selectedLanguages);
            if (currentXmlUrls != null) {
                new LoadXmlTask().execute(currentXmlUrls);
            }
            Toast.makeText(CategoryActivity.this, "All languages selected", Toast.LENGTH_SHORT).show();
        });

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAboutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("About Application");
        builder.setMessage("🔍 Content Aggregator\n\n" +
                "This application functions as a video aggregator using YouTube embed API to display content. All metadata is dynamically loaded from external XML sources.\n\n" +
                "🔄 Technical Architecture:\n" +
                "• Metadata: GitHub Pages XML feeds\n" +
                "• Video streaming: YouTube official embed player\n" +
                "• Local storage: Favorite content\n\n" +
                "📺 How it Works:\n" +
                "The application does not host any video content. All videos are played directly from YouTube servers through the official embed system, respecting copyright and terms of use.\n\n" +
                "⚖️ Legal Disclaimer:\n" +
                "This application is a technological content browser and does not own or distribute video content. All copyrighted materials belong to their respective owners. Using the application implies agreement with YouTube terms of service.");
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
        btnLanguage.setOnFocusChangeListener(focusListener);
        btnAction.setOnFocusChangeListener(focusListener);
        btnComedy.setOnFocusChangeListener(focusListener);
        btnDrama.setOnFocusChangeListener(focusListener);
        btnHorror.setOnFocusChangeListener(focusListener);
        btnThriller.setOnFocusChangeListener(focusListener);
        btnSciFi.setOnFocusChangeListener(focusListener);
        btnRomance.setOnFocusChangeListener(focusListener);
        btnAnimation.setOnFocusChangeListener(focusListener);
        btnCrime.setOnFocusChangeListener(focusListener);
        btnTVSeries.setOnFocusChangeListener(focusListener);
        btnClassic.setOnFocusChangeListener(focusListener);
        btnDocumentary.setOnFocusChangeListener(focusListener);
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
            if (currentXmlUrls != null) {
                new LoadXmlTask().execute(currentXmlUrls);
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
            Toast.makeText(this, "No results for: " + query, Toast.LENGTH_SHORT).show();
        } else {
            movieList.clear();
            movieList.addAll(searchResults);
            movieAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Found " + searchResults.size() + " results", Toast.LENGTH_SHORT).show();
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

    private void openCategory(String categoryName, String[] xmlUrls) {
        if (categoryName.equals(currentCategoryName)) {
            hideSearch();
            new LoadXmlTask().execute(xmlUrls);
        } else {
            hideSearch();
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", categoryName);
            intent.putExtra("xmlUrls", xmlUrls);
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
            intent.putExtra("availableCaptions", movie.getAvailableCaptions());
            intent.putExtra("audioLanguage", movie.getAudioLanguage());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening movie", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openSeriesDetails(Movie movie) {
        try {
            Log.d("CATEGORY_DEBUG", "=== OPENING SERIES ===");
            Log.d("CATEGORY_DEBUG", "Title: " + movie.getTitle());
            Log.d("CATEGORY_DEBUG", "Type: " + movie.getType());
            Log.d("CATEGORY_DEBUG", "Audio: " + movie.getAudioLanguage());
            Log.d("CATEGORY_DEBUG", "Captions: " + movie.getAvailableCaptions());
            Log.d("CATEGORY_DEBUG", "SeasonsJson: " + (movie.getSeasonsJson() != null ? "NOT NULL" : "NULL"));

            Intent intent = new Intent(CategoryActivity.this, DetailsActivitySeries.class);
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("year", movie.getYear());
            intent.putExtra("genre", movie.getGenre());
            intent.putExtra("description", movie.getDescription());
            intent.putExtra("imageUrl", movie.getImageUrl());
            intent.putExtra("videoId", movie.getVideoId());
            intent.putExtra("seasonsJson", movie.getSeasonsJson());
            intent.putExtra("availableCaptions", movie.getAvailableCaptions());
            intent.putExtra("audioLanguage", movie.getAudioLanguage());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening series", Toast.LENGTH_SHORT).show();
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
                if (currentXmlUrls != null) {
                    new LoadXmlTask().execute(currentXmlUrls);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class LoadXmlTask extends AsyncTask<String[], Void, List<Movie>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected List<Movie> doInBackground(String[]... urlsArray) {
            List<Movie> allMoviesFromUrls = new ArrayList<>();
            String[] urls = urlsArray[0];

            // UČITAJ IZ SVIH XML FAJLOVA
            for (String url : urls) {
                try {
                    List<Movie> moviesFromUrl = loadCategoryFromUrl(url);
                    if (moviesFromUrl != null && !moviesFromUrl.isEmpty()) {
                        allMoviesFromUrls.addAll(moviesFromUrl);
                        Log.d("MULTI_XML", "Loaded " + moviesFromUrl.size() + " movies from: " + url);
                    }
                } catch (Exception e) {
                    Log.e("MULTI_XML", "Error loading from URL: " + url, e);
                }
            }

            return allMoviesFromUrls;
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (result != null && !result.isEmpty()) {
                // FILTRIRAJ FILMOVE PO JEZIKU
                List<Movie> filteredMovies = filterMoviesByLanguage(result);

                movieList.clear();
                movieList.addAll(filteredMovies);

                allMovies.clear();
                allMovies.addAll(filteredMovies);

                movieAdapter.notifyDataSetChanged();

                // DEBUG: Proveri sve serije
                int seriesCount = 0;
                for (Movie movie : filteredMovies) {
                    if ("serija".equalsIgnoreCase(movie.getType()) || "series".equalsIgnoreCase(movie.getType())) {
                        seriesCount++;
                        Log.d("POST_DEBUG", "Series: " + movie.getTitle() +
                                " - Audio: " + movie.getAudioLanguage() +
                                " - Captions: " + movie.getAvailableCaptions());
                    }
                }

                Log.d("POST_DEBUG", "Total series found: " + seriesCount);
                Log.d("LANGUAGE_FILTER", "Displaying " + filteredMovies.size() + " movies from " + result.size() + " total");

                Toast.makeText(CategoryActivity.this, "Loaded " + movieList.size() + " video contents", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CategoryActivity.this, "No video content available for selected languages", Toast.LENGTH_LONG).show();
            }
        }

        // FILTRIRANJE FILMOVA PO JEZIKU
        private List<Movie> filterMoviesByLanguage(List<Movie> movies) {
            List<String> favoriteLangs = languageManager.getFavoriteLanguages();
            List<Movie> filtered = new ArrayList<>();

            for (Movie movie : movies) {
                if (movie.hasPreferredLanguage(favoriteLangs)) {
                    filtered.add(movie);
                }
            }

            Log.d("LANGUAGE_FILTER", "Filtered " + filtered.size() + " movies from " + movies.size() + " total for languages: " + favoriteLangs);
            return filtered;
        }

        private List<Movie> loadCategoryFromUrl(String urlString) {
            List<Movie> moviesList = new ArrayList<>();

            try {
                URL url = new URL(urlString);
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
                String availableCaptions = "";
                String audioLanguage = "";

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
                            availableCaptions = "";
                            audioLanguage = "";
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
                                    case "available_captions":
                                        availableCaptions = text;
                                        break;
                                    case "audio_language":
                                        audioLanguage = text;
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

                                // Kreiraj movie objekat sa novim poljima
                                Movie movie = new Movie(title, year, genre, type, description, imageUrl, videoId, null, seasonsJson);
                                movie.setAvailableCaptions(availableCaptions);
                                movie.setAudioLanguage(audioLanguage);

                                moviesList.add(movie);

                                Log.d("MOVIE_LOADED", "Loaded: " + title +
                                        " | Type: " + type +
                                        " | Audio: " + audioLanguage +
                                        " | Captions: " + availableCaptions +
                                        " | Seasons: " + (!seasonsJson.isEmpty() ? seasonsJson.length() + " chars" : "none"));
                            }

                            // Resetuj seasons podatke za sledeći film
                            seasonsJson = "";
                            seasonsList.clear();
                            availableCaptions = "";
                            audioLanguage = "";
                        }

                        currentTag = null;
                    }
                    eventType = parser.next();
                }

                inputStream.close();
                return moviesList;

            } catch (Exception e) {
                Log.e("CATEGORY_LOAD", "Error loading from URL: " + urlString, e);
                return moviesList;
            }
        }
    }
}