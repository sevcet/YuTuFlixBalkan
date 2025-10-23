package com.yutuflix.tv.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

import com.yutuflix.tv.CategoryActivity;
import com.yutuflix.tv.PlayerActivity;
import com.yutuflix.tv.R;
import com.yutuflix.tv.ui.search.SearchFragment;

public class HomeFragment extends Fragment {

    private Button btnHome, btnSearch, btnDomaciFilmovi, btnDomaceSerije, btnAkcija;
    private Button btnKomedija, btnHoror, btnSciFi, btnRomansa;
    private Button testVideo;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_tv, container, false);

        initViews(view);
        setupNavigation();
        setupCategoryButtons();
        setupTestVideo();

        return view;
    }

    private void initViews(View view) {
        btnHome = view.findViewById(R.id.btnHome);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnDomaciFilmovi = view.findViewById(R.id.btnDomaciFilmovi);
        btnDomaceSerije = view.findViewById(R.id.btnDomaceSerije);
        btnAkcija = view.findViewById(R.id.btnAkcija);
        btnKomedija = view.findViewById(R.id.btnKomedija);
        btnHoror = view.findViewById(R.id.btnHoror);
        btnSciFi = view.findViewById(R.id.btnSciFi);
        btnRomansa = view.findViewById(R.id.btnRomansa);
        testVideo = view.findViewById(R.id.testVideo);
    }

    private void setupNavigation() {
        btnHome.setOnClickListener(v -> {
            // Već smo na home - možda osvežiti sadržaj
        });

        btnSearch.setOnClickListener(v -> {
            SearchFragment searchFragment = new SearchFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupCategoryButtons() {
        // Postavi klik listenere za sve kategorije
        btnDomaciFilmovi.setOnClickListener(v -> openCategory("Domaci Filmovi", "https://sevcet.github.io/exyuflix/domaci_filmovi.xml"));
        btnDomaceSerije.setOnClickListener(v -> openCategory("Domace Serije", "https://sevcet.github.io/exyuflix/domace_serije.xml"));
        btnAkcija.setOnClickListener(v -> openCategory("Akcija", "https://sevcet.github.io/exyuflix/akcija.xml"));
        btnKomedija.setOnClickListener(v -> openCategory("Komedija", "https://sevcet.github.io/exyuflix/komedija.xml"));
        btnHoror.setOnClickListener(v -> openCategory("Horor", "https://sevcet.github.io/exyuflix/horor.xml"));
        btnSciFi.setOnClickListener(v -> openCategory("Sci-Fi", "https://sevcet.github.io/exyuflix/sci_fi.xml"));
        btnRomansa.setOnClickListener(v -> openCategory("Romansa", "https://sevcet.github.io/exyuflix/romansa.xml"));
    }

    private void openCategory(String categoryName, String xmlUrl) {
        Intent intent = new Intent(getActivity(), CategoryActivity.class);
        intent.putExtra("categoryName", categoryName);
        intent.putExtra("xmlUrl", xmlUrl);
        startActivity(intent);
    }

    private void setupTestVideo() {
        testVideo.setOnClickListener(v -> {
            String testVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
            String testTitle = "Test Video";

            Intent intent = new Intent(getActivity(), PlayerActivity.class);
            intent.putExtra("videoUrl", testVideoUrl);
            intent.putExtra("videoTitle", testTitle);
            startActivity(intent);
        });
    }
}