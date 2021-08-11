package com.touchizen.idolface.ui.idols

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.api.ApiFactory
import com.touchizen.idolface.api.PlaceholderApi
import com.touchizen.idolface.databinding.FragmentIdolsBinding
import com.touchizen.idolface.model.IdolProfile
import com.touchizen.idolface.utils.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class IdolsFragment : Fragment() {

    private var instance : IdolsFragment? = null
    private lateinit var binding: FragmentIdolsBinding
    private var preference: MPreference? = null

    private lateinit var mDialog: Dialog
    private lateinit var mAdapter: IdolsAdapter
    private var recyclerView : RecyclerView? = null
    private var fab : FloatingActionButton? = null
    var swipeContainer : SwipeRefreshLayout? = null
    private var mToolbar: Toolbar? = null

    val viewModel: IdolsViewModel by viewModels()

    fun getInstance() : IdolsFragment {
        if (instance == null) {
            instance = IdolsFragment()
        }
        return instance as IdolsFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initPreference()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        binding = FragmentIdolsBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initDialog()
        initFab()
        initSwipe()
        initToobar()

        subscribeObservers()
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

        launchAdapter()
    }

    fun initDialog() {
        mDialog = Dialog(requireActivity(), R.style.NewDialog)
        mDialog.addContentView(
            ProgressBar(requireActivity()),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        mDialog.setCancelable(true)
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
            launchAdapter()
        }
    }

    fun initToobar() {
        mToolbar = binding.toolbar
        mToolbar!!.setNavigationOnClickListener {
            onBackButton()
        }
    }

    private fun subscribeObservers() {
        viewModel.idolsProfileState.observe(viewLifecycleOwner, {
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

    suspend fun setupAdapter(service: PlaceholderApi) {
        val idolRequest = service.getIdols()
        try {
            val response = idolRequest.await()
            if(response.isSuccessful){
                val idols = response.body()
                mAdapter = IdolsAdapter(idols!!,this, mDialog)
                recyclerView!!.adapter = mAdapter
                viewModel.idolsProfileState.value = LoadState.OnSuccess()
            }else{
                Log.d("MainActivity ",response.errorBody().toString())
                viewModel.idolsProfileState.value = LoadState.OnFailure(
                    Exception(response.errorBody().toString())
                )
            }

        }catch (e: Exception){
            viewModel.idolsProfileState.value = LoadState.OnFailure(e)
        }

    }

    @DelicateCoroutinesApi
    fun launchAdapter() {

        viewModel.idolsProfileState.value = LoadState.OnLoading

        val service = ApiFactory.placeholderApi
        GlobalScope.launch(Dispatchers.Main) {
            setupAdapter(service)
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).setDrawerLockedClosed()
        (requireActivity() as MainActivity).supportActionBar!!.hide()
        (requireActivity() as MainActivity).setBottomsheetHidden(true)
        //mAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).setDrawerUnlocked()
        (requireActivity() as MainActivity).supportActionBar!!.show()
        (requireActivity() as MainActivity).setBottomsheetHidden(false)
        //mAdapter.stopListening()
    }

    fun onBackButton() {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
            .navigate(R.id.action_to_camera)
    }

}