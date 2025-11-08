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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private RecyclerView categoryRecycler;
    private CategoryAdapter categoryAdapter;
    private List<CategoryData> categories = new ArrayList<>();
    private List<Movie> allMovies = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout loadingContainer;
    private TextView loadingText;
    private EditText searchBar;
    private LinearLayout searchContainer;

    // BUTTON VARIJABLE - GLOBALNI ŽANROVI
    private Button btnHome, btnSearch, btnLanguage, btnAction, btnComedy;
    private Button btnDrama, btnHorror, btnThriller, btnSciFi, btnRomance;
    private Button btnAnimation, btnCrime, btnTVSeries, btnClassic, btnDocumentary;
    private Button btnFavorites, btnAbout, btnPrivacy;

    // KATEGORIJE SA SPOLJNIM XML LINKOVIMA - GLOBALNI SADRŽAJ SA VIŠE XML FAJLOVA
    private final LinkedHashMap<String, String[]> categoryMap = new LinkedHashMap<String, String[]>() {{
        put("Trending Now", new String[]{
                "https://sevcet.github.io/exyuflix/trending1.xml",
                "https://sevcet.github.io/exyuflix/trending2.xml",
                "https://sevcet.github.io/exyuflix/trending3.xml"
        });
        put("Top Rated", new String[]{
                "https://sevcet.github.io/exyuflix/top_rated1.xml",
                "https://sevcet.github.io/exyuflix/top_rated2.xml"
        });
        put("New Releases", new String[]{
                "https://sevcet.github.io/exyuflix/new_releases1.xml",
                "https://sevcet.github.io/exyuflix/new_releases2.xml",
                "https://sevcet.github.io/exyuflix/new_releases3.xml"
        });
        put("Action & Adventure", new String[]{
                "https://sevcet.github.io/exyuflix/action1.xml",
                "https://sevcet.github.io/exyuflix/action2.xml",
                "https://sevcet.github.io/exyuflix/action3.xml",
                "https://sevcet.github.io/exyuflix/action4.xml"
        });
        put("Comedy", new String[]{
                "https://sevcet.github.io/exyuflix/comedy1.xml",
                "https://sevcet.github.io/exyuflix/comedy2.xml",
                "https://sevcet.github.io/exyuflix/comedy3.xml"
        });
        put("Drama", new String[]{
                "https://sevcet.github.io/exyuflix/drama1.xml",
                "https://sevcet.github.io/exyuflix/drama2.xml"
        });
        put("Horror", new String[]{
                "https://sevcet.github.io/exyuflix/horror1.xml",
                "https://sevcet.github.io/exyuflix/horror2.xml"
        });
        put("Thriller", new String[]{
                "https://sevcet.github.io/exyuflix/thriller1.xml",
                "https://sevcet.github.io/exyuflix/thriller2.xml"
        });
        put("Sci-Fi & Fantasy", new String[]{
                "https://sevcet.github.io/exyuflix/scifi1.xml",
                "https://sevcet.github.io/exyuflix/scifi2.xml",
                "https://sevcet.github.io/exyuflix/scifi3.xml"
        });
        put("Romance", new String[]{
                "https://sevcet.github.io/exyuflix/romance1.xml",
                "https://sevcet.github.io/exyuflix/romance2.xml"
        });
        put("Animation & Family", new String[]{
                "https://sevcet.github.io/exyuflix/animation1.xml",
                "https://sevcet.github.io/exyuflix/animation2.xml"
        });
        put("Crime & Mystery", new String[]{
                "https://sevcet.github.io/exyuflix/crime1.xml",
                "https://sevcet.github.io/exyuflix/crime2.xml"
        });
        put("TV Series", new String[]{
                "https://sevcet.github.io/exyuflix/series1.xml",
                "https://sevcet.github.io/exyuflix/series2.xml",
                "https://sevcet.github.io/exyuflix/series3.xml"
        });
        put("Classic Movies", new String[]{
                "https://sevcet.github.io/exyuflix/classic1.xml",
                "https://sevcet.github.io/exyuflix/classic2.xml"
        });
        put("Documentary", new String[]{
                "https://sevcet.github.io/exyuflix/documentary1.xml",
                "https://sevcet.github.io/exyuflix/documentary2.xml"
        });
    }};

    // TRAJNO SKLADIŠTENJE BLOKIRANIH VIDEO ID-JEVA
    private SharedPreferences blockedVideosPrefs;
    private LanguageManager languageManager;

    private boolean isSearchActive = false;
    private boolean isShowingSearchResults = false;

    // Executor za paralelno učitavanje
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tv);

        // INICIJALIZUJ TRAJNO SKLADIŠTE BLOKIRANIH VIDEO ID-JEVA
        initializeBlockedVideos();

        // INICIJALIZUJ LANGUAGE MANAGER
        languageManager = new LanguageManager(this);

        initViews();
        setupButtonNavigation();
        setupSearchBar();
        setupRecyclerView();

        // UČITAJ SVE KATEGORIJE
        loadAllCategories();
    }

    private void initializeBlockedVideos() {
        blockedVideosPrefs = getSharedPreferences("blocked_videos", MODE_PRIVATE);
    }

    private boolean isVideoPermanentlyBlocked(String videoId) {
        return videoId == null || videoId.isEmpty() || blockedVideosPrefs.getBoolean(videoId, false);
    }

    private void initViews() {
        categoryRecycler = findViewById(R.id.categoryRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        loadingContainer = findViewById(R.id.loadingContainer);
        loadingText = findViewById(R.id.loadingText);
        searchBar = findViewById(R.id.searchBar);

        // Search container
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
        // Postavi key listener za Enter (OK dugme na daljinskom)
        searchBar.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        // Enter/OK dugme - izvrši pretragu
                        String query = searchBar.getText().toString().trim();
                        if (!query.isEmpty()) {
                            performSearch(query);
                        } else {
                            Toast.makeText(MainActivity.this, "Enter search term", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                        // Back dugme - zatvori search
                        hideSearch();
                        return true;
                    }
                }
                return false;
            }
        });

        // Fokus listener za search bar
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchBar.setBackgroundResource(R.drawable.tv_button_focused);
                    // Automatski otvori tastaturu kada search bar dobije fokus
                    showKeyboard();
                } else {
                    searchBar.setBackgroundResource(R.drawable.tv_edittext_background);
                }
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        categoryRecycler.setLayoutManager(layoutManager);

        // Dodaj padding da se ne preklapa sa buttonima
        categoryRecycler.setPadding(0, 0, 0, 0);
        categoryRecycler.setClipToPadding(false);

        categoryAdapter = new CategoryAdapter(this, categories, new CategoryAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                // Proveri da li je serija - po tipu
                boolean isSeries = "serija".equalsIgnoreCase(movie.getType()) ||
                        "series".equalsIgnoreCase(movie.getType());

                Log.d("MOVIE_CLICK", "Title: " + movie.getTitle() +
                        ", Type: " + movie.getType() +
                        ", VideoId: " + movie.getVideoId() +
                        ", Audio: " + movie.getAudioLanguage() +
                        ", Captions: " + movie.getAvailableCaptions() +
                        ", IsSeries: " + isSeries);

                if (isSeries) {
                    openSeriesDetails(movie);
                } else {
                    openFilmDetails(movie);
                }
            }
        });
        categoryRecycler.setAdapter(categoryAdapter);
    }

    private void setupButtonNavigation() {
        // HOME BUTTON - refresh sve kategorije
        btnHome.setOnClickListener(v -> {
            hideSearch();
            loadAllCategories();
        });

        // SEARCH BUTTON - toggle search bar
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

        // FAVORITES BUTTON
        btnFavorites.setOnClickListener(v -> {
            hideSearch();
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        // CATEGORY BUTTONS - OTVARAJU CATEGORY ACTIVITY
        btnAction.setOnClickListener(v -> {
            hideSearch();
            openCategory("Action & Adventure", categoryMap.get("Action & Adventure"));
        });

        btnComedy.setOnClickListener(v -> {
            hideSearch();
            openCategory("Comedy", categoryMap.get("Comedy"));
        });

        btnDrama.setOnClickListener(v -> {
            hideSearch();
            openCategory("Drama", categoryMap.get("Drama"));
        });

        btnHorror.setOnClickListener(v -> {
            hideSearch();
            openCategory("Horror", categoryMap.get("Horror"));
        });

        btnThriller.setOnClickListener(v -> {
            hideSearch();
            openCategory("Thriller", categoryMap.get("Thriller"));
        });

        btnSciFi.setOnClickListener(v -> {
            hideSearch();
            openCategory("Sci-Fi & Fantasy", categoryMap.get("Sci-Fi & Fantasy"));
        });

        btnRomance.setOnClickListener(v -> {
            hideSearch();
            openCategory("Romance", categoryMap.get("Romance"));
        });

        btnAnimation.setOnClickListener(v -> {
            hideSearch();
            openCategory("Animation & Family", categoryMap.get("Animation & Family"));
        });

        btnCrime.setOnClickListener(v -> {
            hideSearch();
            openCategory("Crime & Mystery", categoryMap.get("Crime & Mystery"));
        });

        btnTVSeries.setOnClickListener(v -> {
            hideSearch();
            openCategory("TV Series", categoryMap.get("TV Series"));
        });

        btnClassic.setOnClickListener(v -> {
            hideSearch();
            openCategory("Classic Movies", categoryMap.get("Classic Movies"));
        });

        btnDocumentary.setOnClickListener(v -> {
            hideSearch();
            openCategory("Documentary", categoryMap.get("Documentary"));
        });

        // ABOUT BUTTON
        btnAbout.setOnClickListener(v -> {
            hideSearch();
            showAboutDialog();
        });

        // PRIVACY BUTTON - OTVARA PRIVACY ACTIVITY
        btnPrivacy.setOnClickListener(v -> {
            hideSearch();
            Intent intent = new Intent(MainActivity.this, PrivacyActivity.class);
            startActivity(intent);
        });

        // TV FOCUS LISTENERS ZA SVE BUTTONE
        setupButtonFocusListeners();
    }

    private void showLanguageSelectionDialog() {
        final List<String> selectedLanguages = new ArrayList<>(languageManager.getFavoriteLanguages());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
            loadAllCategories();
            Toast.makeText(MainActivity.this, "Languages saved: " + selectedLanguages.size() + " selected", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);

        builder.setNeutralButton("Select All", (dialog, which) -> {
            // Automatski selektuj sve jezike
            selectedLanguages.clear();
            selectedLanguages.addAll(Arrays.asList(languageCodes));

            // Sačuvaj i ponovo učitaj
            languageManager.setFavoriteLanguages(selectedLanguages);
            loadAllCategories();
            Toast.makeText(MainActivity.this, "All languages selected", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAboutDialog() {
        ScrollView scrollView = new ScrollView(MainActivity.this);
        LinearLayout container = new LinearLayout(MainActivity.this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 30, 50, 30);

        TextView message = new TextView(MainActivity.this);
        message.setText(
                "🔍 Content Aggregator\n\n" +
                        "This application functions as a video aggregator using YouTube embed API to display content. All metadata is dynamically loaded from external XML sources.\n\n" +
                        "🔄 Technical Architecture:\n" +
                        "• Metadata: GitHub Pages XML feeds\n" +
                        "• Video streaming: YouTube official embed player\n" +
                        "• Local storage: Favorite content\n\n" +
                        "📺 How it Works:\n" +
                        "The application does not host any video content. All videos are played directly from YouTube servers through the official embed system, respecting copyright and terms of use.\n\n" +
                        "⚖️ Legal Disclaimer:\n" +
                        "This application is a technological content browser and does not own or distribute video content. All copyrighted materials belong to their respective owners. Using the application implies agreement with YouTube terms of service."
        );

        // CRNA POZADINA I BELA SLOVA
        message.setTextColor(Color.WHITE);
        message.setTextSize(16f);
        message.setLineSpacing(1.2f, 1.2f);

        container.setBackgroundColor(Color.BLACK);
        container.addView(message);
        scrollView.addView(container);

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("About Application")
                .setView(scrollView)
                .setPositiveButton("OK", null)
                .create();

        // Postavi crnu pozadinu i bela slova za dialog
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.DKGRAY);
        });

        dialog.show();
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

    private void loadAllCategories() {
        new LoadAllCategoriesTask().execute();
    }

    private void showSearch() {
        isSearchActive = true;
        searchContainer.setVisibility(View.VISIBLE);
        // Postavi fokus na search bar sa malim delay-om
        searchBar.postDelayed(() -> {
            searchBar.requestFocus();
            searchBar.selectAll(); // Selektuj sav tekst
        }, 100);
    }

    private void hideSearch() {
        isSearchActive = false;
        searchContainer.setVisibility(View.GONE);
        searchBar.setText("");
        hideKeyboard();

        // Vrati fokus na prvi button
        btnHome.postDelayed(() -> {
            btnHome.requestFocus();
        }, 100);

        // SAMO ako prikazujemo rezultate pretrage, vrati na originalne podatke
        if (isShowingSearchResults) {
            isShowingSearchResults = false;
            loadAllCategories();
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
            // Prikaži rezultate pretrage kao jednu kategoriju
            List<CategoryData> searchCategory = new ArrayList<>();
            searchCategory.add(new CategoryData("Search results for: " + query, searchResults));

            categories.clear();
            categories.addAll(searchCategory);
            categoryAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Found " + searchResults.size() + " results", Toast.LENGTH_SHORT).show();

            // Oznaci da prikazujemo rezultate pretrage
            isShowingSearchResults = true;

            // Sakrij search UI ali NE ucitavaj originalne podatke
            hideSearchUIOnly();
        }
    }

    // NOVA METODA: Sakrij samo search UI bez ponovnog ucitavanja podataka
    private void hideSearchUIOnly() {
        isSearchActive = false;
        searchContainer.setVisibility(View.GONE);
        searchBar.setText("");
        hideKeyboard();

        // Vrati fokus na prvi button
        btnHome.postDelayed(() -> {
            btnHome.requestFocus();
        }, 100);
    }

    // OTVARA CATEGORY ACTIVITY SA ODGOVARAJUĆIM XML-OM
    private void openCategory(String categoryName, String[] categoryUrls) {
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        intent.putExtra("categoryName", categoryName);
        intent.putExtra("xmlUrls", categoryUrls);
        startActivity(intent);
    }

    private void openFilmDetails(Movie movie) {
        try {
            Intent intent = new Intent(MainActivity.this, DetailsActivityFilm.class);
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
            Log.d("SERIES_DEBUG", "Opening series: " + movie.getTitle());
            Log.d("SERIES_DEBUG", "VideoId: " + movie.getVideoId());
            Log.d("SERIES_DEBUG", "SeasonsJson: " + (movie.getSeasonsJson() != null ? movie.getSeasonsJson().length() : 0));

            Intent intent = new Intent(MainActivity.this, DetailsActivitySeries.class);
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
        // Global back button handling
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearchActive) {
                hideSearch();
                return true;
            } else if (isShowingSearchResults) {
                // Ako prikazujemo rezultate pretrage, back dugme treba da vrati na originalne podatke
                isShowingSearchResults = false;
                loadAllCategories();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // TASK KLASA ZA UČITAVANJE SVIH KATEGORIJA U MAIN ACTIVITY SA FIKSNIM REDOSLEDOM
    private class LoadAllCategoriesTask extends AsyncTask<Void, Integer, List<CategoryData>> {
        private int totalCategories;
        private int loadedCategories;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            loadingContainer.setVisibility(View.VISIBLE);
            loadingText.setText("Loading categories...");
            categoryRecycler.setVisibility(View.GONE);

            totalCategories = categoryMap.size();
            loadedCategories = 0;
        }

        @Override
        protected List<CategoryData> doInBackground(Void... voids) {
            // Koristimo LinkedHashMap da očuvamo redosled
            Map<String, CategoryData> categoryDataMap = new LinkedHashMap<>();
            CountDownLatch latch = new CountDownLatch(totalCategories);

            // Inicijalizuj sve kategorije u fiksnom redosledu
            for (String categoryName : categoryMap.keySet()) {
                categoryDataMap.put(categoryName, new CategoryData(categoryName, new ArrayList<>()));
            }

            for (Map.Entry<String, String[]> entry : categoryMap.entrySet()) {
                String categoryName = entry.getKey();
                String[] categoryUrls = entry.getValue();

                executorService.execute(() -> {
                    try {
                        List<Movie> movies = new ArrayList<>();
                        // UČITAJ IZ SVIH XML FAJLOVA ZA OVU KATEGORIJU
                        for (String categoryUrl : categoryUrls) {
                            List<Movie> moviesFromUrl = loadCategoryFromUrl(categoryUrl);
                            if (moviesFromUrl != null && !moviesFromUrl.isEmpty()) {
                                movies.addAll(moviesFromUrl);
                                Log.d("MULTI_XML", "Loaded " + moviesFromUrl.size() + " movies from: " + categoryUrl);
                            }
                        }

                        if (!movies.isEmpty()) {
                            // FILTRIRAJ FILMOVE PO JEZIKU
                            List<Movie> filteredMovies = filterMoviesByLanguage(movies);

                            synchronized (categoryDataMap) {
                                categoryDataMap.put(categoryName, new CategoryData(categoryName, filteredMovies));
                            }
                            Log.d("CATEGORY_LOAD", "Successfully loaded: " + categoryName + " (" + filteredMovies.size() + " filtered movies from " + movies.size() + " total)");
                        } else {
                            Log.d("CATEGORY_LOAD", "No movies found in: " + categoryName);
                        }
                    } catch (Exception e) {
                        Log.e("CATEGORY_LOAD", "Error loading category: " + categoryName, e);
                    } finally {
                        loadedCategories++;
                        publishProgress(loadedCategories, totalCategories);
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await(); // Čekaj da se sve kategorije učitaju
            } catch (InterruptedException e) {
                Log.e("CATEGORY_LOAD", "Interrupted while waiting for categories", e);
            }

            // Konvertuj mapu u listu očuvavajući redosled
            List<CategoryData> result = new ArrayList<>();
            for (String categoryName : categoryMap.keySet()) {
                CategoryData categoryData = categoryDataMap.get(categoryName);
                if (categoryData != null && !categoryData.getMovies().isEmpty()) {
                    result.add(categoryData);
                }
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int current = values[0];
            int total = values[1];
            loadingText.setText("Loading categories... (" + current + "/" + total + ")");
        }

        @Override
        protected void onPostExecute(List<CategoryData> result) {
            progressBar.setVisibility(View.GONE);
            loadingContainer.setVisibility(View.GONE);
            categoryRecycler.setVisibility(View.VISIBLE);

            if (result != null && !result.isEmpty()) {
                categories.clear();
                categories.addAll(result);

                // Popuni allMovies za pretragu
                allMovies.clear();
                for (CategoryData category : categories) {
                    allMovies.addAll(category.getMovies());
                }

                categoryAdapter.notifyDataSetChanged();

                int totalMovies = allMovies.size();
                int totalSeries = 0;
                for (Movie movie : allMovies) {
                    if ("serija".equalsIgnoreCase(movie.getType()) || "series".equalsIgnoreCase(movie.getType())) {
                        totalSeries++;
                    }
                }

                Toast.makeText(MainActivity.this,
                        "Loaded " + categories.size() + " categories with " + totalMovies + " movies (" + totalSeries + " series)",
                        Toast.LENGTH_SHORT).show();

                Log.d("CATEGORY_RESULT", "Total: " + categories.size() + " categories, " + totalMovies + " movies, " + totalSeries + " series");

                // DEBUG: Ispiši redosled kategorija
                for (int i = 0; i < categories.size(); i++) {
                    Log.d("CATEGORY_ORDER", (i + 1) + ". " + categories.get(i).getCategoryName() + " (" + categories.get(i).getMovies().size() + " movies)");
                }

            } else {
                Toast.makeText(MainActivity.this, "No video content available for selected languages", Toast.LENGTH_LONG).show();
                Log.e("CATEGORY_RESULT", "Failed to load any categories");

                // Očisti prikaz ako nema podataka
                categories.clear();
                allMovies.clear();
                categoryAdapter.notifyDataSetChanged();
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