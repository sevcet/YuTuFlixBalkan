package com.ssait.yutuflixbalkantv.tv;

import android.view.ViewGroup;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import com.bumptech.glide.Glide;
import com.ssait.yutuflixbalkantv.Movie;

public class CardPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView card = new ImageCardView(parent.getContext());
        card.setCardType(ImageCardView.CARD_TYPE_INFO_UNDER);
        card.setMainImageDimensions(313, 176);
        return new ViewHolder(card);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Movie movie = (Movie) item;
        ImageCardView card = (ImageCardView) viewHolder.view;
        card.setTitleText(movie.getTitle());
        Glide.with(viewHolder.view.getContext())
                .load(movie.getImageUrl())
                .centerCrop()
                .into(card.getMainImageView());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {}
}
