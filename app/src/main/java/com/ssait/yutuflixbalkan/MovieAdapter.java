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

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.VH> {

    private Context context;
    private List<Movie> list;

    public MovieAdapter(Context context, List<Movie> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Movie m = list.get(position);
        holder.title.setText(m.getTitle());
        Glide.with(context).load(m.getImageUrl()).placeholder(R.drawable.placeholder).into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if ("film".equalsIgnoreCase(m.getType())) {
                Intent intent = new Intent(context, DetailsActivityFilm.class);
                intent.putExtra("title", m.getTitle());
                intent.putExtra("year", m.getYear());
                intent.putExtra("genre", m.getGenre());
                intent.putExtra("description", m.getDescription());
                intent.putExtra("imageUrl", m.getImageUrl());
                intent.putExtra("videoId", m.getVideoId());
                context.startActivity(intent);
            } else { // serija
                Intent intent = new Intent(context, DetailsActivitySeries.class);
                intent.putExtra("title", m.getTitle());
                intent.putExtra("year", m.getYear());
                intent.putExtra("genre", m.getGenre());
                intent.putExtra("description", m.getDescription());
                intent.putExtra("imageUrl", m.getImageUrl());
                intent.putExtra("seasonsJson", m.getSeasonsJson());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        public VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.movieImage);
            title = itemView.findViewById(R.id.movieTitle);
        }
    }
}