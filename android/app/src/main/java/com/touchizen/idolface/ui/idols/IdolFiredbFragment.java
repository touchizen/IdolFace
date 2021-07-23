package com.touchizen.idolface.ui.idols;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.touchizen.idolface.BuildConfig;
import com.touchizen.idolface.MainActivity;
import com.touchizen.idolface.R;
import com.touchizen.idolface.model.Idol;
import com.touchizen.idolface.model.IdolProfile;
import com.touchizen.idolface.utils.MPreference;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class IdolFiredbFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener
{
    public static final String TAG = IdolFiredbFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final IdolFiredbFragment instance = new IdolFiredbFragment();
    private static MPreference preference = null;

    private Activity mActivity;
    private DatabaseReference mDatabase;
    private IdolFiredbAdapter mAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;

    public SwipeRefreshLayout swipeContainer;

    public static IdolFiredbFragment getInstance() {

        return IdolFiredbFragment.instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPreference();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_idols, container, false);

        initViews(rootView);
        initDatabase();
        //setTitle(getResources().getString(R.string.fragment_idols));

        return rootView;
    }

    private void initPreference() {
        if (preference == null) {
            preference = new MPreference(getContext());
        }
    }

    private void initViews(View rootView) {
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        LinearLayoutManager _manager = new LinearLayoutManager(mActivity);
        //_manager.setReverseLayout(true);
        //_manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(_manager);

        progressBar = rootView.findViewById(R.id.progressBar);
        swipeContainer = rootView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(this);
        mFab = rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preference.isNotLoggedIn()) {
                    Navigation.findNavController(requireActivity(), R.id.fragment_container)
                            .navigate(R.id.action_idols_to_flogin);
                }
                else {
                    Navigation.findNavController(requireActivity(), R.id.fragment_container)
                            .navigate(R.id.action_to_FIdolProfile);
                }
            }
        });

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(this);
        //initPostCounter();
    }

    private void initDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();

        final Dialog mDialog = new Dialog(mActivity, R.style.NewDialog);
        mDialog.addContentView(
                new ProgressBar(mActivity),
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
        mDialog.setCancelable(true);

        // Set up FirebaseRecyclerAdapter with the Query
        Query idolsQuery = getQuery(mDatabase);


        // This configuration comes from the Paging Support Library
        // https://developer.android.com/reference/android/arch/paging/PagedList.Config.html
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();

        // The options for the adapter combine the paging configuration with query information
        // and application-specific options for lifecycle, etc.
        DatabasePagingOptions<IdolProfile> options = new DatabasePagingOptions.Builder<IdolProfile>()
                .setLifecycleOwner(this)
                .setQuery(idolsQuery, config, IdolProfile.class)
                .build();

        mAdapter = new IdolFiredbAdapter(
                options,
                this,
                mDialog
        );
        recyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();

        ((MainActivity)mActivity).setDrawerLockedClosed();
        ((MainActivity)mActivity).getSupportActionBar().hide();
        ((MainActivity)mActivity).setBottomsheetHidden(true);

        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MainActivity)mActivity).setDrawerUnlocked();
        ((MainActivity)mActivity).getSupportActionBar().show();
        ((MainActivity)mActivity).setBottomsheetHidden(false);

        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        /*
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
         */
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    public Query getQuery(DatabaseReference databaseReference) {

        return databaseReference.child("idols");
    }


    @Override
    public void onRefresh() {
        mAdapter.refresh();
    }

    public void onBackButton() {
        ((MainActivity)mActivity).onBackPressed();
    }

    @Override
    public void onClick(View v) {
        //if (v.getId() == android.R.id.home) {
            onBackButton();
        //}
    }
}
