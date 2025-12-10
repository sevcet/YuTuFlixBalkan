package com.yutuflix.tv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.appcompat.app.AlertDialog;
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

    // ===========================================
    // 43 podržana jezika
    // ===========================================
    private static final String[][] LANG_OPTIONS = {
            {"en", "English"},
            {"fr", "French"},
            {"de", "German"},
            {"es", "Spanish"},
            {"it", "Italian"},
            {"pt", "Portuguese"},
            {"nl", "Dutch"},
            {"pl", "Polish"},
            {"cs", "Czech"},
            {"sk", "Slovak"},
            {"sl", "Slovenian"},
            {"hr", "Croatian"},
            {"bs", "Bosnian"},
            {"sr", "Serbian"},
            {"mk", "Macedonian"},
            {"sq", "Albanian"},
            {"bg", "Bulgarian"},
            {"ro", "Romanian"},
            {"hu", "Hungarian"},
            {"tr", "Turkish"},
            {"el", "Greek"},
            {"da", "Danish"},
            {"sv", "Swedish"},
            {"fi", "Finnish"},
            {"no", "Norwegian"},
            {"is", "Icelandic"},
            {"et", "Estonian"},
            {"lv", "Latvian"},
            {"lt", "Lithuanian"},
            {"uk", "Ukrainian"},
            {"ru", "Russian"},
            {"ar", "Arabic"},
            {"fa", "Persian"},
            {"hi", "Hindi"},
            {"bn", "Bengali"},
            {"zh", "Chinese"},
            {"ja", "Japanese"},
            {"ko", "Korean"},
            {"vi", "Vietnamese"},
            {"id", "Indonesian"},
            {"ms", "Malay"},
            {"th", "Thai"}
    };

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<Movie> movieList = new ArrayList<>();
    private List<Movie> allMovies = new ArrayList<>();
    private MovieAdapter movieAdapter;

    private Button btnHome, btnAction, btnComedy, btnCrime, btnDocumentary, btnDrama, btnFamily;
    private Button btnHorror, btnRomance, btnSciFi, btnThriller, btnWestern, btnSeries;
    private Button btnFavorites, btnLanguages, btnAbout, btnPrivacy, btnSearch;

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
            Toast.makeText(this, "Error: Missing XML URL", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        searchBar = findViewById(R.id.searchBar);
        searchContainer = findViewById(R.id.searchContainer);

        btnHome = findViewById(R.id.btnHome);
        btnAction = findViewById(R.id.btnAction);
        btnComedy = findViewById(R.id.btnComedy);
        btnCrime = findViewById(R.id.btnCrime);
        btnDocumentary = findViewById(R.id.btnDocumentary);
        btnDrama = findViewById(R.id.btnDrama);
        btnFamily = findViewById(R.id.btnFamily);
        btnHorror = findViewById(R.id.btnHorror);
        btnRomance = findViewById(R.id.btnRomance);
        btnSciFi = findViewById(R.id.btnSciFi);
        btnThriller = findViewById(R.id.btnThriller);
        btnWestern = findViewById(R.id.btnWestern);
        btnSeries = findViewById(R.id.btnSeries);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnLanguages = findViewById(R.id.btnLanguages);
        btnAbout = findViewById(R.id.btnAbout);
        btnPrivacy = findViewById(R.id.btnPrivacy);
        btnSearch = findViewById(R.id.btnSearch);
    }

    private void setupSearchBar() {
        searchBar.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

                    String query = searchBar.getText().toString().trim();
                    if (!query.isEmpty()) performSearch(query);
                    else Toast.makeText(this, "Enter search term", Toast.LENGTH_SHORT).show();

                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideSearch();
                    return true;
                }
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(layoutManager);

        movieAdapter = new MovieAdapter(this, movieList, movie -> {
            if ("serija".equalsIgnoreCase(movie.getType()) ||
                    "series".equalsIgnoreCase(movie.getType())) {
                openSeriesDetails(movie);
            } else {
                openFilmDetails(movie);
            }
        });
        recyclerView.setAdapter(movieAdapter);
    }

    private void setupButtonNavigation() {

        btnSearch.setOnClickListener(v -> {
            if (isSearchActive) hideSearch();
            else showSearch();
        });

        btnHome.setOnClickListener(v -> {
            hideSearch();
            Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnAction.setOnClickListener(v -> openCategory("Action",
                "https://sevcet.github.io/schoder/Action.xml"));

        btnComedy.setOnClickListener(v -> openCategory("Comedy",
                "https://sevcet.github.io/schoder/Comedy.xml"));

        btnCrime.setOnClickListener(v -> openCategory("Crime",
                "https://sevcet.github.io/schoder/Crime.xml"));

        btnDocumentary.setOnClickListener(v -> openCategory("Documentary",
                "https://sevcet.github.io/schoder/Documentary.xml"));

        btnDrama.setOnClickListener(v -> openCategory("Drama",
                "https://sevcet.github.io/schoder/Drama.xml"));

        btnFamily.setOnClickListener(v -> openCategory("Family",
                "https://sevcet.github.io/schoder/Family.xml"));

        btnHorror.setOnClickListener(v -> openCategory("Horror",
                "https://sevcet.github.io/schoder/Horror.xml"));

        btnRomance.setOnClickListener(v -> openCategory("Romance",
                "https://sevcet.github.io/schoder/Romance.xml"));

        btnSciFi.setOnClickListener(v -> openCategory("SciFi",
                "https://sevcet.github.io/schoder/SciFi.xml"));

        btnThriller.setOnClickListener(v -> openCategory("Thriller",
                "https://sevcet.github.io/schoder/Thriller.xml"));

        btnWestern.setOnClickListener(v -> openCategory("Western",
                "https://sevcet.github.io/schoder/Western.xml"));

        btnSeries.setOnClickListener(v -> openCategory("Series",
                "https://sevcet.github.io/exyuflix/domace_serije.xml"));

        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        btnLanguages.setOnClickListener(v -> showLanguageDialog());

        btnAbout.setOnClickListener(v -> showAboutDialog());

        btnPrivacy.setOnClickListener(v ->
                startActivity(new Intent(CategoryActivity.this, PrivacyActivity.class)));

        // Focus efekti TV
        View.OnFocusChangeListener fl = (v, hasFocus) -> {
            if (hasFocus) {
                v.setBackgroundResource(R.drawable.tv_button_focused);
                v.setScaleX(1.05f);
                v.setScaleY(1.05f);
            } else {
                v.setBackgroundResource(R.drawable.tv_button_background);
                v.setScaleX(1f);
                v.setScaleY(1f);
            }
        };

        btnHome.setOnFocusChangeListener(fl);
        btnAction.setOnFocusChangeListener(fl);
        btnComedy.setOnFocusChangeListener(fl);
        btnCrime.setOnFocusChangeListener(fl);
        btnDocumentary.setOnFocusChangeListener(fl);
        btnDrama.setOnFocusChangeListener(fl);
        btnFamily.setOnFocusChangeListener(fl);
        btnHorror.setOnFocusChangeListener(fl);
        btnRomance.setOnFocusChangeListener(fl);
        btnSciFi.setOnFocusChangeListener(fl);
        btnThriller.setOnFocusChangeListener(fl);
        btnWestern.setOnFocusChangeListener(fl);
        btnSeries.setOnFocusChangeListener(fl);
        btnFavorites.setOnFocusChangeListener(fl);
        btnLanguages.setOnFocusChangeListener(fl);
        btnAbout.setOnFocusChangeListener(fl);
        btnPrivacy.setOnFocusChangeListener(fl);
        btnSearch.setOnFocusChangeListener(fl);
    }

    // ======================================================
    //            JEZIČKI DIJALOG (isto kao MainActivity)
    // ======================================================
    private void showLanguageDialog() {

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String currentLang = prefs.getString("user_language", "en");

        String[] langNames = new String[LANG_OPTIONS.length];
        int selectedIndex = 0;

        for (int i = 0; i < LANG_OPTIONS.length; i++) {
            langNames[i] = LANG_OPTIONS[i][1];
            if (LANG_OPTIONS[i][0].equals(currentLang)) {
                selectedIndex = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select language")
                .setSingleChoiceItems(langNames, selectedIndex, (dialog, which) -> {

                    String langCode = LANG_OPTIONS[which][0];
                    String langName = LANG_OPTIONS[which][1];

                    prefs.edit().putString("user_language", langCode).apply();

                    Toast.makeText(
                            this,
                            "Language set to " + langName,
                            Toast.LENGTH_SHORT
                    ).show();

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("About");
        builder.setMessage("This application aggregates YouTube video metadata from external XML feeds...");
        builder.setPositiveButton("OK", null);
        builder.show();
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

        if (isShowingSearchResults) {
            isShowingSearchResults = false;
            new LoadXmlTask().execute(currentXmlUrl);
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
            isShowingSearchResults = true;
            hideSearch();
        }
    }

    private void openCategory(String category, String url) {
        Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
        intent.putExtra("categoryName", category);
        intent.putExtra("xmlUrl", url);
        startActivity(intent);
        finish();
    }

    private void openFilmDetails(Movie movie) {
        Intent intent = new Intent(CategoryActivity.this, DetailsActivityFilm.class);
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("year", movie.getYear());
        intent.putExtra("genre", movie.getGenre());
        intent.putExtra("description", movie.getDescription());
        intent.putExtra("imageUrl", movie.getImageUrl());
        intent.putExtra("videoId", movie.getVideoId());
        startActivity(intent);
    }

    private void openSeriesDetails(Movie movie) {
        Intent intent = new Intent(CategoryActivity.this, DetailsActivitySeries.class);
        intent.putExtra("title", movie.getTitle());
        intent.putExtra("year", movie.getYear());
        intent.putExtra("genre", movie.getGenre());
        intent.putExtra("description", movie.getDescription());
        intent.putExtra("imageUrl", movie.getImageUrl());
        intent.putExtra("seasonsJson", movie.getSeasonsJson());
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearchActive) {
                hideSearch();
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

                InputStream inputStream = connection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(inputStream, "UTF-8");

                int eventType = parser.getEventType();
                String currentTag = null;

                String title = "", year = "", genre = "", type = "film", description = "",
                        imageUrl = "", videoId = "";

                String seasonsJson = "";
                boolean inSeasonsSection = false;
                boolean inSeason = false;
                boolean inEpisode = false;

                List<JSONObject> seasonsList = new ArrayList<>();
                JSONObject currentSeason = null;
                List<JSONObject> currentEpisodes = null;
                int seasonNumber = 0;

                String epTitle = "", epImage = "", epVideo = "";

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();

                        if ("movie".equals(currentTag)) {
                            title = ""; year = ""; genre = ""; type = "film"; description = "";
                            imageUrl = ""; videoId = ""; seasonsJson = "";
                            inSeasonsSection = false;
                            seasonsList.clear();
                        }

                        else if ("seasons".equals(currentTag)) {
                            inSeasonsSection = true;
                            seasonsList.clear();
                        }

                        else if ("season".equals(currentTag) && inSeasonsSection) {
                            inSeason = true;
                            seasonNumber = Integer.parseInt(parser.getAttributeValue(null, "number"));
                            currentEpisodes = new ArrayList<>();
                            currentSeason = new JSONObject();
                        }

                        else if ("episode".equals(currentTag) && inSeason) {
                            inEpisode = true;
                            epTitle = ""; epImage = ""; epVideo = "";
                        }
                    }

                    else if (eventType == XmlPullParser.TEXT) {
                        if (currentTag != null) {
                            String text = parser.getText().trim();

                            if (inEpisode) {
                                switch (currentTag) {
                                    case "title": epTitle = text; break;
                                    case "imageUrl": epImage = text; break;
                                    case "videoId": epVideo = text; break;
                                }
                            }

                            else if (!inSeasonsSection) {
                                switch (currentTag) {
                                    case "title": title = text; break;
                                    case "year": year = text; break;
                                    case "genre": genre = text; break;
                                    case "type": type = text; break;
                                    case "description": description = text; break;
                                    case "imageUrl": imageUrl = text; break;
                                    case "videoId": videoId = text; break;
                                }
                            }
                        }
                    }

                    else if (eventType == XmlPullParser.END_TAG) {
                        String tagName = parser.getName();

                        if ("episode".equals(tagName) && inEpisode) {
                            if (!epVideo.isEmpty()) {
                                JSONObject epObj = new JSONObject();
                                epObj.put("title", epTitle);
                                epObj.put("imageUrl", epImage);
                                epObj.put("videoId", epVideo);
                                currentEpisodes.add(epObj);
                            }
                            inEpisode = false;
                        }

                        else if ("season".equals(tagName) && inSeason) {
                            if (!currentEpisodes.isEmpty()) {
                                currentSeason.put("number", seasonNumber);
                                currentSeason.put("episodes", new JSONArray(currentEpisodes));
                                seasonsList.add(currentSeason);
                            }
                            inSeason = false;
                        }

                        else if ("seasons".equals(tagName)) {
                            if (!seasonsList.isEmpty()) {
                                seasonsJson = new JSONArray(seasonsList).toString();
                            }
                            inSeasonsSection = false;
                        }

                        else if ("movie".equals(tagName)) {
                            if (!seasonsJson.isEmpty()) type = "serija";

                            moviesList.add(new Movie(title, year, genre, type, description, imageUrl, videoId, null, seasonsJson));
                        }

                        currentTag = null;
                    }

                    eventType = parser.next();
                }

                return moviesList;

            } catch (Exception e) {
                Log.e("XML_ERROR", "Loading XML failed", e);
                return moviesList;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            movieList.clear();
            movieList.addAll(result);

            allMovies.clear();
            allMovies.addAll(result);

            movieAdapter.notifyDataSetChanged();
            Toast.makeText(CategoryActivity.this, "Loaded: " + result.size() + " items", Toast.LENGTH_SHORT).show();
        }
    }
}
