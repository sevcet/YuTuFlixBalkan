package com.yutuflix.tv.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
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
}