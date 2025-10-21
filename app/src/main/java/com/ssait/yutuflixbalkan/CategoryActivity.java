package com.ssait.yutuflixbalkan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    RecyclerView categoryRecycler;
    MovieAdapter movieAdapter;
    List<Movie> movies = new ArrayList<>();
    List<Movie> allMovies = new ArrayList<>(); // SVI FILMOVI ZA PAGINATION
    EditText searchBar;
    LottieAnimationView loadingProgress;
    LinearLayout loadingContainer;
    TextView loadingText;

    // PAGINATION VARIJABLE
    private static final int PAGE_SIZE = 15;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    String categoryName;
    String xmlUrl;

    // TRAJNO SKLADIŠTENJE BLOKIRANIH VIDEO ID-JEVA
    private SharedPreferences blockedVideosPrefs;
    private CacheManager cacheManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // INICIJALIZUJ TRAJNO SKLADIŠTE BLOKIRANIH VIDEO ID-JEVA
        initializeBlockedVideos();

        // INICIJALIZUJ CACHE MANAGER
        cacheManager = new CacheManager(this);

        categoryRecycler = findViewById(R.id.categoryRecyclerView);
        searchBar = findViewById(R.id.searchBar);
        loadingContainer = findViewById(R.id.loadingContainer);
        loadingProgress = findViewById(R.id.loadingProgress);
        loadingText = findViewById(R.id.loadingText);

        // PAGINATION SCROLL LISTENER
        categoryRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMoreData && dy > 0) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0) {
                            loadNextPage();
                        }
                    }
                }
            }
        });

        categoryRecycler.setLayoutManager(new GridLayoutManager(this, 2));

        categoryName = getIntent().getStringExtra("categoryName");
        xmlUrl = getIntent().getStringExtra("xmlUrl");

        if (categoryName == null) categoryName = "";
        if (xmlUrl == null) {
            xmlUrl = "https://sevcet.github.io/exyuflix/categories.xml";
        }

        movieAdapter = new MovieAdapter(this, new ArrayList<>());
        categoryRecycler.setAdapter(movieAdapter);

        // INSTANT UČITAVANJE KATEGORIJE
        loadCategoryInstant();

        // POSTAVI OSTALE ELEMENTE
        setupSearchAndButtons();
    }

    // INSTANT UČITAVANJE KATEGORIJE
    private void loadCategoryInstant() {
        showLoading();
        setLoadingText("Učitavam " + categoryName + "...");

        new CategoryInstantLoadTask(this, categoryName, xmlUrl, blockedVideosPrefs, cacheManager).execute();
    }

    // UČITAVANJE KATEGORIJE SA INTERNETA
    private void loadCategoryFromInternet() {
        setLoadingText("Preuzimam " + categoryName + " sa interneta...");
        new CategoryInternetLoadTask(this, categoryName, xmlUrl, blockedVideosPrefs, cacheManager).execute();
    }

    // PROVERA AŽURIRANJA KATEGORIJE U POZADINI
    private void checkCategoryUpdatesInBackground() {
        new CategoryBackgroundUpdateTask(this, categoryName, xmlUrl, blockedVideosPrefs, cacheManager).execute();
    }

    // UCITAJ PRVU STRANU
    private void loadFirstPage() {
        currentPage = 0;
        movies.clear();

        int endIndex = Math.min(PAGE_SIZE, allMovies.size());
        if (endIndex > 0) {
            movies.addAll(allMovies.subList(0, endIndex));
        }

        movieAdapter = new MovieAdapter(this, movies);
        categoryRecycler.setAdapter(movieAdapter);
        hasMoreData = endIndex < allMovies.size();

        Toast.makeText(this, "Učitano " + movies.size() + " filmova", Toast.LENGTH_SHORT).show();
    }

    // UCITAJ SLEDECU STRANU
    private void loadNextPage() {
        if (isLoading || !hasMoreData) return;

        isLoading = true;
        showLoading();
        setLoadingText("Učitavam još filmova...");

        new LoadNextPageTask(this).execute();
    }

    // PARSIRANJE FILMA
    private Movie parseMovieFromElement(Element mEl) {
        try {
            String title = mEl.selectFirst("title") != null ? mEl.selectFirst("title").text() : "";
            String year = mEl.selectFirst("year") != null ? mEl.selectFirst("year").text() : "";
            String genre = mEl.selectFirst("genre") != null ? mEl.selectFirst("genre").text() : "";
            String type = mEl.selectFirst("type") != null ? mEl.selectFirst("type").text() : "film";
            String description = mEl.selectFirst("description") != null ? mEl.selectFirst("description").text() : "";
            String imageUrl = mEl.selectFirst("imageUrl") != null ? mEl.selectFirst("imageUrl").text() : "";
            String videoId = mEl.selectFirst("videoId") != null ? mEl.selectFirst("videoId").text() : "";

            if (title.isEmpty() || videoId.isEmpty()) {
                return null;
            }

            List<Season> seasons = null;
            String seasonsJson = null;

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

                            if (!isVideoPermanentlyBlocked(epVideoId)) {
                                episodes.add(new Episode(epTitle, epImage, epVideoId));
                                JSONObject eObj = new JSONObject();
                                try {
                                    eObj.put("title", epTitle);
                                    eObj.put("imageUrl", epImage);
                                    eObj.put("videoId", epVideoId);
                                    eArray.put(eObj);
                                } catch (Exception ignored) {}
                            }
                        }

                        if (!episodes.isEmpty()) {
                            seasons.add(new Season(seasonNumber, episodes));
                            JSONObject sObj = new JSONObject();
                            try {
                                sObj.put("number", seasonNumber);
                                sObj.put("episodes", eArray);
                                sArray.put(sObj);
                            } catch (Exception ignored) {}
                        }
                    }
                    seasonsJson = sArray.toString();
                }
            }

            return new Movie(title, year, genre, type, description, imageUrl, videoId, seasons, seasonsJson);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    private void initializeBlockedVideos() {
        blockedVideosPrefs = getSharedPreferences("blocked_videos", MODE_PRIVATE);
    }

    private boolean isVideoPermanentlyBlocked(String videoId) {
        return videoId == null || videoId.isEmpty() || blockedVideosPrefs.getBoolean(videoId, false);
    }

    // ✅ POPRAVLJENA PRETRAGA - SADA RADI SA CACHE I INTERNET PODACIMA
    private void setupSearchAndButtons() {
        // PRETRAGA
        findViewById(R.id.searchBtn).setOnClickListener(v -> {
            String query = searchBar.getText().toString().trim().toLowerCase();

            if (query.isEmpty()) {
                // ✅ VRATI SE NA ORIGINALNU LISTU KADA JE PRETRAGA PRAZNA
                loadFirstPage();
                return;
            }

            // ✅ PRETRAŽI U allMovies LISTI KOJA JE VEĆ POPUNJENA
            List<Movie> searchResults = new ArrayList<>();
            for (Movie movie : allMovies) {
                if ((movie.getTitle() != null && movie.getTitle().toLowerCase().contains(query)) ||
                        (movie.getGenre() != null && movie.getGenre().toLowerCase().contains(query)) ||
                        (movie.getYear() != null && movie.getYear().toLowerCase().contains(query))) {
                    searchResults.add(movie);
                }
            }

            // ✅ PRIKAŽI REZULTATE PRETRAGE
            if (searchResults.isEmpty()) {
                Toast.makeText(this, "Nema rezultata za: '" + query + "'", Toast.LENGTH_SHORT).show();
                // ✅ PRIKAŽI PRAZNU LISTU UMESTO ORIGINALNE
                movieAdapter = new MovieAdapter(this, new ArrayList<>());
                categoryRecycler.setAdapter(movieAdapter);
            } else {
                movieAdapter = new MovieAdapter(this, searchResults);
                categoryRecycler.setAdapter(movieAdapter);
                Toast.makeText(this, "Pronađeno " + searchResults.size() + " filmova", Toast.LENGTH_SHORT).show();
            }
        });

        // DUGMAD
        setupCategoryButtons();
    }

    private void setupCategoryButtons() {
        ImageButton btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        Button btnDomaci_filmovi = findViewById(R.id.btnDomaci_filmovi);
        Button btnDomace_serije = findViewById(R.id.btnDomace_serije);
        Button btnAkcija = findViewById(R.id.btnAkcija);
        Button btnKomedija = findViewById(R.id.btnKomedija);
        Button btnHoror = findViewById(R.id.btnHoror);
        Button btnSci_Fi = findViewById(R.id.btnSci_Fi);
        Button btnRomansa = findViewById(R.id.btnRomansa);
        Button btnMisterija = findViewById(R.id.btnMisterija);
        Button btnFavorites = findViewById(R.id.btnFavorites);
        Button btnAbout = findViewById(R.id.btnAbout);
        Button btnPrivacy = findViewById(R.id.btnPrivacy);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnDokumentarni = findViewById(R.id.btnDokumentarni);
        Button btnAnimirani = findViewById(R.id.btnAnimirani);

        // POPRAVLJENI KLIK LISTENERI
        btnDomaci_filmovi.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Domaci filmovi");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/domaci_filmovi.xml");
            startActivity(intent);
            finish();
        });

        btnDomace_serije.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Domace serije");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/domace_serije.xml");
            startActivity(intent);
            finish();
        });

        btnAkcija.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Akcija");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/akcija.xml");
            startActivity(intent);
            finish();
        });

        btnKomedija.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Komedija");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/komedija.xml");
            startActivity(intent);
            finish();
        });

        btnHoror.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Horor");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/horor.xml");
            startActivity(intent);
            finish();
        });

        btnSci_Fi.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Sci-Fi");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/sci_fi.xml");
            startActivity(intent);
            finish();
        });

        btnRomansa.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Romansa");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/romansa.xml");
            startActivity(intent);
            finish();
        });

        btnMisterija.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Misterija");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/misterija.xml");
            startActivity(intent);
            finish();
        });

        btnDokumentarni.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Dokumentarni");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/dokumentarni.xml");
            startActivity(intent);
            finish();
        });

        btnAnimirani.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, CategoryActivity.class);
            intent.putExtra("categoryName", "Animirani");
            intent.putExtra("xmlUrl", "https://sevcet.github.io/exyuflix/animirani.xml");
            startActivity(intent);
            finish();
        });

        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        // O aplikaciji
        btnAbout.setOnClickListener(v -> {
            ScrollView scrollView = new ScrollView(CategoryActivity.this);
            TextView message = new TextView(CategoryActivity.this);
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

            new AlertDialog.Builder(CategoryActivity.this)
                    .setTitle("O aplikaciji")
                    .setView(scrollView)
                    .setPositiveButton("OK", null)
                    .show();
        });

        btnPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, PrivacyActivity.class);
            startActivity(intent);
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Preporučujem ovu aplikaciju");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.ssait.yutuflixbalkan");
            startActivity(Intent.createChooser(shareIntent, "Podeli putem"));
        });
    }

    // DODAJ METODU ZA MANUELNO AŽURIRANJE
    public void onRefreshClick(View view) {
        String cacheKey = "category_" + categoryName.replace(" ", "_").toLowerCase() + "_permanent_v1";
        cacheManager.forceCacheUpdate(cacheKey);
        loadCategoryFromInternet();
        Toast.makeText(this, "Ažuriram podatke...", Toast.LENGTH_SHORT).show();
    }

    // STATIČKE ASYNCTASK KLASE

    private static class CategoryInstantLoadTask extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<CategoryActivity> activityReference;
        private final String categoryName;
        private final String xmlUrl;
        private final SharedPreferences blockedVideosPrefs;
        private final CacheManager cacheManager;

        CategoryInstantLoadTask(CategoryActivity activity, String categoryName, String xmlUrl,
                                SharedPreferences blockedVideosPrefs, CacheManager cacheManager) {
            this.activityReference = new WeakReference<>(activity);
            this.categoryName = categoryName;
            this.xmlUrl = xmlUrl;
            this.blockedVideosPrefs = blockedVideosPrefs;
            this.cacheManager = cacheManager;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            CategoryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;

            try {
                // PRVO POKUŠAJ CACHE
                String cacheKey = "category_" + categoryName.replace(" ", "_").toLowerCase() + "_permanent_v1";
                String cachedXml = cacheManager.readFromCache(cacheKey + "_filtered");

                if (cachedXml != null && !cachedXml.isEmpty()) {
                    Document doc = Jsoup.parse(cachedXml, "", Parser.xmlParser());
                    Elements movieEls = doc.select("category > movie, movie");

                    List<Movie> cachedMovies = new ArrayList<>();
                    for (Element mEl : movieEls) {
                        Movie movie = activity.parseMovieFromElement(mEl);
                        if (movie != null && !activity.isVideoPermanentlyBlocked(movie.getVideoId())) {
                            cachedMovies.add(movie);
                        }
                    }

                    // ✅ POPUNI allMovies LISTU ZA PRETRAGU
                    activity.runOnUiThread(() -> {
                        activity.movies.clear();
                        activity.movies.addAll(cachedMovies);
                        activity.allMovies.clear(); // ✅ OČISTI PRVO
                        activity.allMovies.addAll(cachedMovies); // ✅ POPUNI allMovies
                        activity.movieAdapter = new MovieAdapter(activity, activity.movies);
                        activity.categoryRecycler.setAdapter(activity.movieAdapter);
                        activity.hideLoading();

                        Toast.makeText(activity,
                                "Učitano " + activity.movies.size() + " filmova iz memorije",
                                Toast.LENGTH_SHORT).show();
                    });

                    return true;
                }

                return false;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean hasCache) {
            CategoryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (!hasCache) {
                activity.loadCategoryFromInternet();
            } else {
                // PROVERI AŽURIRANJA U POZADINI
                activity.checkCategoryUpdatesInBackground();
            }
        }
    }

    private static class CategoryInternetLoadTask extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<CategoryActivity> activityReference;
        private final String categoryName;
        private final String xmlUrl;
        private final SharedPreferences blockedVideosPrefs;
        private final CacheManager cacheManager;

        CategoryInternetLoadTask(CategoryActivity activity, String categoryName, String xmlUrl,
                                 SharedPreferences blockedVideosPrefs, CacheManager cacheManager) {
            this.activityReference = new WeakReference<>(activity);
            this.categoryName = categoryName;
            this.xmlUrl = xmlUrl;
            this.blockedVideosPrefs = blockedVideosPrefs;
            this.cacheManager = cacheManager;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            CategoryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;

            try {
                String cacheKey = "category_" + categoryName.replace(" ", "_").toLowerCase() + "_permanent_v1";
                String xmlContent = cacheManager.getFilteredXml(xmlUrl, cacheKey, blockedVideosPrefs);

                if (xmlContent == null || xmlContent.isEmpty()) {
                    return false;
                }

                Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());
                Elements movieEls = doc.select("category > movie, movie");

                List<Movie> newMovies = new ArrayList<>();
                for (Element mEl : movieEls) {
                    Movie movie = activity.parseMovieFromElement(mEl);
                    if (movie != null && !activity.isVideoPermanentlyBlocked(movie.getVideoId())) {
                        newMovies.add(movie);
                    }
                }

                // ✅ POPUNI allMovies LISTU ZA PRETRAGU
                activity.runOnUiThread(() -> {
                    activity.movies.clear();
                    activity.movies.addAll(newMovies);
                    activity.allMovies.clear(); // ✅ OČISTI PRVO
                    activity.allMovies.addAll(newMovies); // ✅ POPUNI allMovies
                    activity.movieAdapter = new MovieAdapter(activity, activity.movies);
                    activity.categoryRecycler.setAdapter(activity.movieAdapter);
                    activity.hideLoading();

                    Toast.makeText(activity,
                            "Učitano " + activity.movies.size() + " filmova sa interneta",
                            Toast.LENGTH_SHORT).show();
                });

                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            CategoryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (!success) {
                activity.hideLoading();
                Toast.makeText(activity, "Greška pri učitavanju", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class CategoryBackgroundUpdateTask extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<CategoryActivity> activityReference;
        private final String categoryName;
        private final String xmlUrl;
        private final SharedPreferences blockedVideosPrefs;
        private final CacheManager cacheManager;

        CategoryBackgroundUpdateTask(CategoryActivity activity, String categoryName, String xmlUrl,
                                     SharedPreferences blockedVideosPrefs, CacheManager cacheManager) {
            this.activityReference = new WeakReference<>(activity);
            this.categoryName = categoryName;
            this.xmlUrl = xmlUrl;
            this.blockedVideosPrefs = blockedVideosPrefs;
            this.cacheManager = cacheManager;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            CategoryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;

            try {
                String cacheKey = "category_" + categoryName.replace(" ", "_").toLowerCase() + "_permanent_v1";

                if (cacheManager.shouldUpdateCache(cacheKey)) {
                    String xmlContent = cacheManager.getFilteredXml(xmlUrl, cacheKey, blockedVideosPrefs);

                    if (xmlContent != null && !xmlContent.isEmpty()) {
                        Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());
                        Elements movieEls = doc.select("category > movie, movie");

                        final int newMovieCount = movieEls.size();

                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity,
                                    "Ažurirani filmovi u pozadini (" + newMovieCount + " filmova)",
                                    Toast.LENGTH_SHORT).show();
                        });

                        return true;
                    }
                }

                return false;

            } catch (Exception e) {
                return false;
            }
        }
    }

    private static class LoadNextPageTask extends AsyncTask<Void, Void, List<Movie>> {
        private final WeakReference<CategoryActivity> activityReference;

        LoadNextPageTask(CategoryActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected List<Movie> doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
                CategoryActivity activity = activityReference.get();
                if (activity != null && !activity.isFinishing()) {
                    return activity.allMovies;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<Movie> allMoviesList) {
            CategoryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.isLoading = false;
            activity.hideLoading();

            if (allMoviesList.isEmpty()) {
                activity.hasMoreData = false;
                return;
            }

            activity.currentPage++;
            int startIndex = activity.currentPage * activity.PAGE_SIZE;

            if (startIndex >= allMoviesList.size()) {
                activity.hasMoreData = false;
                return;
            }

            int endIndex = Math.min(startIndex + activity.PAGE_SIZE, allMoviesList.size());
            List<Movie> newMovies = allMoviesList.subList(startIndex, endIndex);

            int startPosition = activity.movies.size();
            activity.movies.addAll(newMovies);
            activity.movieAdapter.notifyItemRangeInserted(startPosition, newMovies.size());

            activity.hasMoreData = endIndex < allMoviesList.size();

            if (activity.hasMoreData) {
                Toast.makeText(activity,
                        "Dodato " + newMovies.size() + " filmova", Toast.LENGTH_SHORT).show();
            }
        }
    }
}