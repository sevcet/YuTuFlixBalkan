package com.youtuflixbalkan.tv;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.*;
import androidx.lifecycle.ViewModelProvider;
import com.youtuflixbalkan.R;
import com.ssait.yutuflixbalkan.Movie;
import com.youtuflixbalkan.ui.player.PlayerActivity;
import com.youtuflixbalkan.ui.home.HomeViewModel;

public class MainBrowseFragment extends BrowseSupportFragment {

    private HomeViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupUI();
        loadRows();
    }

    private void setupUI() {
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        setTitle(getString(R.string.app_name));
    }

    private void loadRows() {
        prepareEntranceTransition();

        viewModel.getCategories().observe(this, list -> {
            ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            for (Category cat : list) {
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
                for (Movie m : cat.movies) listRowAdapter.add(m);
                rowsAdapter.add(new ListRow(new HeaderItem(cat.name), listRowAdapter));
            }
            setAdapter(rowsAdapter);
            startEntranceTransition();
        });
    }

    @Override
    public void onItemViewClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Movie) {
            Movie movie = (Movie) item;
            Intent i = new Intent(getActivity(), PlayerActivity.class);
            i.putExtra("VIDEO_URL", movie.videoUrl);
            startActivity(i);
        }
    }
}