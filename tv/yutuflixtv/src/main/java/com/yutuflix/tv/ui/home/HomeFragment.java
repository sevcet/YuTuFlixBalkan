package com.yutuflix.tv.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.yutuflix.tv.PlayerActivity;
import com.yutuflix.tv.R;
import com.yutuflix.tv.ui.search.SearchFragment;
import com.yutuflix.tv.ui.categories.CategoriesFragment;

public class HomeFragment extends Fragment {

    private Button btnHome, btnSearch, btnCategories;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tv, container, false);

        btnHome = view.findViewById(R.id.btnHome);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnCategories = view.findViewById(R.id.btnCategories);

        setupNavigation();
        setupSampleVideo(); // Dodaj sample video za test

        return view;
    }

    private void setupNavigation() {
        btnHome.setOnClickListener(v -> {
            // Već smo na home
        });

        btnSearch.setOnClickListener(v -> {
            SearchFragment searchFragment = new SearchFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnCategories.setOnClickListener(v -> {
            CategoriesFragment categoriesFragment = new CategoriesFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, categoriesFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupSampleVideo() {
        // Pronađi TextView i postavi klik listener
        View view = getView();
        if (view != null) {
            View testVideo = view.findViewById(R.id.testVideo);
            if (testVideo != null) {
                testVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Koristi javni test video URL koji radi na svim uređajima
                        String testVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                        String testTitle = "Test Video";

                        Intent intent = new Intent(getActivity(), PlayerActivity.class);
                        intent.putExtra("videoUrl", testVideoUrl);
                        intent.putExtra("videoTitle", testTitle);
                        startActivity(intent);
                    }
                });

                // Fokus za TV navigaciju
                testVideo.setFocusable(true);
                testVideo.setFocusableInTouchMode(true);
            }
        }
    }
}