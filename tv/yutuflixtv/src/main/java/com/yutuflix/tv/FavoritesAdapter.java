package com.yutuflix.tv;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavViewHolder> {

    private Context context;
    private List<RecyclerItem> favorites;
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(RecyclerItem item);
        void onRemoveFavorite(RecyclerItem item);
    }

    public FavoritesAdapter(Context context, OnFavoriteClickListener listener) {
        this.context = context;
        this.favorites = new ArrayList<>();
        this.listener = listener;
    }

    public void updateFavorites(List<RecyclerItem> newFavorites) {
        this.favorites = newFavorites;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_tv, parent, false);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        RecyclerItem item = favorites.get(position);

        holder.title.setText(item.getTitle());
        holder.genre.setText(item.getGenre());
        holder.year.setText(item.getYear());

        // Prikaži tip (Film/Serija)
        String type = item.isSeries() ? "Serija" : "Film";
        holder.type.setText(type);

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.poster);
        } else {
            holder.poster.setImageResource(R.drawable.placeholder);
        }

        // Klik na item → otvori detalje
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(item);
            }
        });

        // TV focus listener
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.itemView.setBackgroundResource(R.drawable.tv_button_focused);
                holder.itemView.setScaleX(1.02f);
                holder.itemView.setScaleY(1.02f);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.tv_button_background);
                holder.itemView.setScaleX(1.0f);
                holder.itemView.setScaleY(1.0f);
            }
        });

        // Dugme za uklanjanje iz omiljenih
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFavorite(item);
                Toast.makeText(context, "Uklonjeno iz omiljenih", Toast.LENGTH_SHORT).show();
            }
        });

        // TV focus za remove button
        holder.removeButton.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.removeButton.setBackgroundResource(R.drawable.tv_button_focused);
            } else {
                holder.removeButton.setBackgroundResource(R.drawable.tv_button_background);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, year, genre, type;
        TextView removeButton;

        public FavViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.favoritePoster);
            title = itemView.findViewById(R.id.favoriteTitle);
            year = itemView.findViewById(R.id.favoriteYear);
            genre = itemView.findViewById(R.id.favoriteGenre);
            type = itemView.findViewById(R.id.favoriteType);
            removeButton = itemView.findViewById(R.id.removeFavorite);

            // TV optimizacija
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
            removeButton.setFocusable(true);
            removeButton.setFocusableInTouchMode(true);
        }
    }
}