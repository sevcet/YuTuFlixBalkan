package com.ssait.yutuflixbalkan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavViewHolder> {

    private Context context;
    private List<RecyclerItem> favorites;

    public FavoritesAdapter(Context context, List<RecyclerItem> favorites) {
        this.context = context;
        this.favorites = favorites;
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        RecyclerItem item = favorites.get(position);

        holder.title.setText(item.getTitle());
        holder.genre.setText(item.getGenre());
        holder.year.setText(item.getYear());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.poster);

        // Klik na poster → otvori detalje (film ili serija)
        holder.itemView.setOnClickListener(v -> {
            if (item.isSeries()) {
                Intent intent = new Intent(context, DetailsActivitySeries.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("year", item.getYear());
                intent.putExtra("genre", item.getGenre());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("imageUrl", item.getImageUrl());
                intent.putExtra("seasonsJson", item.getSeasonsJson());
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, DetailsActivityFilm.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("year", item.getYear());
                intent.putExtra("genre", item.getGenre());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("imageUrl", item.getImageUrl());
                intent.putExtra("videoId", item.getVideoId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, year, genre;

        public FavViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.favoritePoster);
            title = itemView.findViewById(R.id.favoriteTitle);
            year = itemView.findViewById(R.id.favoriteYear);
            genre = itemView.findViewById(R.id.favoriteGenre);
        }
    }
}

