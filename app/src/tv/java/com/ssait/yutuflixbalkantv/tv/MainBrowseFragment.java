package com.ssait.yutuflixbalkantv.tv;

import android.content.Intent;
import android.os.Bundle;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.*;
import com.ssait.yutuflixbalkantv.R;
import com.ssait.yutuflixbalkantv.Movie;
import com.ssait.yutuflixbalkantv.PlayerActivity;
import com.ssait.yutuflixbalkantv.Season;
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
        List<Category> list = new ArrayList<>();
        list.add(new Category("Domaći", fakeMovies("Domaći")));
        list.add(new Category("Strani", fakeMovies("Strani")));

        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        for (Category cat : list) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
            for (Movie m : cat.movies) listRowAdapter.add(m);
            rowsAdapter.add(new ListRow(new HeaderItem(cat.name), listRowAdapter));
        }
        setAdapter(rowsAdapter);
        startEntranceTransition();
    }

    private List<Movie> fakeMovies(String cat) {
        List<Movie> m = new ArrayList<>();
        String thumb = "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg";
        if (cat.equals("Domaći")) {
            m.add(new Movie("Domaći film 1", "2024", "Drama", "film", "Opis 1", thumb, "id1", new ArrayList<Season>(), "https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_1mb.mp4"));
            m.add(new Movie("Domaći film 2", "2024", "Drama", "film", "Opis 2", thumb, "id2", new ArrayList<Season>(), "https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_2mb.mp4"));
        } else {
            m.add(new Movie("Strani film 1", "2023", "Akcija", "film", "Opis 3", thumb, "id3", new ArrayList<Season>(), "https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_1mb.mp4"));
            m.add(new Movie("Strani film 2", "2023", "Akcija", "film", "Opis 4", thumb, "id4", new ArrayList<Season>(), "https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_2mb.mp4"));
        }
        return m;
    }

    private static class Category {
        String name; List<Movie> movies;
        Category(String name, List<Movie> movies) { this.name = name; this.movies = movies; }
    }

    public void onItemViewClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Movie) {
            Movie movie = (Movie) item;
            Intent i = new Intent(getActivity(), PlayerActivity.class);
            i.putExtra("VIDEO_URL", movie.getVideoUrl());
            startActivity(i);
        }
    }
}