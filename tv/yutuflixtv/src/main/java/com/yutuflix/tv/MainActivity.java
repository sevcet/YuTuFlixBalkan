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
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    private Button btnKomedija, btnHoror, btnSciFi, btnRomansa, btnFavorites;

    private String xmlUrl = "https://sevcet.github.io/yutuflixapp.xml";
    private boolean isSearchActive = false;
    private boolean isShowingSearchResults = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tv);

        initViews();
        setupButtonNavigation();
        setupSearchBar();
        setupRecyclerView();

        // Učitaj podatke
        new LoadXmlTask().execute(xmlUrl);
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
        btnFavorites = findViewById(R.id.btnFavorites);
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
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categories, new CategoryAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                // Proveri da li je serija - po tipu ili po postojanju seasonsJson
                boolean isSeries = "serija".equalsIgnoreCase(movie.getType()) ||
                        "series".equalsIgnoreCase(movie.getType());

                Log.d("MOVIE_CLICK", "Title: " + movie.getTitle() +
                        ", Type: " + movie.getType() +
                        ", SeasonsJson: " + (movie.getSeasonsJson() != null ? movie.getSeasonsJson().length() : 0) +
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
        // HOME BUTTON - refresh
        btnHome.setOnClickListener(v -> {
            hideSearch();
            new LoadXmlTask().execute(xmlUrl);
        });

        // SEARCH BUTTON - toggle search bar
        btnSearch.setOnClickListener(v -> {
            if (isSearchActive) {
                hideSearch();
            } else {
                showSearch();
            }
        });

        // FAVORITES BUTTON
        btnFavorites.setOnClickListener(v -> {
            hideSearch();
            // TODO: Implementiraj FavoritesActivity za TV
            Toast.makeText(this, "Favorites funkcionalnost će biti dodata uskoro", Toast.LENGTH_SHORT).show();
        });

        // CATEGORY BUTTONS - OTVARAJU CATEGORY ACTIVITY SA ODGOVARAJUĆIM XML-OM
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
        btnFavorites.setOnFocusChangeListener(focusListener);
        btnDomaciFilmovi.setOnFocusChangeListener(focusListener);
        btnDomaceSerije.setOnFocusChangeListener(focusListener);
        btnAkcija.setOnFocusChangeListener(focusListener);
        btnKomedija.setOnFocusChangeListener(focusListener);
        btnHoror.setOnFocusChangeListener(focusListener);
        btnSciFi.setOnFocusChangeListener(focusListener);
        btnRomansa.setOnFocusChangeListener(focusListener);
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
            new LoadXmlTask().execute(xmlUrl);
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
            Log.d("SERIES_DEBUG", "SeasonsJson length: " + (movie.getSeasonsJson() != null ? movie.getSeasonsJson().length() : 0));

            Intent intent = new Intent(MainActivity.this, DetailsActivitySeries.class);
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
        // Global back button handling
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearchActive) {
                hideSearch();
                return true;
            } else if (isShowingSearchResults) {
                // Ako prikazujemo rezultate pretrage, back dugme treba da vrati na originalne podatke
                isShowingSearchResults = false;
                new LoadXmlTask().execute(xmlUrl);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class LoadXmlTask extends AsyncTask<String, Void, List<CategoryData>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            loadingContainer.setVisibility(View.VISIBLE);
            loadingText.setText("Učitavam filmove...");
            categoryRecycler.setVisibility(View.GONE);
        }

        @Override
        protected List<CategoryData> doInBackground(String... urls) {
            List<CategoryData> categoryList = new ArrayList<>();
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
                String currentCategory = "";

                String title = "", year = "", genre = "", type = "film", description = "", imageUrl = "", videoId = "";
                String seasonsJson = "";
                boolean inSeasonsJson = false;
                StringBuilder seasonsJsonBuilder = new StringBuilder();

                int movieCount = 0;
                int seriesCount = 0;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();
                        if ("category".equals(currentTag)) {
                            currentCategory = parser.getAttributeValue(null, "name");
                            if (currentCategory == null) currentCategory = "";
                            Log.d("XML_PARSER", "Category: " + currentCategory);
                        } else if ("movie".equals(currentTag)) {
                            // Resetuj podatke
                            title = ""; year = ""; genre = ""; type = "film";
                            description = ""; imageUrl = ""; videoId = "";
                            seasonsJson = "";
                            inSeasonsJson = false;
                            seasonsJsonBuilder = new StringBuilder();
                        } else if ("seasonsJson".equals(currentTag)) {
                            inSeasonsJson = true;
                            seasonsJsonBuilder = new StringBuilder();
                            Log.d("XML_PARSER", "Started seasonsJson tag for: " + title);
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (currentTag != null) {
                            String text = parser.getText().trim();
                            if (inSeasonsJson) {
                                // Sakupljaj tekst unutar seasonsJson taga
                                if (!text.isEmpty()) {
                                    seasonsJsonBuilder.append(text);
                                    Log.d("XML_PARSER", "Added to seasonsJson: " + text.length() + " chars");
                                }
                            } else {
                                // Obični tagovi
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
                        if ("seasonsJson".equals(parser.getName())) {
                            inSeasonsJson = false;
                            seasonsJson = seasonsJsonBuilder.toString().trim();
                            Log.d("XML_PARSER", "Finished seasonsJson for: " + title + ", length: " + seasonsJson.length());
                            if (!seasonsJson.isEmpty()) {
                                Log.d("XML_PARSER", "First 50 chars: " + seasonsJson.substring(0, Math.min(seasonsJson.length(), 50)));
                            }
                        } else if ("movie".equals(parser.getName())) {
                            // Kraj filma - dodaj u listu
                            if (!title.isEmpty()) {
                                movieCount++;

                                // DEBUG: Ispisi podatke o filmu/seriji
                                Log.d("XML_PARSER", "Movie " + movieCount + ": " + title +
                                        " | Type: " + type +
                                        " | SeasonsJson: " + (!seasonsJson.isEmpty() ? "YES (" + seasonsJson.length() + " chars)" : "NO"));

                                Movie movie = new Movie(title, year, genre, type, description, imageUrl, videoId, null, seasonsJson);
                                moviesList.add(movie);

                                // Dodaj u odgovarajuću kategoriju
                                boolean categoryExists = false;
                                for (CategoryData category : categoryList) {
                                    if (category.getCategoryName().equals(currentCategory)) {
                                        category.getMovies().add(movie);
                                        categoryExists = true;
                                        break;
                                    }
                                }

                                if (!categoryExists && !currentCategory.isEmpty()) {
                                    List<Movie> categoryMovies = new ArrayList<>();
                                    categoryMovies.add(movie);
                                    categoryList.add(new CategoryData(currentCategory, categoryMovies));
                                }

                                // Resetuj seasonsJson za sledeći film
                                seasonsJson = "";
                            }
                        }
                        currentTag = null;
                    }
                    eventType = parser.next();
                }

                inputStream.close();

                Log.d("XML_PARSER", "Total movies parsed: " + movieCount);
                Log.d("XML_PARSER", "Total series detected: " + seriesCount);

                // Ako nema kategorija, napravi jednu opštu kategoriju
                if (categoryList.isEmpty() && !moviesList.isEmpty()) {
                    categoryList.add(new CategoryData("Svi filmovi", moviesList));
                }

                return categoryList;

            } catch (Exception e) {
                Log.e("XML_PARSER", "Error parsing XML: " + e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
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
                int totalSeries = 0;
                for (CategoryData category : categories) {
                    allMovies.addAll(category.getMovies());
                    // Prebroj serije
                    for (Movie movie : category.getMovies()) {
                        if ("serija".equalsIgnoreCase(movie.getType()) || "series".equalsIgnoreCase(movie.getType())) {
                            totalSeries++;
                        }
                    }
                }

                categoryAdapter.notifyDataSetChanged();

                Toast.makeText(MainActivity.this,
                        "Učitano " + categories.size() + " kategorija sa " + allMovies.size() + " filmova (" + totalSeries + " serija)",
                        Toast.LENGTH_SHORT).show();

                Log.d("LOAD_RESULT", "Total categories: " + categories.size());
                Log.d("LOAD_RESULT", "Total movies: " + allMovies.size());
                Log.d("LOAD_RESULT", "Total series: " + totalSeries);

            } else {
                Toast.makeText(MainActivity.this, "Nema video sadržaja", Toast.LENGTH_LONG).show();
                Log.e("LOAD_RESULT", "Failed to load any content");
            }
        }
    }
}