package com.yutuflix.tv.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import com.yutuflix.tv.R;
import com.yutuflix.tv.ui.home.HomeFragment;
import com.yutuflix.tv.ui.categories.CategoriesFragment;

public class SearchFragment extends Fragment {

    private Button btnHome, btnSearch, btnCategories;
    private EditText etSearch;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_tv, container, false);

        btnHome = view.findViewById(R.id.btnHome);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnCategories = view.findViewById(R.id.btnCategories);
        etSearch = view.findViewById(R.id.etSearch);

        setupNavigation();

        return view;
    }

    private void setupNavigation() {
        btnHome.setOnClickListener(v -> {
            HomeFragment homeFragment = new HomeFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnSearch.setOnClickListener(v -> {
            // Već smo na search
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