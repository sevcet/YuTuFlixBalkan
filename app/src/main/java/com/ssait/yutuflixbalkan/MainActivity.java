package com.ssait.yutuflixbalkan;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    RecyclerView categoryRecycler;
    CategoryAdapter categoryAdapter;
    MovieAdapter movieAdapter;
    List<CategoryData> categories = new ArrayList<>();
    List<Movie> allMovies = new ArrayList<>();
    EditText searchBar;
    LottieAnimationView loadingProgress;
    LinearLayout loadingContainer;
    TextView loadingText;

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

    // Executor za paralelno učitavanje
    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        categoryRecycler = findViewById(R.id.categoryRecyclerView);
        searchBar = findViewById(R.id.searchBar);
        loadingContainer = findViewById(R.id.loadingContainer);
        loadingProgress = findViewById(R.id.loadingProgress);
        loadingText = findViewById(R.id.loadingText);

        // Početni prikaz kategorija
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categories);
        categoryRecycler.setAdapter(categoryAdapter);

        // UČITAJ SVE KATEGORIJE
        loadAllCategories();

        // Dugmad kategorija
        setupCategoryButtons();
    }

    // UČITAVANJE SVIH KATEGORIJA
    private void loadAllCategories() {
        new LoadAllCategoriesTask().execute();
    }

    // LOADING METODE
    private void showLoading() {
        runOnUiThread(() -> {
            if (loadingContainer != null) loadingContainer.setVisibility(View.VISIBLE);
            if (loadingProgress != null) loadingProgress.playAnimation();
            if (categoryRecycler != null) categoryRecycler.setVisibility(RecyclerView.INVISIBLE);
        });
    }

    private void hideLoading() {
        runOnUiThread(() -> {
            if (loadingContainer != null) loadingContainer.setVisibility(View.GONE);
            if (loadingProgress != null) loadingProgress.pauseAnimation();
            if (categoryRecycler != null) categoryRecycler.setVisibility(RecyclerView.VISIBLE);
        });
    }

    private void setLoadingText(String text) {
        runOnUiThread(() -> {
            if (loadingText != null) loadingText.setText(text);
        });
    }

    private void setupCategoryButtons() {
        Button btnDomaci_filmovi = findViewById(R.id.btnDomaci_filmovi);
        Button btnDomace_serije = findViewById(R.id.btnDomace_serije);
        Button btnAkcija = findViewById(R.id.btnAkcija);
        Button btnKomedija = findViewById(R.id.btnKomedija);
        Button btnHoror = findViewById(R.id.btnHoror);
        Button btnSci_Fi = findViewById(R.id.btnSci_Fi);
        Button btnRomansa = findViewById(R.id.btnRomansa);
        Button btnMisterija = findViewById(R.id.btnMisterija);
        Button btnDokumentarni = findViewById(R.id.btnDokumentarni);
        Button btnAnimirani = findViewById(R.id.btnAnimirani);
        Button btnFavorites = findViewById(R.id.btnFavorites);
        Button btnAbout = findViewById(R.id.btnAbout);
        Button btnPrivacy = findViewById(R.id.btnPrivacy);
        Button btnShare = findViewById(R.id.btnShare);

        // Svako dugme sada vodi na svoj XML fajl
        btnDomaci_filmovi.setOnClickListener(v -> openCategory("Domaci Filmovi", "https://sevcet.github.io/exyuflix/domaci_filmovi.xml"));
        btnDomace_serije.setOnClickListener(v -> openCategory("Domace Serije", "https://sevcet.github.io/exyuflix/domace_serije.xml"));
        btnAkcija.setOnClickListener(v -> openCategory("Akcija", "https://sevcet.github.io/exyuflix/akcija.xml"));
        btnKomedija.setOnClickListener(v -> openCategory("Komedija", "https://sevcet.github.io/exyuflix/komedija.xml"));
        btnHoror.setOnClickListener(v -> openCategory("Horor", "https://sevcet.github.io/exyuflix/horor.xml"));
        btnSci_Fi.setOnClickListener(v -> openCategory("Sci-Fi", "https://sevcet.github.io/exyuflix/sci_fi.xml"));
        btnRomansa.setOnClickListener(v -> openCategory("Romansa", "https://sevcet.github.io/exyuflix/romansa.xml"));
        btnMisterija.setOnClickListener(v -> openCategory("Misterija", "https://sevcet.github.io/exyuflix/misterija.xml"));
        btnDokumentarni.setOnClickListener(v -> openCategory("Dokumentarni", "https://sevcet.github.io/exyuflix/dokumentarni.xml"));
        btnAnimirani.setOnClickListener(v -> openCategory("Animirani", "https://sevcet.github.io/exyuflix/animirani.xml"));

        // Favorites
        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        // O aplikaciji
        btnAbout.setOnClickListener(v -> {
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

        // Privacy
        btnPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PrivacyActivity.class);
            startActivity(intent);
        });

        // Share
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Preporučujem ovu aplikaciju");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.ssait.yutuflixbalkan");
            startActivity(Intent.createChooser(shareIntent, "Podeli putem"));
        });

        // Pretraga
        findViewById(R.id.searchBtn).setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim().toLowerCase();

            if (query.isEmpty()) {
                showCategoryView();
                return;
            }

            List<Movie> searchResults = new ArrayList<>();
            for (Movie movie : allMovies) {
                if (movie.getTitle().toLowerCase().contains(query) ||
                        movie.getGenre().toLowerCase().contains(query) ||
                        movie.getYear().toLowerCase().contains(query)) {
                    searchResults.add(movie);
                }
            }

            showSearchResults(searchResults, query);
        });
    }

    private void showCategoryView() {
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categories);
        categoryRecycler.setAdapter(categoryAdapter);
    }

    private void showSearchResults(List<Movie> results, String query) {
        if (results.isEmpty()) {
            List<CategoryData> noResultsCategory = new ArrayList<>();
            noResultsCategory.add(new CategoryData("Nema rezultata za: '" + query + "'", new ArrayList<>()));

            categoryRecycler.setLayoutManager(new LinearLayoutManager(this));
            categoryAdapter = new CategoryAdapter(this, noResultsCategory);
            categoryRecycler.setAdapter(categoryAdapter);
        } else {
            categoryRecycler.setLayoutManager(new GridLayoutManager(this, 2));
            movieAdapter = new MovieAdapter(this, results);
            categoryRecycler.setAdapter(movieAdapter);
        }
    }

    // Otvara kategoriju sa tačnim XML fajlom
    private void openCategory(String categoryName, String categoryUrl) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra("categoryName", categoryName);
        intent.putExtra("xmlUrl", categoryUrl);
        startActivity(intent);
    }

    // DODAJ METODU ZA MANUELNO AŽURIRANJE
    public void onRefreshClick(View view) {
        loadAllCategories();
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
            showLoading();
            setLoadingText("Učitavam kategorije...");

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
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
                e.printStackTrace();
            }

            // Konvertuj mapu u listu očuvavajući redosled
            List<CategoryData> result = new ArrayList<>();
            for (String categoryName : categoryMap.keySet()) {
                CategoryData categoryData = categoryDataMap.get(categoryName);
                if (categoryData != null && !categoryData.getItems().isEmpty()) {
                    result.add(categoryData);
                }
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int current = values[0];
            int total = values[1];
            setLoadingText("Učitavam kategorije... (" + current + "/" + total + ")");
        }

        @Override
        protected void onPostExecute(List<CategoryData> result) {
            hideLoading();

            if (result != null && !result.isEmpty()) {
                categories.clear();
                categories.addAll(result);

                // Popuni allMovies za pretragu
                allMovies.clear();
                for (CategoryData category : categories) {
                    allMovies.addAll(category.getItems());
                }

                categoryAdapter.notifyDataSetChanged();

                int totalMovies = allMovies.size();
                int totalSeries = 0;
                for (Movie movie : allMovies) {
                    if ("serija".equalsIgnoreCase(movie.getType()) || "series".equalsIgnoreCase(movie.getType())) {
                        totalSeries++;
                    }
                }

                // JEDINO OBAVEŠTENJE: Broj dostupnih video sadržaja
                Toast.makeText(MainActivity.this,
                        "Dostupno " + totalMovies + " video sadržaja (" + totalSeries + " serija)",
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(MainActivity.this, "Nema video sadržaja", Toast.LENGTH_LONG).show();

                // Očisti prikaz ako nema podataka
                categories.clear();
                allMovies.clear();
                categoryAdapter.notifyDataSetChanged();
            }
        }

        private List<Movie> loadCategoryFromUrl(String urlString) {
            List<Movie> moviesList = new ArrayList<>();

            try {
                Document doc = Jsoup.connect(urlString)
                        .timeout(10000) // Smanjen timeout na 10 sekundi
                        .parser(Parser.xmlParser())
                        .get();

                Elements movieEls = doc.select("movie");

                for (Element mEl : movieEls) {
                    try {
                        String title = mEl.selectFirst("title") != null ? mEl.selectFirst("title").text() : "";
                        String year = mEl.selectFirst("year") != null ? mEl.selectFirst("year").text() : "";
                        String genre = mEl.selectFirst("genre") != null ? mEl.selectFirst("genre").text() : "";
                        String type = mEl.selectFirst("type") != null ? mEl.selectFirst("type").text() : "film";
                        String description = mEl.selectFirst("description") != null ? mEl.selectFirst("description").text() : "";
                        String imageUrl = mEl.selectFirst("imageUrl") != null ? mEl.selectFirst("imageUrl").text() : "";
                        String videoId = mEl.selectFirst("videoId") != null ? mEl.selectFirst("videoId").text() : "";

                        if (title.isEmpty() || videoId.isEmpty()) {
                            continue;
                        }

                        List<Season> seasons = null;
                        String seasonsJson = null;

                        // KOMPLEKSNA OBRADA SEZONA I EPIZODA ZA SERIJE
                        if ("serija".equalsIgnoreCase(type) || "series".equalsIgnoreCase(type)) {
                            Elements seasonsEls = mEl.select("seasons > season");
                            if (!seasonsEls.isEmpty()) {
                                seasons = new ArrayList<>();
                                JSONArray sArray = new JSONArray();
                                for (Element sEl : seasonsEls) {
                                    int seasonNumber = 1;
                                    try {
                                        seasonNumber = Integer.parseInt(sEl.attr("number"));
                                    } catch (Exception ignored) {}

                                    Elements episodeEls = sEl.select("episode");
                                    List<Episode> episodes = new ArrayList<>();
                                    JSONArray eArray = new JSONArray();

                                    for (Element epEl : episodeEls) {
                                        String epTitle = epEl.selectFirst("title") != null ? epEl.selectFirst("title").text() : "";
                                        String epImage = epEl.selectFirst("imageUrl") != null ? epEl.selectFirst("imageUrl").text() : "";
                                        String epVideoId = epEl.selectFirst("videoId") != null ? epEl.selectFirst("videoId").text() : "";

                                        // DODAJ EPIZODU SAMO AKO IMA VALIDNE PODATKE
                                        if (!epVideoId.isEmpty()) {
                                            episodes.add(new Episode(epTitle, epImage, epVideoId));
                                            JSONObject eObj = new JSONObject();
                                            eObj.put("title", epTitle);
                                            eObj.put("imageUrl", epImage);
                                            eObj.put("videoId", epVideoId);
                                            eArray.put(eObj);
                                        }
                                    }

                                    if (!episodes.isEmpty()) {
                                        seasons.add(new Season(seasonNumber, episodes));
                                        JSONObject sObj = new JSONObject();
                                        sObj.put("number", seasonNumber);
                                        sObj.put("episodes", eArray);
                                        sArray.put(sObj);
                                    }
                                }
                                seasonsJson = sArray.toString();
                            }
                        }

                        Movie movie = new Movie(title, year, genre, type, description, imageUrl, videoId, seasons, seasonsJson);
                        moviesList.add(movie);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return moviesList;

            } catch (Exception e) {
                e.printStackTrace();
                return moviesList;
            }
        }
    }
}