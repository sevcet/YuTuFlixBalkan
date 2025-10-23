package com.yutuflix.tv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private Context context;
    private List<CategoryData> categories;
    private OnMovieClickListener onMovieClickListener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public CategoryAdapter(Context context, List<CategoryData> categories, OnMovieClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.onMovieClickListener = listener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_tv, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        CategoryData category = categories.get(position);
        holder.categoryTitle.setText(category.getCategoryName());

        // Horizontalni RecyclerView za filmove u kategoriji
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        holder.moviesRecycler.setLayoutManager(layoutManager);

        MovieAdapter movieAdapter = new MovieAdapter(context, category.getMovies(), new MovieAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                if (onMovieClickListener != null) {
                    onMovieClickListener.onMovieClick(movie);
                }
            }
        });
        holder.moviesRecycler.setAdapter(movieAdapter);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        RecyclerView moviesRecycler;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
            moviesRecycler = itemView.findViewById(R.id.moviesRecycler);
        }
    }
}