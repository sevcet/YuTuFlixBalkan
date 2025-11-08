package com.ssait.yutuflixbalkan;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

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

        // UČITAJ KATEGORIJU
        loadCategory();

        // POSTAVI OSTALE ELEMENTE
        setupSearchAndButtons();
    }

    // UČITAVANJE KATEGORIJE
    private void loadCategory() {
        showLoading();
        setLoadingText("Učitavam " + categoryName + "...");

        new LoadCategoryTask().execute();
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
    }

    // UCITAJ SLEDECU STRANU
    private void loadNextPage() {
        if (isLoading || !hasMoreData) return;

        isLoading = true;
        showLoading();
        setLoadingText("Učitavam još filmova...");

        new LoadNextPageTask(this).execute();
    }

    // PARSIRANJE FILMA - KOMPLEKSNA OBRADA SEZONA I EPIZODA
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

    // ✅ POPRAVLJENA PRETRAGA
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
                // ✅ PRIKAŽI PRAZNU LISTU UMESTO ORIGINALNE
                movieAdapter = new MovieAdapter(this, new ArrayList<>());
                categoryRecycler.setAdapter(movieAdapter);
            } else {
                movieAdapter = new MovieAdapter(this, searchResults);
                categoryRecycler.setAdapter(movieAdapter);
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
        loadCategory();
    }

    // TASK KLASA ZA UČITAVANJE KATEGORIJE
    private class LoadCategoryTask extends AsyncTask<Void, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            showLoading();
            setLoadingText("Učitavam " + categoryName + "...");
        }

        @Override
        protected List<Movie> doInBackground(Void... voids) {
            List<Movie> moviesList = new ArrayList<>();

            try {
                Document doc = Jsoup.connect(xmlUrl)
                        .timeout(10000) // Smanjen timeout na 10 sekundi
                        .parser(Parser.xmlParser())
                        .get();

                Elements movieEls = doc.select("category > movie, movie");

                for (Element mEl : movieEls) {
                    try {
                        Movie movie = parseMovieFromElement(mEl);
                        if (movie != null) {
                            moviesList.add(movie);
                        }
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

        @Override
        protected void onPostExecute(List<Movie> result) {
            hideLoading();

            if (result != null && !result.isEmpty()) {
                movies.clear();
                movies.addAll(result);
                allMovies.clear();
                allMovies.addAll(result);

                movieAdapter = new MovieAdapter(CategoryActivity.this, movies);
                categoryRecycler.setAdapter(movieAdapter);

                // JEDINO OBAVEŠTENJE: Broj dostupnih video sadržaja
                Toast.makeText(CategoryActivity.this,
                        "Dostupno " + movies.size() + " video sadržaja",
                        Toast.LENGTH_SHORT).show();

            } else {
                // Očisti prikaz ako nema podataka
                movies.clear();
                allMovies.clear();
                movieAdapter.notifyDataSetChanged();
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
        }
    }
}