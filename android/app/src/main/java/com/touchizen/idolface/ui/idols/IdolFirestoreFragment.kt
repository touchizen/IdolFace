package com.touchizen.idolface.ui.idols

//import com.touchizen.androidxswipelayout.adapter.util.DividerItemDecoration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentIdolsBinding
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.utils.DividerItemDecoration
import com.touchizen.idolface.utils.IdolUtils
import com.touchizen.idolface.utils.MPreference
import com.touchizen.idolface.utils.NavigationHelper
import com.touchizen.swipe.SwipeLayout


class IdolFirestoreFragment : Fragment() {

    private var instance : IdolFirestoreFragment? = null
    private lateinit var binding: FragmentIdolsBinding
    private var preference: MPreference? = null

    private lateinit var mAdapter: FirestorePagingAdapter<IdolProfile, IdolViewHolder>
    private val mFirestore = FirebaseFirestore.getInstance()
    private val mIdolsCollection = mFirestore.collection(IdolUtils.IDOLS_COLLECTION)
    private val mQuery = mIdolsCollection
        .whereEqualTo("isApproved",true)
        .orderBy("pictureCount", Query.Direction.DESCENDING)
    private var recyclerView : RecyclerView? = null
    private var fab : FloatingActionButton? = null
    private var swipeContainer : SwipeRefreshLayout? = null
    private var mToolbar: Toolbar? = null

    fun getInstance() : IdolFirestoreFragment {
        if (instance == null) {
            instance = IdolFirestoreFragment()
        }
        return instance as IdolFirestoreFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initPreference()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        binding = FragmentIdolsBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initFab()
        initSwipe()
        initToobar()

    }

    private fun initPreference() {
        if (preference == null) {
            preference = MPreference(requireContext())
        }
    }
    // Init RecyclerView
    fun initRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        // Item Decorator:

        // Item Decorator:
        recyclerView!!.addItemDecoration(DividerItemDecoration(resources.getDrawable(R.drawable.divider)))
        //recyclerView!!.itemAnimator = FadeInLeftAnimator()

        setupAdapter()
    }

    fun initFab() {
        fab = binding.fab
        fab?.setOnClickListener{
            goToProfile(null)
        }
    }

    fun goToProfile(idol: IdolProfile?) {
        NavigationHelper.openIdolProfile(requireActivity(), idol)
    }

    fun initSwipe() {
        swipeContainer = binding.swipeContainer
        swipeContainer?.setOnRefreshListener {
            mAdapter.refresh()
        }
    }

    fun initToobar() {
        mToolbar = binding.toolbar
        mToolbar!!.setNavigationOnClickListener {
            onBackButton()
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).setDrawerLockedClosed()
        (requireActivity() as MainActivity).supportActionBar!!.hide()
        (requireActivity() as MainActivity).setBottomsheetHidden(true)
        mAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).setDrawerUnlocked()
        (requireActivity() as MainActivity).supportActionBar!!.show()
        (requireActivity() as MainActivity).setBottomsheetHidden(false)
        mAdapter.stopListening()
    }

    fun onBackButton() {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
            .navigate(R.id.action_to_camera)
    }

    private fun setupAdapter() {

        // Init Paging Configuration
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(10)
            .build()

        // Init Adapter Configuration
        val options = FirestorePagingOptions.Builder<IdolProfile>()
            .setLifecycleOwner(this)
            .setQuery(mQuery, config, IdolProfile::class.java)
            .build()

        // Instantiate Paging Adapter
        mAdapter = object : FirestorePagingAdapter<IdolProfile, IdolViewHolder>(options),
            SwipeLayout.SwipeListener
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdolViewHolder {
                val view = layoutInflater.inflate(R.layout.item_idol, parent, false)
                return IdolViewHolder(view, createOnClickListener(), requireActivity())
            }

            override fun onBindViewHolder(viewHolder: IdolViewHolder, position: Int, idol: IdolProfile) {
                // Bind to ViewHolder
                idol.id = getItem(position)?.id!!
                viewHolder.swipeLayout.showMode = SwipeLayout.ShowMode.PullOut
                viewHolder.swipeLayout.addDrag(
                    SwipeLayout.DragEdge.Right,
                    viewHolder.swipeLayout.findViewById<View>(R.id.right_view)
                )

                viewHolder.swipeLayout.getSurfaceView().setOnClickListener {
                    NavigationHelper.openIdolGalllery(requireActivity(),idol)
                }

                viewHolder.swipeLayout.findViewById<View>(R.id.favorite).setOnClickListener{
                }

                viewHolder.swipeLayout.findViewById<View>(R.id.idol_profile).setOnClickListener{
                    goToProfile(idol)
                }

                viewHolder.swipeLayout.addSwipeListener(this)

                viewHolder.bindData(idol)
            }

            override fun onError(e: Exception) {
                super.onError(e)
                e.message?.let { Log.e("MainActivity", it) }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                        swipeContainer!!.isRefreshing = true
                    }

                    LoadingState.LOADING_MORE -> {
                        swipeContainer!!.isRefreshing = true
                    }

                    LoadingState.LOADED -> {
                        swipeContainer!!.isRefreshing = false
                    }

                    LoadingState.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                        swipeContainer!!.isRefreshing = false
                    }

                    LoadingState.FINISHED -> {
                        swipeContainer!!.isRefreshing = false
                    }
                }
            }

            /**
             * SwipeLayout.SwipeListener events.
             */
            fun showDropright(layout: SwipeLayout?) {
                val ivDropRight = layout?.findViewById<ImageView>(R.id.dropright)
                ivDropRight?.setImageResource(R.drawable.ic_arrow_dropright)

            }
            fun showDropleft(layout: SwipeLayout?) {
                val ivDropRight = layout?.findViewById<ImageView>(R.id.dropright)
                ivDropRight?.setImageResource(R.drawable.ic_arrow_dropleft)
            }

            override fun onStartOpen(layout: SwipeLayout?) {
                showDropright(layout)
            }

            override fun onOpen(layout: SwipeLayout?) {
                showDropright(layout)
            }

            override fun onStartClose(layout: SwipeLayout?) {
                showDropleft(layout)
            }

            override fun onClose(layout: SwipeLayout?) {
                showDropleft(layout)
            }

            override fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int) {
            }

            override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) {
            }
        }

        // Finally Set the Adapter to RecyclerView
        recyclerView!!.adapter = mAdapter
    }


    private fun createOnClickListener(): IdolViewHolder.OnClickListener? {
        return IdolViewHolder.OnClickListener { position, view, idol ->
            //goToProfile(idol)
        }
    }
}