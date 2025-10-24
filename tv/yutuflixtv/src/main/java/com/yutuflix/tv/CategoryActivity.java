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
            // DEBUG: Proveri šta šaljemo
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
                StringBuilder seasonsJsonBuilder = new StringBuilder();
                boolean inSeasonsSection = false;
                int seasonCount = 0;
                int episodeCount = 0;
                boolean inEpisode = false;
                String currentImageUrl = "", currentVideoId = "", currentTitle = "";

                Log.d("XML_DEBUG", "=== START PARSING XML ===");
                Log.d("XML_DEBUG", "XML URL: " + urls[0]);

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();
                        Log.d("XML_DEBUG", "START_TAG: " + currentTag);

                        if ("movie".equals(currentTag)) {
                            // Resetuj podatke
                            title = ""; year = ""; genre = ""; type = "film";
                            description = ""; imageUrl = ""; videoId = "";
                            seasonsJsonBuilder = new StringBuilder();
                            inSeasonsSection = false;
                            seasonCount = 0;
                            episodeCount = 0;
                            inEpisode = false;
                            Log.d("XML_DEBUG", "--- NEW MOVIE ---");
                        } else if ("seasons".equals(currentTag)) {
                            inSeasonsSection = true;
                            seasonsJsonBuilder.append("[");
                            Log.d("XML_DEBUG", "ENTERING SEASONS SECTION");
                        } else if ("season".equals(currentTag) && inSeasonsSection) {
                            seasonCount++;
                            episodeCount = 0;
                            seasonsJsonBuilder.append(seasonCount > 1 ? "," : "");
                            seasonsJsonBuilder.append("{\"number\":").append(seasonCount).append(",\"episodes\":[");
                            Log.d("XML_DEBUG", "START SEASON " + seasonCount);
                        } else if ("episode".equals(currentTag) && inSeasonsSection) {
                            inEpisode = true;
                            episodeCount++;
                            seasonsJsonBuilder.append(episodeCount > 1 ? "," : "");
                            seasonsJsonBuilder.append("{\"title\":\"\",\"imageUrl\":\"\",\"videoId\":\"\"");
                            currentImageUrl = "";
                            currentVideoId = "";
                            currentTitle = "";
                            Log.d("XML_DEBUG", "START EPISODE " + episodeCount);
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (currentTag != null) {
                            String text = parser.getText().trim();

                            if (inSeasonsSection && inEpisode) {
                                // U episodes sekciji - obrađujemo episode podatke
                                if ("imageUrl".equals(currentTag)) {
                                    currentImageUrl = text;
                                    seasonsJsonBuilder.append(",\"imageUrl\":\"").append(text).append("\"");
                                } else if ("videoId".equals(currentTag)) {
                                    currentVideoId = text;
                                    seasonsJsonBuilder.append(",\"videoId\":\"").append(text).append("\"");
                                } else if ("title".equals(currentTag)) {
                                    currentTitle = text;
                                    seasonsJsonBuilder.append(",\"title\":\"").append(text).append("\"");
                                }
                            } else if (inSeasonsSection) {
                                // Ignoriši tekst van episode tagova u seasons sekciji
                            } else {
                                // Van seasons sekcije - osnovni podaci
                                switch (currentTag) {
                                    case "title":
                                        title = text;
                                        Log.d("XML_DEBUG", "Title: " + text);
                                        break;
                                    case "year": year = text; break;
                                    case "genre": genre = text; break;
                                    case "type":
                                        type = text;
                                        Log.d("XML_DEBUG", "Type: " + text);
                                        break;
                                    case "description": description = text; break;
                                    case "imageUrl": imageUrl = text; break;
                                    case "videoId": videoId = text; break;
                                }
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        String tagName = parser.getName();
                        Log.d("XML_DEBUG", "END_TAG: " + tagName);

                        if ("episode".equals(tagName) && inSeasonsSection) {
                            seasonsJsonBuilder.append("}");
                            inEpisode = false;
                            Log.d("XML_DEBUG", "END EPISODE - Image: " + currentImageUrl + ", Video: " + currentVideoId);
                        } else if ("season".equals(tagName) && inSeasonsSection) {
                            seasonsJsonBuilder.append("]}");
                            Log.d("XML_DEBUG", "END SEASON " + seasonCount + " with " + episodeCount + " episodes");
                        } else if ("seasons".equals(tagName)) {
                            seasonsJsonBuilder.append("]");
                            inSeasonsSection = false;
                            Log.d("XML_DEBUG", "EXITING SEASONS SECTION");
                            Log.d("XML_DEBUG", "Generated seasonsJson length: " + seasonsJsonBuilder.length());
                            if (seasonsJsonBuilder.length() > 0) {
                                Log.d("XML_DEBUG", "First 100 chars: " + seasonsJsonBuilder.substring(0, Math.min(seasonsJsonBuilder.length(), 100)));
                            }
                        } else if ("movie".equals(tagName)) {
                            // Kraj filma - dodaj u listu
                            if (!title.isEmpty()) {
                                String seasonsJson = seasonsJsonBuilder.length() > 0 ? seasonsJsonBuilder.toString() : "";
                                Movie movie = new Movie(title, year, genre, type, description, imageUrl, videoId, null, seasonsJson);
                                movies.add(movie);

                                Log.d("XML_DEBUG", "=== MOVIE CREATED ===");
                                Log.d("XML_DEBUG", "Final - Title: " + title + ", Type: " + type);
                                Log.d("XML_DEBUG", "Final - SeasonsJson length: " + seasonsJson.length());
                                Log.d("XML_DEBUG", "Seasons: " + seasonCount + ", Episodes: " + episodeCount);
                            }
                        }
                        currentTag = null;
                    }
                    eventType = parser.next();
                }

                inputStream.close();
                Log.d("XML_DEBUG", "=== FINISHED PARSING XML ===");
                Log.d("XML_DEBUG", "Total movies found: " + movies.size());

                return movies;

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("XML_ERROR", "Error parsing XML: " + e.getMessage());
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