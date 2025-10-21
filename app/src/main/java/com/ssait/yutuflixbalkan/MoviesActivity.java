package com.ssait.yutuflixbalkan;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MoviesActivity extends AppCompatActivity {

    RecyclerView moviesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        moviesRecycler = findViewById(R.id.moviesRecycler);
        moviesRecycler.setLayoutManager(new LinearLayoutManager(this));

        String categoryName = getIntent().getStringExtra("categoryName");
        ArrayList<Movie> movies = getIntent().getParcelableArrayListExtra("moviesList");

        setTitle(categoryName);

        MovieAdapter adapter = new MovieAdapter(this, movies);
        moviesRecycler.setAdapter(adapter);
    }
}

