package com.touchizen.idolface.ui.idols;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.firebase.database.DatabaseReference;
import com.touchizen.idolface.MainActivity;
import com.touchizen.idolface.R;
import com.touchizen.idolface.model.Idol;
import com.touchizen.idolface.model.IdolProfile;

import org.jetbrains.annotations.NotNull;

public class IdolFiredbAdapter extends FirebaseRecyclerPagingAdapter<IdolProfile, IdolViewHolder> {

    public static final String TAG = IdolFiredbAdapter.class.getSimpleName();
    final Dialog mDialog;
    final SwipeRefreshLayout mSwipeContainer;
    final MainActivity activity;
    final IdolFiredbFragment fragment;
    public IdolFiredbAdapter(DatabasePagingOptions<IdolProfile> options, IdolFiredbFragment fragment, Dialog dialog) {
        super(options);
        this.fragment = fragment;
        this.mDialog = dialog;
        this.mSwipeContainer = fragment.swipeContainer;
        this.activity = (MainActivity) fragment.getActivity();
    }

    @Override
    protected void onBindViewHolder(IdolViewHolder viewHolder, int position, final IdolProfile model) {
        final DatabaseReference idolRef = getRef(position);

        //Log.i(TAG, "=== onBindViewHolder ===" + idolRef.getKey() + ">" + position + "===" + model.getItemType().getTypeCode());

        //if (model.getItemType().getTypeCode() != ItemType.LOAD.getTypeCode()) {
        model.setId(idolRef.getKey());
        viewHolder.bindData(model);
        //}
    }

    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
        boolean isInitial = false;
        switch (state) {
            case LOADING_INITIAL:
                // The initial load has begun
                // ...
                //mDialog.show();
                //isInitial = true;
            case LOADING_MORE:
                // The adapter has started to load an additional page
                // ...
                //progressBar.setVisibility(View.VISIBLE);
                mDialog.show();
                break;

            case LOADED:
                // The previous load (either initial or additional) completed
                // ...
                mDialog.dismiss();
                mSwipeContainer.setRefreshing(false);
                break;
            case FINISHED:
                //Reached end of Data set
                mDialog.dismiss();
                mSwipeContainer.setRefreshing(false);

                break;
            case ERROR:
                // The previous load (either initial or additional) failed. Call
                // the retry() method in order to retry the load operation.
                // ...
                retry();
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NotNull
    @Override
    public IdolViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.item_idol, viewGroup, false);
        return new IdolViewHolder(itemView, createOnClickListener(), activity);
    }

    private IdolViewHolder.OnClickListener createOnClickListener() {
        return new IdolViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view, IdolProfile idol) {
                //fragment.showSplitVideos(lecture);
                Log.d("test",""+idol.getId());
            }

            /*
            @Override
            public void onLikeClick(LikeController likeController, int position) {
                Lesson post = getItem(position);
                likeController.handleLikeClickAction(activity, post);
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callback != null) {
                    callback.onAuthorClick(getItem(position).getAuthorId(), view);
                }
            }
             */
        };
    }
};

