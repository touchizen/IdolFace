package com.touchizen.idolface.ui.idolgallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentIdolGalleryBinding
import com.touchizen.idolface.model.IdolImage
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.ui.CustomProgressView
import com.touchizen.idolface.utils.*
import com.touchizen.swipe.SwipeLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.Exception

@AndroidEntryPoint
class IdolGalleryFragment : Fragment() {

    private var instance : IdolGalleryFragment? = null
    private lateinit var binding: FragmentIdolGalleryBinding
    private var preference: MPreference? = null

    private lateinit var context: Activity

    //private lateinit var mAdapter: FirestorePagingAdapter<IdolImage, IdolGalleryViewHolder>
    private lateinit var mAdapter: IdolGalleryAdapter
    private val mFirestore = FirebaseFirestore.getInstance()
    //private val mIdolGalleryCollection = mFirestore.collection(IdolUtils.IDOLGALLERY_COLLECTION)
    //private lateinit var mQuery : Query
    private var recyclerView : RecyclerView? = null
    private var fab : FloatingActionButton? = null
    private var swipeContainer : SwipeRefreshLayout? = null
    private var mToolbar: Toolbar? = null

    private var progressView: CustomProgressView? = null
    val viewModel: IdolGalleryViewModel by viewModels()

    fun getInstance() : IdolGalleryFragment {
        if (instance == null) {
            instance = IdolGalleryFragment()
        }
        return instance as IdolGalleryFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<IdolProfile>(NavigationHelper.IDOLPROFILE)?.let {
            viewModel.idolProfile.value = it
        }

        initVars()
        initPreference()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        binding = FragmentIdolGalleryBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context = requireActivity()
        binding.lifecycleOwner = viewLifecycleOwner
        progressView = CustomProgressView(context)

        initRecyclerView()
        initFab()
        initSwipe()
        initToobar()

        subscribeObservers()
    }

    private fun subscribeObservers() {

        viewModel.imageUpdateState.observe(viewLifecycleOwner, {
            when (it) {
                is LoadState.OnSuccess -> {
                    if (findNavController().isValidDestination(R.id.idolgallery_fragment)) {
                        progressView?.dismiss()
                        mAdapter.refresh()
                    }
                }
                is LoadState.OnFailure -> {
                    progressView?.dismiss()
                }
                is LoadState.OnLoading -> {
                    progressView?.show()
                }
                else -> {}
            }
        })

        viewModel.checkIdolImageState.observe(viewLifecycleOwner,{
            when (it) {
                is LoadState.OnSuccess -> {
                    progressView?.dismiss()
                }
                is LoadState.OnFailure -> {
                    progressView?.dismiss()
                }
                is LoadState.OnLoading -> {
                    progressView?.show()
                }
                else -> {}
            }
        })

        viewModel.idolGalleryState.observe(viewLifecycleOwner, {
            when (it) {
                is LoadState.OnSuccess -> {
                    swipeContainer!!.isRefreshing = false
                }
                is LoadState.OnFailure -> {
                    swipeContainer!!.isRefreshing = false
                }
                is LoadState.OnLoading -> {
                    swipeContainer!!.isRefreshing = true
                }
                else -> {}
            }
        })
    }


    private fun initVars() {
        viewModel.initVars()
    }

    private fun initPreference() {
        if (preference == null) {
            preference = MPreference(requireContext())
        }
    }
    // Init RecyclerView
    fun initRecyclerView() {

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        recyclerView = binding.recyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = linearLayoutManager

//        recyclerView!!.layoutManager = StaggeredGridLayoutManager(
//            2,
//            StaggeredGridLayoutManager.VERTICAL
//        )

        // Item Decorator:
        setupAdapter()
    }

    fun initFab() {
        fab = binding.fab
        fab?.setOnClickListener{
            uploadImage()
        }
    }

    fun uploadImage() {
        if (preference!!.isNotLoggedIn()) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(R.id.action_to_flogin)
        } else {
            ImageUtils.askPermission(this)
        }
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

        /**
         * Setting title for gallery.
         */
        var secondJob = viewModel.idolProfile.value!!.groupName
        if (secondJob.isNullOrEmpty()) {
            secondJob = viewModel.idolProfile.value!!.jobClass
        }
        binding.toolbarTitle.text = viewModel.idolProfile.value!!.name +
                String.format("(%s)",secondJob)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imagePath: Uri? = ImageUtils.getPhotoUri(data)

