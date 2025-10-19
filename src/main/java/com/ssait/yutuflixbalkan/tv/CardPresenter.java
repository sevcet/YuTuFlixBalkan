package com.youtuflixbalkan.tv;

import android.view.ViewGroup;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import com.bumptech.glide.Glide;
import com.ssait.yutuflixbalkan.Movie;

public class CardPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView card = new ImageCardView(parent.getContext());
        card.setCardType(ImageCardView.CardType.COVER);
        card.setMainImageDimensions(313, 176); // 16:9
        return new ViewHolder(card);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Movie movie = (Movie) item;
        ImageCardView card = (ImageCardView) viewHolder.view;
        card.setTitleText(movie.title);
        Glide.with(viewHolder.view.getContext())
                .load(movie.thumb)
                .centerCrop()
                .into(card.getMainImageView());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {}
}
