package com.yutuflix.tv;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<Movie> movieList;
    private OnMovieClickListener onMovieClickListener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public MovieAdapter(Context context, List<Movie> movieList, OnMovieClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.onMovieClickListener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_tv, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.titleText.setText(movie.getTitle());
        holder.yearText.setText(movie.getYear() != null ? movie.getYear() : "");

        if (movie.getImageUrl() != null && !movie.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(movie.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder);
        }

        // TV FOCUS EFFECT
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    holder.itemView.setBackgroundResource(R.drawable.tv_button_focused);
                    holder.itemView.setScaleX(1.05f);
                    holder.itemView.setScaleY(1.05f);
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.tv_button_background);
                    holder.itemView.setScaleX(1.0f);
                    holder.itemView.setScaleY(1.0f);
                }
            }
        });

        // Klik listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMovieClickListener != null) {
                    onMovieClickListener.onMovieClick(movie);
                }
            }
        });

        // Fokus za TV
        holder.itemView.setFocusable(true);
        holder.itemView.setFocusableInTouchMode(true);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleText;
        TextView yearText;

        public MovieViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.movieImage);
            titleText = itemView.findViewById(R.id.movieTitle);
            yearText = itemView.findViewById(R.id.movieYear);

            // Postavi background za TV fokus
            itemView.setBackgroundResource(R.drawable.tv_button_background);
        }
    }
}