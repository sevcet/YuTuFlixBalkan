package com.ssait.yutuflixbalkan;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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

    // PAGINATION VARIJABLE
    private static final int PAGE_SIZE = 20;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private List<CategoryData> allCategories = new ArrayList<>();

    // TRAJNO SKLADIŠTENJE BLOKIRANIH VIDEO ID-JEVA
    private SharedPreferences blockedVideosPrefs;
    private CacheManager cacheManager;

    // VRATI NA ORIGINALNI XML SA FILMOVIMA
    String xmlUrl = "https://sevcet.github.io/yutuflixapp.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
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
        });

        // Početni prikaz kategorija
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(this, categories);
        categoryRecycler.setAdapter(categoryAdapter);

        // INSTANT UČITAVANJE - PRVO POKUŠAJ CACHE
        loadInstantData();

        // Dugmad kategorija
        setupCategoryButtons();
    }

    // INSTANT UČITAVANJE - PRVO POKUŠAJ CACHE
    private void loadInstantData() {
        showLoading();
        setLoadingText("Učitavam filmove...");

        new InstantLoadTask(this, xmlUrl, blockedVideosPrefs, cacheManager).execute();
    }

    // UČITAVANJE SA INTERNETA
    private void loadXmlFromInternet() {
        setLoadingText("Preuzimam nove filmove...");
        new InternetLoadTask(this, xmlUrl, blockedVideosPrefs, cacheManager).execute();
    }

    // PROVERA AŽURIRANJA U POZADINI
    private void checkForUpdatesInBackground() {
        new BackgroundUpdateTask(this, xmlUrl, blockedVideosPrefs, cacheManager).execute();
    }

    // UCITAJ PRVU STRANU
    private void loadFirstPage() {
        currentPage = 0;
        categories.clear();

        int endIndex = Math.min(PAGE_SIZE, allCategories.size());
        if (endIndex > 0) {
            categories.addAll(allCategories.subList(0, endIndex));
        }

        categoryAdapter.notifyDataSetChanged();
        hasMoreData = endIndex < allCategories.size();

        Toast.makeText(this, "Učitano " + categories.size() + " kategorija", Toast.LENGTH_SHORT).show();
    }

    // UCITAJ SLEDECU STRANU
    private void loadNextPage() {
        if (isLoading || !hasMoreData) return;

        isLoading = true;
        showLoading();
        setLoadingText("Učitavam još kategorija...");

        new LoadNextPageTask(this).execute();
    }

    // PARSIRANJE FILMA IZ ELEMENTA
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
        btnDomaci_filmovi.setOnClickListener(v -> openCategory("Domaci filmovi", "https://sevcet.github.io/exyuflix/domaci_filmovi.xml"));
        btnDomace_serije.setOnClickListener(v -> openCategory("Domace serije", "https://sevcet.github.io/exyuflix/domace_serije.xml"));
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
        String cacheKey = "main_xml_permanent_v1";
        cacheManager.forceCacheUpdate(cacheKey);
        loadXmlFromInternet();
        Toast.makeText(this, "Ažuriram podatke...", Toast.LENGTH_SHORT).show();
    }

    // STATIČKE ASYNCTASK KLASE

    private static class InstantLoadTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<MainActivity> activityReference;
        private String xmlUrl;
        private SharedPreferences blockedVideosPrefs;
        private CacheManager cacheManager;

        InstantLoadTask(MainActivity activity, String xmlUrl, SharedPreferences blockedVideosPrefs, CacheManager cacheManager) {
            this.activityReference = new WeakReference<>(activity);
            this.xmlUrl = xmlUrl;
            this.blockedVideosPrefs = blockedVideosPrefs;
            this.cacheManager = cacheManager;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;

            try {
                // PRVO POKUŠAJ DA UČITAŠ IZ CACHE-A
                String cacheKey = "main_xml_permanent_v1";
                String cachedXml = cacheManager.readFromCache(cacheKey + "_filtered");

                if (cachedXml != null && !cachedXml.isEmpty()) {
                    // IMAMO CACHE - PARSIRAJ GA
                    Document doc = Jsoup.parse(cachedXml, "", Parser.xmlParser());
                    Elements categoryEls = doc.select("movies > category, category");

                    List<CategoryData> cachedCategories = new ArrayList<>();
                    List<Movie> cachedMovies = new ArrayList<>();

                    for (Element catEl : categoryEls) {
                        String catName = catEl.attr("name");
                        Elements movieEls = catEl.select("movie");

                        List<Movie> movies = new ArrayList<>();
                        for (Element mEl : movieEls) {
                            Movie movie = activity.parseMovieFromElement(mEl);
                            if (movie != null && !activity.isVideoPermanentlyBlocked(movie.getVideoId())) {
                                movies.add(movie);
                                cachedMovies.add(movie);
                            }
                        }

                        if (!movies.isEmpty()) {
                            cachedCategories.add(new CategoryData(catName, movies));
                        }
                    }

                    // POSTAVI CACHE PODATKE
                    activity.runOnUiThread(() -> {
                        activity.categories.clear();
                        activity.categories.addAll(cachedCategories);
                        activity.allMovies.clear();
                        activity.allMovies.addAll(cachedMovies);
                        activity.categoryAdapter.notifyDataSetChanged();
                        activity.hideLoading();

                        Toast.makeText(activity,
                                "Učitano " + activity.categories.size() + " kategorija iz memorije",
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
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            // AKO NEMA CACHE-A, PREUZMI SA INTERNETA
            if (!hasCache) {
                activity.loadXmlFromInternet();
            } else {
                // PROVERI AŽURIRANJA U POZADINI
                activity.checkForUpdatesInBackground();
            }
        }
    }

    private static class InternetLoadTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<MainActivity> activityReference;
        private String xmlUrl;
        private SharedPreferences blockedVideosPrefs;
        private CacheManager cacheManager;

        InternetLoadTask(MainActivity activity, String xmlUrl, SharedPreferences blockedVideosPrefs, CacheManager cacheManager) {
            this.activityReference = new WeakReference<>(activity);
            this.xmlUrl = xmlUrl;
            this.blockedVideosPrefs = blockedVideosPrefs;
            this.cacheManager = cacheManager;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;

            try {
                String cacheKey = "main_xml_permanent_v1";
                String xmlContent = cacheManager.getFilteredXml(xmlUrl, cacheKey, blockedVideosPrefs);

                if (xmlContent == null || xmlContent.isEmpty()) {
                    return false;
                }

                Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());
                Elements categoryEls = doc.select("movies > category, category");

                List<CategoryData> newCategories = new ArrayList<>();
                List<Movie> newMovies = new ArrayList<>();

                for (Element catEl : categoryEls) {
                    String catName = catEl.attr("name");
                    Elements movieEls = catEl.select("movie");

                    List<Movie> movies = new ArrayList<>();
                    for (Element mEl : movieEls) {
                        Movie movie = activity.parseMovieFromElement(mEl);
                        if (movie != null && !activity.isVideoPermanentlyBlocked(movie.getVideoId())) {
                            movies.add(movie);
                            newMovies.add(movie);
                        }
                    }

                    if (!movies.isEmpty()) {
                        newCategories.add(new CategoryData(catName, movies));
                    }
                }

                // POSTAVI NOVE PODATKE
                activity.runOnUiThread(() -> {
                    activity.categories.clear();
                    activity.categories.addAll(newCategories);
                    activity.allMovies.clear();
                    activity.allMovies.addAll(newMovies);
                    activity.categoryAdapter.notifyDataSetChanged();
                    activity.hideLoading();

                    Toast.makeText(activity,
                            "Učitano " + activity.categories.size() + " kategorija sa interneta",
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
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (!success) {
                activity.hideLoading();
                Toast.makeText(activity, "Greška pri učitavanju", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class BackgroundUpdateTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<MainActivity> activityReference;
        private String xmlUrl;
        private SharedPreferences blockedVideosPrefs;
        private CacheManager cacheManager;

        BackgroundUpdateTask(MainActivity activity, String xmlUrl, SharedPreferences blockedVideosPrefs, CacheManager cacheManager) {
            this.activityReference = new WeakReference<>(activity);
            this.xmlUrl = xmlUrl;
            this.blockedVideosPrefs = blockedVideosPrefs;
            this.cacheManager = cacheManager;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;

            try {
                String cacheKey = "main_xml_permanent_v1";

                // PROVERI DA LI TREBA AŽURIRATI (8 SATI)
                if (cacheManager.shouldUpdateCache(cacheKey)) {
                    String xmlContent = cacheManager.getFilteredXml(xmlUrl, cacheKey, blockedVideosPrefs);

                    if (xmlContent != null && !xmlContent.isEmpty()) {
                        Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());
                        Elements categoryEls = doc.select("movies > category, category");

                        final int newCategoryCount = categoryEls.size();

                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity,
                                    "Ažurirani podaci u pozadini (" + newCategoryCount + " kategorija)",
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

    private static class LoadNextPageTask extends AsyncTask<Void, Void, List<CategoryData>> {
        private WeakReference<MainActivity> activityReference;

        LoadNextPageTask(MainActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected List<CategoryData> doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
                MainActivity activity = activityReference.get();
                if (activity != null && !activity.isFinishing()) {
                    return activity.allCategories;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<CategoryData> allCategoriesList) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.isLoading = false;
            activity.hideLoading();

            if (allCategoriesList.isEmpty()) {
                activity.hasMoreData = false;
                return;
            }

            activity.currentPage++;
            int startIndex = activity.currentPage * activity.PAGE_SIZE;

            if (startIndex >= allCategoriesList.size()) {
                activity.hasMoreData = false;
                return;
            }

            int endIndex = Math.min(startIndex + activity.PAGE_SIZE, allCategoriesList.size());
            List<CategoryData> newCategories = allCategoriesList.subList(startIndex, endIndex);

            int startPosition = activity.categories.size();
            activity.categories.addAll(newCategories);
            activity.categoryAdapter.notifyItemRangeInserted(startPosition, newCategories.size());

            activity.hasMoreData = endIndex < allCategoriesList.size();
        }
    }
}