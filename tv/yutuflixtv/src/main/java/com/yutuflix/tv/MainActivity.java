package com.yutuflix.tv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private Button btnSearchExecute, btnSearchCancel;

    // BUTTON VARIJABLE - DODAO SAM FAVORITES
    private Button btnHome, btnSearch, btnDomaciFilmovi, btnDomaceSerije, btnAkcija;
    private Button btnKomedija, btnHoror, btnSciFi, btnRomansa, btnFavorites;

    private String xmlUrl = "https://sevcet.github.io/yutuflixapp.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tv);

        initViews();
        setupButtonNavigation();
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
        btnSearchExecute = findViewById(R.id.btnSearchExecute);
        btnSearchCancel = findViewById(R.id.btnSearchCancel);

        // INIT BUTTONS - DODAO FAVORITES
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

    private void setupRecyclerView() {
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categories, new CategoryAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                if ("serija".equalsIgnoreCase(movie.getType()) || "series".equalsIgnoreCase(movie.getType())) {
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
            if (searchContainer.getVisibility() == View.VISIBLE) {
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

        // EXECUTE SEARCH
        btnSearchExecute.setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "Unesite termin za pretragu", Toast.LENGTH_SHORT).show();
            } else {
                performSearch(query);
            }
        });

        // CANCEL SEARCH
        btnSearchCancel.setOnClickListener(v -> {
            hideSearch();
            // Vrati na originalne podatke
            new LoadXmlTask().execute(xmlUrl);
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
        searchContainer.setVisibility(View.VISIBLE);
        // Postavi fokus na search bar
        searchBar.postDelayed(() -> {
            searchBar.requestFocus();
        }, 100);
    }

    private void hideSearch() {
        searchContainer.setVisibility(View.GONE);
        searchBar.setText("");
        // Sakrij soft keyboard ako je otvoren
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
        }
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

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        currentTag = parser.getName();
                        if ("category".equals(currentTag)) {
                            currentCategory = parser.getAttributeValue(null, "name");
                            if (currentCategory == null) currentCategory = "";
                        } else if ("movie".equals(currentTag)) {
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
                            }
                        }
                        currentTag = null;
                    }
                    eventType = parser.next();
                }

                inputStream.close();

                // Ako nema kategorija, napravi jednu opštu kategoriju
                if (categoryList.isEmpty() && !moviesList.isEmpty()) {
                    categoryList.add(new CategoryData("Svi filmovi", moviesList));
                }

                return categoryList;

            } catch (Exception e) {
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
                for (CategoryData category : categories) {
                    allMovies.addAll(category.getMovies());
                }

                categoryAdapter.notifyDataSetChanged();

                Toast.makeText(MainActivity.this,
                        "Učitano " + categories.size() + " kategorija sa " + allMovies.size() + " filmova",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Nema video sadržaja", Toast.LENGTH_LONG).show();
            }
        }
    }
}