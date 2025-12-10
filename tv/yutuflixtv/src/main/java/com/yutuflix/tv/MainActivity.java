package com.yutuflix.tv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    // ===========================================
    // 43 jezika koje aplikacija podržava
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

    private RecyclerView categoryRecycler;
    private CategoryAdapter categoryAdapter;
    private List<CategoryData> categories = new ArrayList<>();
    private List<Movie> allMovies = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout loadingContainer;
    private TextView loadingText;
    private EditText searchBar;
    private LinearLayout searchContainer;

    private Button btnAction, btnComedy, btnCrime, btnDocumentary, btnDrama,
            btnFamily, btnHorror, btnRomance, btnSciFi, btnThriller, btnWestern,
            btnSeries, btnFavorites, btnLanguages, btnAbout, btnPrivacy, btnSearch;

    private SharedPreferences blockedVideosPrefs;

    private final LinkedHashMap<String, String> categoryMap = new LinkedHashMap<String, String>() {{
        put("Action", "https://sevcet.github.io/schoder/Action_100.xml");
        put("Comedy", "https://sevcet.github.io/schoder/Comedy_100.xml");
        put("Crime", "https://sevcet.github.io/schoder/Crime_100.xml");
        put("Documentary", "https://sevcet.github.io/schoder/Documentary_100.xml");
        put("Drama", "https://sevcet.github.io/schoder/Drama_100.xml");
        put("Family", "https://sevcet.github.io/schoder/Family_100.xml");
        put("Horror", "https://sevcet.github.io/schoder/Horror_100.xml");
        put("Romance", "https://sevcet.github.io/schoder/Romance_100.xml");
        put("SciFi", "https://sevcet.github.io/schoder/SciFi_100.xml");
        put("Thriller", "https://sevcet.github.io/schoder/Thriller_100.xml");
        put("Western", "https://sevcet.github.io/schoder/Western_100.xml");
        put("Series", "https://sevcet.github.io/exyuflix/domace_serije.xml");
    }};

    private boolean isSearchActive = false;
    private boolean isShowingSearchResults = false;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tv);

        initializeBlockedVideos();
        initViews();
        setupRecyclerView();
        setupButtons();
        setupSearchBar();
        loadAllCategories();
    }

    private void initializeBlockedVideos() {
        blockedVideosPrefs = getSharedPreferences("blocked_videos", MODE_PRIVATE);
    }

    private void initViews() {
        categoryRecycler = findViewById(R.id.categoryRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        loadingContainer = findViewById(R.id.loadingContainer);
        loadingText = findViewById(R.id.loadingText);
        searchContainer = findViewById(R.id.searchContainer);
        searchBar = findViewById(R.id.searchBar);

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

    private void setupRecyclerView() {
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categories, movie -> {
            if ("serija".equalsIgnoreCase(movie.getType()) ||
                    "series".equalsIgnoreCase(movie.getType())) {
                openSeriesDetails(movie);
            } else {
                openFilmDetails(movie);
            }
        });
        categoryRecycler.setAdapter(categoryAdapter);
    }

    private void setupButtons() {

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

        btnFavorites.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class)));

        btnLanguages.setOnClickListener(v -> showLanguageDialog());
        btnAbout.setOnClickListener(v -> showAboutDialog());

        btnPrivacy.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PrivacyActivity.class)));

        btnSearch.setOnClickListener(v -> {
            if (isSearchActive) hideSearch();
            else showSearch();
        });
    }

    private void setupSearchBar() {
        searchBar.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

                    String q = searchBar.getText().toString().trim();
                    if (q.isEmpty()) {
                        Toast.makeText(this, "Enter a search term", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    performSearch(q);
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

    private void showSearch() {
        isSearchActive = true;
        searchContainer.setVisibility(View.VISIBLE);
        searchBar.postDelayed(() -> {
            searchBar.requestFocus();
            searchBar.selectAll();
        }, 120);
    }

    private void hideSearch() {
        isSearchActive = false;
        searchContainer.setVisibility(View.GONE);
        searchBar.setText("");
        hideKeyboard();

        if (isShowingSearchResults) {
            isShowingSearchResults = false;
            loadAllCategories();
        }
    }

    private void hideSearchUIOnly() {
        searchContainer.setVisibility(View.GONE);
        hideKeyboard();
        isSearchActive = false;
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }

    private void performSearch(String q) {
        List<Movie> results = new ArrayList<>();

        for (Movie m : allMovies) {
            if (m.getTitle().toLowerCase().contains(q.toLowerCase()) ||
                    m.getGenre().toLowerCase().contains(q.toLowerCase()) ||
                    (m.getYear() != null && m.getYear().toLowerCase().contains(q.toLowerCase()))) {

                results.add(m);
            }
        }

        if (results.isEmpty()) {
            Toast.makeText(this, "No results for: " + q, Toast.LENGTH_SHORT).show();
            return;
        }

        isShowingSearchResults = true;

        categories.clear();
        categories.add(new CategoryData("Search results for: " + q, results));
        categoryAdapter.notifyDataSetChanged();

        hideSearchUIOnly();
    }

    private void openCategory(String categoryName, String url) {
        Intent i = new Intent(MainActivity.this, CategoryActivity.class);
        i.putExtra("categoryName", categoryName);
        i.putExtra("xmlUrl", url);
        startActivity(i);
    }

    private void openFilmDetails(Movie movie) {
        Intent i = new Intent(MainActivity.this, DetailsActivityFilm.class);
        i.putExtra("title", movie.getTitle());
        i.putExtra("year", movie.getYear());
        i.putExtra("genre", movie.getGenre());
        i.putExtra("description", movie.getDescription());
        i.putExtra("imageUrl", movie.getImageUrl());
        i.putExtra("videoId", movie.getVideoId());
        startActivity(i);
    }

    private void openSeriesDetails(Movie movie) {
        Intent i = new Intent(MainActivity.this, DetailsActivitySeries.class);
        i.putExtra("title", movie.getTitle());
        i.putExtra("year", movie.getYear());
        i.putExtra("genre", movie.getGenre());
        i.putExtra("description", movie.getDescription());
        i.putExtra("imageUrl", movie.getImageUrl());
        i.putExtra("videoId", movie.getVideoId());
        i.putExtra("seasonsJson", movie.getSeasonsJson());
        startActivity(i);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearchActive) {
                hideSearch();
                return true;
            }
            if (isShowingSearchResults) {
                isShowingSearchResults = false;
                loadAllCategories();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    // ======================================================
    //            JEZIČKI DIJALOG (43 jezika)
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
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(50, 30, 50, 30);
        box.setBackgroundColor(Color.BLACK);

        TextView txt = new TextView(this);
        txt.setText(
                "Content Technical Viewer\n\n" +
                        "This application works as a video aggregator using the YouTube embed player. " +
                        "All metadata is loaded dynamically from external XML sources.\n\n" +
                        "Architecture:\n" +
                        "• Metadata: GitHub Pages XML\n" +
                        "• Streaming: YouTube official embed\n" +
                        "• Local storage: Favorites\n\n" +
                        "Disclaimer:\n" +
                        "This app does not host or distribute any videos. Content is streamed directly " +
                        "from YouTube according to their Terms of Service."
        );

        txt.setTextColor(Color.WHITE);
        txt.setTextSize(16f);

        box.addView(txt);
        scroll.addView(box);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("About")
                .setView(scroll)
                .setPositiveButton("OK", null)
                .create();

        dialog.show();
    }

    private void loadAllCategories() {
        new LoadAllCategoriesTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null)
            executorService.shutdown();
    }

    private class LoadAllCategoriesTask extends AsyncTask<Void, Integer, List<CategoryData>> {

        int totalCategories;
        int loadedCategories;

        @Override
        protected void onPreExecute() {
            loadingContainer.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            categoryRecycler.setVisibility(View.GONE);
            loadingText.setText("Loading categories...");

            totalCategories = categoryMap.size();
            loadedCategories = 0;
        }

        @Override
        protected List<CategoryData> doInBackground(Void... voids) {

            Map<String, CategoryData> map = new LinkedHashMap<>();
            CountDownLatch latch = new CountDownLatch(totalCategories);

            for (String name : categoryMap.keySet())
                map.put(name, new CategoryData(name, new ArrayList<>()));

            for (Map.Entry<String, String> e : categoryMap.entrySet()) {

                String categoryName = e.getKey();
                String url = e.getValue();

                executorService.execute(() -> {

                    try {
                        List<Movie> movies = loadCategoryFromUrl(url);
                        if (movies != null)
                            map.put(categoryName, new CategoryData(categoryName, movies));

                    } catch (Exception ex) {
                        Log.e("CATEGORY_LOAD", "Error loading " + categoryName, ex);
                    }

                    loadedCategories++;
                    publishProgress(loadedCategories, totalCategories);
                    latch.countDown();
                });
            }

            try { latch.await(); } catch (Exception ignored) {}

            List<CategoryData> result = new ArrayList<>();
            for (String name : categoryMap.keySet()) {
                CategoryData d = map.get(name);
                if (d != null && !d.getMovies().isEmpty())
                    result.add(d);
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... v) {
            loadingText.setText("Loading categories... (" + v[0] + "/" + v[1] + ")");
        }

        @Override
        protected void onPostExecute(List<CategoryData> result) {

            loadingContainer.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            categoryRecycler.setVisibility(View.VISIBLE);

            categories.clear();
            allMovies.clear();

            if (result != null) {
                categories.addAll(result);

                for (CategoryData cd : categories)
                    allMovies.addAll(cd.getMovies());

                categoryAdapter.notifyDataSetChanged();

            } else {
                Toast.makeText(MainActivity.this,
                        "No content available",
                        Toast.LENGTH_LONG).show();
            }
        }

        private List<Movie> loadCategoryFromUrl(String urlString) {

            List<Movie> movies = new ArrayList<>();

            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                InputStream input = conn.getInputStream();

                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(input, null);

                String title = null, year = null, genre = null, type = null,
                        description = null, imageUrl = null, videoId = null;

                List<Season> seasons = null;
                Season currentSeason = null;

                int eventType = parser.getEventType();
                boolean insideMovie = false;

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    String tag = parser.getName();

                    switch (eventType) {

                        case XmlPullParser.START_TAG:

                            if ("movie".equals(tag)) {
                                insideMovie = true;

                                title = "";
                                year = "";
                                genre = "";
                                type = "";
                                description = "";
                                imageUrl = "";
                                videoId = "";

                                seasons = new ArrayList<>();
                            }

                            if (!insideMovie) break;

                            if ("title".equals(tag)) title = parser.nextText();
                            else if ("year".equals(tag)) year = parser.nextText();
                            else if ("genre".equals(tag)) genre = parser.nextText();
                            else if ("type".equals(tag)) type = parser.nextText();
                            else if ("description".equals(tag)) description = parser.nextText();
                            else if ("imageUrl".equals(tag)) imageUrl = parser.nextText();
                            else if ("videoId".equals(tag)) videoId = parser.nextText();

                            else if ("season".equals(tag)) {

                                int seasonNum = Integer.parseInt(
                                        parser.getAttributeValue(null, "number")
                                );

                                currentSeason = new Season(seasonNum, new ArrayList<>());
                            }

                            else if ("episode".equals(tag) && currentSeason != null) {

                                String epTitle = "";
                                String epThumb = "";
                                String epVideo = "";

                                int innerEvent;
                                while (true) {
                                    innerEvent = parser.next();
                                    String innerTag = parser.getName();

                                    if (innerEvent == XmlPullParser.START_TAG) {

                                        if ("title".equals(innerTag))
                                            epTitle = parser.nextText();

                                        else if ("imageUrl".equals(innerTag))
                                            epThumb = parser.nextText();

                                        else if ("videoId".equals(innerTag))
                                            epVideo = parser.nextText();
                                    }

                                    if (innerEvent == XmlPullParser.END_TAG &&
                                            "episode".equals(innerTag)) break;
                                }

                                Episode ep = new Episode(
                                        epTitle,
                                        epThumb,
                                        epVideo
                                );

                                currentSeason.getEpisodes().add(ep);
                            }

                            break;

                        case XmlPullParser.END_TAG:

                            if ("season".equals(tag)) {
                                seasons.add(currentSeason);
                                currentSeason = null;
                            }

                            if ("movie".equals(tag)) {

                                insideMovie = false;

                                String seasonsJson = "";

                                if ("serija".equalsIgnoreCase(type) ||
                                        "series".equalsIgnoreCase(type)) {

                                    JSONArray seasonsArray = new JSONArray();

                                    for (Season s : seasons) {

                                        JSONObject seasonObj = new JSONObject();
                                        seasonObj.put("number", s.getNumber());

                                        JSONArray epsArray = new JSONArray();

                                        for (Episode ep : s.getEpisodes()) {
                                            JSONObject epObj = new JSONObject();

                                            epObj.put("title", ep.getTitle());
                                            epObj.put("imageUrl", ep.getImageUrl());
                                            epObj.put("videoId", ep.getVideoId());

                                            epsArray.put(epObj);
                                        }

                                        seasonObj.put("episodes", epsArray);
                                        seasonsArray.put(seasonObj);
                                    }

                                    seasonsJson = seasonsArray.toString();
                                }

                                Movie m = new Movie(
                                        title, year, genre, type,
                                        description, imageUrl, videoId,
                                        seasons,
                                        seasonsJson
                                );

                                movies.add(m);
                            }

                            break;
                    }

                    eventType = parser.next();
                }

                input.close();
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return movies;
        }
    }
}
