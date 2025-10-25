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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
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

    // BUTTON VARIJABLE
    private Button btnHome, btnSearch, btnDomaciFilmovi, btnDomaceSerije, btnAkcija;
    private Button btnKomedija, btnHoror, btnSciFi, btnRomansa, btnMisterija, btnDokumentarni;
    private Button btnAnimirani, btnFavorites, btnAbout, btnPrivacy, btnShare;

    // KATEGORIJE SA SPOLJNIM XML LINKOVIMA - FIKSNI REDOSLED
    private final LinkedHashMap<String, String> categoryMap = new LinkedHashMap<String, String>() {{
        put("Domaci Filmovi", "https://sevcet.github.io/exyuflix/domaci_filmovi.xml");
        put("Domace Serije", "https://sevcet.github.io/exyuflix/domace_serije.xml");
        put("Akcija", "https://sevcet.github.io/exyuflix/akcija.xml");
        put("Komedija", "https://sevcet.github.io/exyuflix/komedija.xml");
        put("Horor", "https://sevcet.github.io/exyuflix/horor.xml");
        put("Sci-Fi", "https://sevcet.github.io/exyuflix/sci_fi.xml");
        put("Romansa", "https://sevcet.github.io/exyuflix/romansa.xml");
        put("Misterija", "https://sevcet.github.io/exyuflix/misterija.xml");
        put("Dokumentarni", "https://sevcet.github.io/exyuflix/dokumentarni.xml");
        put("Animirani", "https://sevcet.github.io/exyuflix/animirani.xml");
    }};

    // TRAJNO SKLADIŠTENJE BLOKIRANIH VIDEO ID-JEVA
    private SharedPreferences blockedVideosPrefs;
    private CacheManager cacheManager;

    // VRATI NA ORIGINALNI XML SA FILMOVIMA
    String xmlUrl = "https://sevcet.github.io/yutuflixapp.xml";

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

        // INICIJALIZUJ CACHE MANAGER
        cacheManager = new CacheManager(this);

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
        btnMisterija = findViewById(R.id.btnMisterija);
        btnDokumentarni = findViewById(R.id.btnDokumentarni);
        btnAnimirani = findViewById(R.id.btnAnimirani);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnAbout = findViewById(R.id.btnAbout);
        btnPrivacy = findViewById(R.id.btnPrivacy);
        btnShare = findViewById(R.id.btnShare);
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
                            Toast.makeText(MainActivity.this, "Unesite termin za pretragu", Toast.LENGTH_SHORT).show();
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

        // CATEGORY BUTTONS - OTVARAJU CATEGORY ACTIVITY
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

        // FAVORITES BUTTON - TEMPORARY
        btnFavorites.setOnClickListener(v -> {
            hideSearch();
            Toast.makeText(this, "Favorites funkcionalnost će biti dodata uskoro", Toast.LENGTH_SHORT).show();
        });

        // ABOUT BUTTON
        btnAbout.setOnClickListener(v -> {
            hideSearch();
            ScrollView scrollView = new ScrollView(MainActivity.this);
            TextView message = new TextView(MainActivity.this);

            message.setText(
                    "🔍 Tehnički Preglednik Sadržaja\n\n" +
                            "Ova aplikacija funkcioniše kao video agregator koji koristi YouTube embed API za prikaz sadržaja. Svi metapodaci (metadata) se dinamički učitavaju sa eksternih XML izvora.\n\n" +
                            "🔄 Tehnička Arhitektura:\n" +
                            "• Metadata: GitHub Pages XML feedovi\n" +
                            "• Video streaming: YouTube official embed player\n" +
                            "• Lokalno skladištenje: Omiljeni sadržaji\n\n" +
                            "📺 Način Rada:\n" +
                            "Aplikacija ne hostira nikakav video sadržaj. Svi video zapisi se reprodukuju direktno sa YouTube servera putem službenog embed sistema, uz poštovanje autorskih prava i uslova korišćenja.\n\n" +
                            "⚖️ Pravni Disclaimer:\n" +
                            "Ova aplikacija je tehnološki preglednik i ne poseduje niti distribuira video sadržaje. Svi autorski materijali pripadaju njihovim vlasnicima. Korišćenje aplikacije podrazumeva saglasnost sa YouTube uslovima korišćenja."
            );
            message.setTextColor(Color.BLACK);
            message.setPadding(50, 30, 50, 30);
            message.setTextSize(16f);

            scrollView.addView(message);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("O aplikaciji")
                    .setView(scrollView)
                    .setPositiveButton("OK", null)
                    .show();
        });

        // PRIVACY BUTTON - TEMPORARY
        btnPrivacy.setOnClickListener(v -> {
            hideSearch();
            Toast.makeText(this, "Privacy funkcionalnost će biti dodata uskoro", Toast.LENGTH_SHORT).show();
        });

        // SHARE BUTTON
        btnShare.setOnClickListener(v -> {
            hideSearch();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Preporučujem ovu aplikaciju");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.yutuflix.tv");
            startActivity(Intent.createChooser(shareIntent, "Podeli putem"));
        });

        // TV FOCUS LISTENERS ZA SVE BUTTONE
        setupButtonFocusListeners();
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
        btnShare.setOnFocusChangeListener(focusListener);
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
            Toast.makeText(this, "Nema rezultata za: " + query, Toast.LENGTH_SHORT).show();
        } else {
            // Prikaži rezultate pretrage kao jednu kategoriju
            List<CategoryData> searchCategory = new ArrayList<>();
            searchCategory.add(new CategoryData("Rezultati pretrage za: " + query, searchResults));

            categories.clear();
            categories.addAll(searchCategory);
            categoryAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Pronađeno " + searchResults.size() + " rezultata", Toast.LENGTH_SHORT).show();

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
    private void openCategory(String categoryName, String categoryUrl) {
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        intent.putExtra("categoryName", categoryName);
        intent.putExtra("xmlUrl", categoryUrl);
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
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Greška pri otvaranju filma", Toast.LENGTH_SHORT).show();
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
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Greška pri otvaranju serije", Toast.LENGTH_SHORT).show();
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
            loadingText.setText("Učitavam kategorije...");
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

            for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
                String categoryName = entry.getKey();
                String categoryUrl = entry.getValue();

                executorService.execute(() -> {
                    try {
                        List<Movie> movies = loadCategoryFromUrl(categoryUrl);
                        if (movies != null && !movies.isEmpty()) {
                            synchronized (categoryDataMap) {
                                categoryDataMap.put(categoryName, new CategoryData(categoryName, movies));
                            }
                            Log.d("CATEGORY_LOAD", "Successfully loaded: " + categoryName + " (" + movies.size() + " movies)");
                        } else {
                            Log.d("CATEGORY_LOAD", "No movies found in: " + categoryName);
                            // Ako nema filmova, ostavi praznu kategoriju da se zadrži redosled
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
            loadingText.setText("Učitavam kategorije... (" + current + "/" + total + ")");
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
                        "Učitano " + categories.size() + " kategorija sa " + totalMovies + " filmova (" + totalSeries + " serija)",
                        Toast.LENGTH_SHORT).show();

                Log.d("CATEGORY_RESULT", "Total: " + categories.size() + " categories, " + totalMovies + " movies, " + totalSeries + " series");

                // DEBUG: Ispiši redosled kategorija
                for (int i = 0; i < categories.size(); i++) {
                    Log.d("CATEGORY_ORDER", (i + 1) + ". " + categories.get(i).getCategoryName());
                }

            } else {
                Toast.makeText(MainActivity.this, "Nema video sadržaja", Toast.LENGTH_LONG).show();
                Log.e("CATEGORY_RESULT", "Failed to load any categories");

                // Očisti prikaz ako nema podataka
                categories.clear();
                allMovies.clear();
                categoryAdapter.notifyDataSetChanged();
            }
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
                Log.e("CATEGORY_LOAD", "Error loading from URL: " + urlString, e);
                return moviesList;
            }
        }
    }
}