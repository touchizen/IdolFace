package com.touchizen.idolface.ui.idols;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.touchizen.idolface.R;
import com.touchizen.idolface.model.Idol;
import com.touchizen.idolface.model.IdolProfile;
import com.touchizen.idolface.utils.ImageUtil;
import com.touchizen.swipe.SwipeLayout;

public class IdolViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = IdolViewHolder.class.getSimpleName();
    public static final int MAX_TEXT_LENGTH_IN_LIST = 300; //characters

    protected Context context;
    public SwipeLayout swipeLayout;
    public ImageView ivFavorite;
    public ImageView ivIdolProfile;

    private ImageView ivIdolImage;
    private TextView  tvIdolName;
    private TextView  tvIdolName2;
    private TextView  tvCompany;
    private TextView  tvImageCount;
    private IdolProfile mIdol;
    private Activity activity;

    public IdolViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public IdolViewHolder(View view, final OnClickListener onClickListener, Activity activity) {
        this(view, onClickListener, activity, true);
    }

    public IdolViewHolder(
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
        tvIdolName = view.findViewById(R.id.idolName);
        tvIdolName2 = view.findViewById(R.id.idolName2);
        tvCompany = view.findViewById(R.id.company);
        tvImageCount = view.findViewById(R.id.imageCount);

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onItemClick(getAdapterPosition(), v, mIdol);
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
    public void bindData(IdolProfile idol) {

        this.mIdol = idol;
        String imageUrl = idol.getImage();
        int width = (int) context.getResources().getDimension(R.dimen.idol_image_width);
        int height = (int) context.getResources().getDimension(R.dimen.idol_image_height);

        // Displayed and saved to cache image, as needs for post detail.
        Glide.with(activity)
             .load(imageUrl)
             .apply(RequestOptions.circleCropTransform())
             .into(ivIdolImage);

        tvIdolName.setVisibility(
             imageUrl == null || imageUrl.isEmpty() ? View.VISIBLE : View.GONE
        );
        tvIdolName.setText( idol.getName() );
        tvIdolName2.setText( idol.getName() );

        String strCompany = "";
        if (idol.getGroupName() != null && !idol.getGroupName().isEmpty()) {
            strCompany = idol.getGroupName();
        }
        if (idol.getJobClass() != null && !idol.getJobClass().isEmpty()) {
            if (strCompany.isEmpty()) {
                strCompany = idol.getJobClass();
            }
            else {
                strCompany += "," + idol.getJobClass();
            }
        }
        if (idol.getCompany() != null && !idol.getCompany().isEmpty()) {
            if (strCompany.isEmpty()) {
                strCompany = idol.getCompany();
            }
            else {
                strCompany += "," + idol.getCompany();
            }
        }

        tvCompany.setText( strCompany );

        String strPhotos = context.getString(R.string.photos,idol.getPictureCount());
        tvImageCount.setText(strPhotos);
    }

    private String removeNewLinesDividers(String text) {
        int decoratedTextLength = Math.min(text.length(), MAX_TEXT_LENGTH_IN_LIST);
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    public interface OnClickListener {
        void onItemClick(int position, View view, IdolProfile idol);

        /*
        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);
         */
    }
}
