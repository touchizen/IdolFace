package com.touchizen.idolface.ui.idolgallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.touchizen.idolface.R;
import com.touchizen.idolface.model.IdolImage;
import com.touchizen.swipe.SwipeLayout;

public class IdolGalleryViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = IdolGalleryViewHolder.class.getSimpleName();
    public static final int MAX_TEXT_LENGTH_IN_LIST = 300; //characters

    public Context context;
    public SwipeLayout swipeLayout;
    public ImageView ivFavorite;
    public ImageView ivIdolProfile;

    private ImageView ivIdolImage;
    private TextView  tvUserName;
    private IdolImage mIdolImage;
    public Activity activity;

    public IdolGalleryViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public IdolGalleryViewHolder(View view, final OnClickListener onClickListener, Activity activity) {
        this(view, onClickListener, activity, true);
    }

    public IdolGalleryViewHolder(
            View view,
            final OnClickListener onClickListener,
            Activity activity,
            boolean isAuthorNeeded
    ) {
        super(view);
        this.context = view.getContext();
        this.activity = activity;

        swipeLayout = view.findViewById(R.id.swipe);
        ivFavorite = view.findViewById(R.id.favorite);
        ivIdolProfile = view.findViewById(R.id.idol_profile);

        ivIdolImage = view.findViewById(R.id.idolImage);
        tvUserName = view.findViewById(R.id.userName);

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onItemClick(getAdapterPosition(), v, mIdolImage);
            }
        });

        /*
        likeViewGroup.setOnClickListener(view1 -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onLikeClick(likeController, position);
            }
        });

        authorImageView.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onAuthorClick(getAdapterPosition(), v);
            }
        });
         */
    }

    @SuppressLint("SetTextI18n")
    public void bind(IdolImage idolImage) {

        this.mIdolImage = idolImage;
        String imageUrl = idolImage.getImageUrl();

        // Displayed and saved to cache image, as needs for post detail.
        Glide.with(activity)
                .load(imageUrl)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_error)
                .into(ivIdolImage);

        //tvUserName.setVisibility(View.GONE);
        tvUserName.setText( idolImage.getUserName() );

    }

    private String removeNewLinesDividers(String text) {
        int decoratedTextLength = Math.min(text.length(), MAX_TEXT_LENGTH_IN_LIST);
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    public interface OnClickListener {
        void onItemClick(int position, View view, IdolImage idol);

        /*
        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);
         */
    }
}
