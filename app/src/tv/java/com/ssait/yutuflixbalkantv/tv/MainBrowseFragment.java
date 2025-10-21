package com.ssait.yutuflixbalkantv.tv;

import android.content.Intent;
import android.os.Bundle;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.*;
import com.ssait.yutuflixbalkan.R;
import com.ssait.yutuflixbalkan.Movie;
import com.ssait.yutuflixbalkan.Season;
import com.ssait.yutuflixbalkantv.tv.PlayerActivity;
import java.util.ArrayList;
import java.util.List;

public class MainBrowseFragment extends BrowseSupportFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
        loadRows();
    }

    private void setupUI() {
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(getResources().getColor(R.color.colorPrimary));
        setTitle(getString(R.string.app_name));
    }

    private void loadRows() {
        prepareEntranceTransition();
        new Thread(() -> {
            try {
                // UČITAJ SVE KATEGORIJE (XML-ovi)
                List<Category> list = new ArrayList<>();

                list.add(new Category("Domaći filmovi", loadXml("https://sevcet.github.io/exyuflix/domaci_filmovi.xml")));
                list.add(new Category("Domaće serije", loadXml("https://sevcet.github.io/exyuflix/domace_serije.xml")));
                list.add(new Category("Akcija", loadXml("https://sevcet.github.io/exyuflix/akcija.xml")));
                list.add(new Category("Komedija", loadXml("https://sevcet.github.io/exyuflix/komedija.xml")));
                list.add(new Category("Horor", loadXml("https://sevcet.github.io/exyuflix/horor.xml")));
                list.add(new Category("Sci-Fi", loadXml("https://sevcet.github.io/exyuflix/sci_fi.xml")));
                list.add(new Category("Romansa", loadXml("https://sevcet.github.io/exyuflix/romansa.xml")));
                list.add(new Category("Misterija", loadXml("https://sevcet.github.io/exyuflix/misterija.xml")));
                list.add(new Category("Dokumentarni", loadXml("https://sevcet.github.io/exyuflix/dokumentarni.xml")));
                list.add(new Category("Animirani", loadXml("https://sevcet.github.io/exyuflix/animirani.xml")));

                ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
                for (Category cat : list) {
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
                    // 5 u redu – Leanback automatski stavlja 5-6 na TV ekran
                    for (Movie m : cat.movies) listRowAdapter.add(m);
                    rowsAdapter.add(new ListRow(new HeaderItem(cat.name), listRowAdapter));
                }

                getActivity().runOnUiThread(() -> {
                    setAdapter(rowsAdapter);
                    startEntranceTransition();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /* ---------- XML UČITAVANJE ---------- */
    private List<Movie> loadXml(String url) {
        List<Movie> list = new ArrayList<>();
        try {
            // KORISTI ISTI PARSER KOJI IMAŠ U MOBILNOJ APLIKACIJI
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url).get();
            org.jsoup.select.Elements items = doc.select("item");
            for (org.jsoup.nodes.Element el : items) {
                String title  = el.selectFirst("title")  != null ? el.selectFirst("title").text()  : "";
                String thumb  = el.selectFirst("thumb")  != null ? el.selectFirst("thumb").text()  : "";
                String videoId = el.selectFirst("videoId") != null ? el.selectFirst("videoId").text() : "";
                if (title.isEmpty() || videoId.isEmpty()) continue;

                // KORISTI ISTI KONSTRUKTOR KOJI IMAŠ U MOBILNOJ APLIKACIJI
                list.add(new Movie(title, "", "", "film", "", thumb, videoId, new ArrayList<Season>(), "https://www.youtube.com/watch?v=" + videoId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /* ---------- POMOĆNE KLASE ---------- */
    private static class Category {
        String name;
        List<Movie> movies;
        Category(String name, List<Movie> movies) {
            this.name = name;
            this.movies = movies;
        }
    }

    // KLIK NA FILM – bez @Override
    public void onItemViewClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Movie) {
            Movie movie = (Movie) item;
            Intent i = new Intent(getActivity(), PlayerActivity.class);
            i.putExtra("videoId", movie.getVideoId());
            startActivity(i);
        }
    }
}