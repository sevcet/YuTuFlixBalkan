package com.yutuflix.tv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import java.util.List;

public class VideoAdapter extends BaseAdapter {
    private Context context;
    private List<VideoItem> videoList;
    private LayoutInflater inflater;

    public VideoAdapter(Context context, List<VideoItem> videoList) {
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
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_video_tv, parent, false);
        }

        Button videoButton = convertView.findViewById(R.id.videoButton);
        VideoItem video = videoList.get(position);

        videoButton.setText(video.getTitle());

        // Postavi fokus za TV navigaciju
        videoButton.setFocusable(true);
        videoButton.setFocusableInTouchMode(true);

        return convertView;
    }
}