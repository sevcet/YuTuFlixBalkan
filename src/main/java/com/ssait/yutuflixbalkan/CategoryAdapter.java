
package com.ssait.yutuflixbalkan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    private Context context;
    private List<CategoryData> categories;

    public CategoryAdapter(Context context, List<CategoryData> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CategoryData c = categories.get(position);
        holder.categoryTitle.setText(c.getName());

        MovieAdapter movieAdapter = new MovieAdapter(context, c.getItems());
        holder.innerRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.innerRecycler.setAdapter(movieAdapter);
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        RecyclerView innerRecycler;
        public VH(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
            innerRecycler = itemView.findViewById(R.id.movieRecyclerView);
        }
    }
}