        imagePath?.let {
            viewModel.uploadIdolImage( it)
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).setDrawerLockedClosed()
        (requireActivity() as MainActivity).supportActionBar!!.hide()
        (requireActivity() as MainActivity).setBottomsheetHidden(true)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).setDrawerUnlocked()
        (requireActivity() as MainActivity).supportActionBar!!.show()
        (requireActivity() as MainActivity).setBottomsheetHidden(false)
    }

    fun onBackButton() {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
            .navigate(R.id.action_to_idol_fragment)
    }

    private fun setupAdapter() {
        mAdapter = IdolGalleryAdapter(this)
        recyclerView?.adapter = mAdapter

        lifecycleScope.launch {
            viewModel.flow.collectLatest {
                mAdapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            mAdapter.loadStateFlow.collectLatest { loadStates ->

                when(loadStates.refresh) {
                    is androidx.paging.LoadState.Loading ->
                        viewModel.idolGalleryState.value = LoadState.OnLoading
                    is androidx.paging.LoadState.NotLoading ->
                        viewModel.idolGalleryState.value = LoadState.OnSuccess()
                    is androidx.paging.LoadState.Error ->
                        viewModel.idolGalleryState.value = LoadState.OnFailure(Exception("refresh error"))
                }
                when(loadStates.append) {
                    is androidx.paging.LoadState.Loading ->
                        viewModel.idolGalleryState.value = LoadState.OnLoading
                    is androidx.paging.LoadState.NotLoading ->
                        viewModel.idolGalleryState.value = LoadState.OnSuccess()
                    is androidx.paging.LoadState.Error ->
                        viewModel.idolGalleryState.value = LoadState.OnFailure(Exception("append error"))
                }
                when(loadStates.prepend) {
                    is androidx.paging.LoadState.Loading ->
                        viewModel.idolGalleryState.value = LoadState.OnLoading
                    is androidx.paging.LoadState.NotLoading ->
                        viewModel.idolGalleryState.value = LoadState.OnSuccess()
                    is androidx.paging.LoadState.Error ->
                        viewModel.idolGalleryState.value = LoadState.OnFailure(Exception("prepend error"))
                }
            }
        }
    }

    private fun setupAdapterOld() {

        // Init Paging Configuration
//        val config = PagedList.Config.Builder()
//            .setEnablePlaceholders(false)
//            .setPrefetchDistance(2)
//            .setPageSize(10)
//            .build()
//
//        // Init Adapter Configuration
//        val options = FirestorePagingOptions.Builder<IdolImage>()
//            .setLifecycleOwner(this)
//            .setQuery(mQuery, config, IdolImage::class.java)
//            .build()

        // Instantiate Paging Adapter
//        mAdapter = object : FirestorePagingAdapter<IdolImage, IdolGalleryViewHolder>(options),
//            SwipeLayout.SwipeListener
//        {
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdolGalleryViewHolder {
//                val view = layoutInflater.inflate(R.layout.item_idol_gallery, parent, false)
//                return IdolGalleryViewHolder(view, createOnClickListener(), requireActivity())
//            }
//
//            override fun onBindViewHolder(viewHolder: IdolGalleryViewHolder, position: Int, idolImage: IdolImage) {
//                // Bind to ViewHolder
//
//                idolImage.id = getItem(position)?.id!!
//                viewHolder.swipeLayout.showMode = SwipeLayout.ShowMode.PullOut
//                viewHolder.swipeLayout.addDrag(
//                    SwipeLayout.DragEdge.Right,
//                    viewHolder.swipeLayout.findViewById<View>(R.id.right_view)
//                )
//
//                viewHolder.swipeLayout.getSurfaceView().setOnClickListener {
//                    NavigationHelper.openIdolImageProfile(
//                        requireActivity(),
//                        idolImage,
//                        viewModel.idolProfile.value
//                    )
//                }
//
//                viewHolder.swipeLayout.findViewById<View>(R.id.favorite).setOnClickListener{
//                }
//
//                viewHolder.swipeLayout.findViewById<View>(R.id.idol_profile).setOnClickListener{
//                }
//
//                viewHolder.swipeLayout.addSwipeListener(this)
//
//                viewHolder.bind(idolImage)
//            }
//
//            override fun onError(e: Exception) {
//                super.onError(e)
//                e.message?.let { Log.e("IdolGallery===", it) }
//            }
//
//            override fun onLoadingStateChanged(state: LoadingState) {
//                when (state) {
//                    LoadingState.LOADING_INITIAL -> {
//                        swipeContainer!!.isRefreshing = true
//                    }
//
//                    LoadingState.LOADING_MORE -> {
//                        swipeContainer!!.isRefreshing = true
//                    }
//
//                    LoadingState.LOADED -> {
//                        swipeContainer!!.isRefreshing = false
//                    }
//
//                    LoadingState.ERROR -> {
//                        Toast.makeText(
//                            requireContext(),
//                            "Error Occurred!",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        swipeContainer!!.isRefreshing = false
//                    }
//
//                    LoadingState.FINISHED -> {
//                        swipeContainer!!.isRefreshing = false
//                    }
//                }
//            }
//
//            /**
//             * SwipeLayout.SwipeListener events.
//             */
//            fun showDropright(layout: SwipeLayout?) {
//                val ivDropRight = layout?.findViewById<ImageView>(R.id.dropright)
//                ivDropRight?.setImageResource(R.drawable.ic_arrow_dropright)
//
//            }
//            fun showDropleft(layout: SwipeLayout?) {
//                val ivDropRight = layout?.findViewById<ImageView>(R.id.dropright)
//                ivDropRight?.setImageResource(R.drawable.ic_arrow_dropleft)
//            }
//
//            override fun onStartOpen(layout: SwipeLayout?) {
//                showDropright(layout)
//            }
//
//            override fun onOpen(layout: SwipeLayout?) {
//                showDropright(layout)
//            }
//
//            override fun onStartClose(layout: SwipeLayout?) {
//                showDropleft(layout)
//            }
//
//            override fun onClose(layout: SwipeLayout?) {
//                showDropleft(layout)
//            }
//
//            override fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int) {
//            }
//
//            override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) {
//            }
//        }

        // Finally Set the Adapter to RecyclerView
        recyclerView!!.adapter = mAdapter
    }


    private fun createOnClickListener(): IdolGalleryViewHolder.OnClickListener? {
        return IdolGalleryViewHolder.OnClickListener { position, view, idol ->
            //goToProfile(idol)
        }
    }
}