package com.yutuflix.tv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;

public class VideoGridAdapter extends BaseAdapter {
    private Context context;
    private List<VideoItem> videoList;
    private LayoutInflater inflater;

    public VideoGridAdapter(Context context, List<VideoItem> videoList) {
        this.context = context;
        this.videoList = videoList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return videoList.size();
    }

    @Override
    public Object getItem(int position) {
        return videoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_video_grid_tv, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.titleText = convertView.findViewById(R.id.titleText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VideoItem video = videoList.get(position);

        // Postavi naslov
        holder.titleText.setText(video.getTitle());

        // Učitaj sliku sa Glide
        if (video.getThumbnail() != null && !video.getThumbnail().isEmpty()) {
            Glide.with(context)
                    .load(video.getThumbnail())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder);
        }

        // Postavi fokus za TV navigaciju
        convertView.setFocusable(true);
        convertView.setFocusableInTouchMode(true);

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView titleText;
    }
}