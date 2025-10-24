package com.yutuflix.tv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private List<Movie> allMovies = new ArrayList<>(); // Za pretragu
    private MovieAdapter movieAdapter;

    // BUTTON VARIJABLE
    private Button btnHome, btnSearch, btnDomaciFilmovi, btnDomaceSerije, btnAkcija;
    private Button btnKomedija, btnHoror, btnSciFi, btnRomansa, btnFavorites;

    // SEARCH VARIJABLE
    private EditText searchBar;
    private LinearLayout searchContainer;
    private boolean isSearchActive = false;
    private boolean isShowingSearchResults = false; // NOVO: da pratimo da li prikazujemo rezultate pretrage

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

        // SEARCH VIEWS
        searchBar = findViewById(R.id.searchBar);
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
                            Toast.makeText(CategoryActivity.this, "Unesite termin za pretragu", Toast.LENGTH_SHORT).show();
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
            hideSearch();
            Intent intent = new Intent(CategoryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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

        // CATEGORY BUTTONS
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
            // Prikaži rezultate pretrage
            movieList.clear();
            movieList.addAll(searchResults);
            movieAdapter.notifyDataSetChanged();

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

    private void openCategory(String categoryName, String xmlUrl) {
        if (categoryName.equals(currentCategoryName)) {
            // Ako je ista kategorija, samo osveži
            hideSearch();
            new LoadXmlTask().execute(xmlUrl);
        } else {
            // Ako je druga kategorija, otvori novu aktivnost
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
        // Global back button handling
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSearchActive) {
                hideSearch();
                return true;
            } else if (isShowingSearchResults) {
                // Ako prikazujemo rezultate pretrage, back dugme treba da vrati na originalne podatke
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
                String seasonsJson = "";

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

                // Popuni allMovies za pretragu
                allMovies.clear();
                allMovies.addAll(result);

                movieAdapter.notifyDataSetChanged();

                Toast.makeText(CategoryActivity.this, "Učitano " + movieList.size() + " video sadržaja", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CategoryActivity.this, "Nema video sadržaja u ovoj kategoriji", Toast.LENGTH_LONG).show();
            }
        }
    }
}