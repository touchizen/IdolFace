/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.touchizen.idolface

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.UiThread
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.FirebaseDatabase
import com.touchizen.idolface.databinding.ActivityMainBinding
import com.touchizen.idolface.model.UserStatus
import com.touchizen.idolface.tflite.Classifier
import com.touchizen.idolface.ui.logout.LogoutFragment
import com.touchizen.idolface.utils.ForceUpdateAsync
import com.touchizen.idolface.utils.MPreference
import com.touchizen.idolface.utils.NavigationHelper
import com.touchizen.idolface.utils.isValidDestination
import java.io.File
import java.lang.String


const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

/**
 * Main entry point into our app. This app follows the single-activity pattern, and all
 * functionality is implemented in the form of fragments.
 */
abstract class MainActivity :
    AppCompatActivity(),
    AdapterView.OnItemSelectedListener,
    View.OnClickListener
{
    private lateinit var container: FrameLayout
    private var toggle: ActionBarDrawerToggle? = null
    private var drawerLayout: DrawerLayout? = null

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController


    private var bottomSheetLayout: LinearLayout? = null
    private var gestureLayout: LinearLayout? = null
    private var sheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    public var isProcessingFrame = false
    public var postInferenceCallback: Runnable? = null

    protected var recognitionTextView: TextView? = null
    protected var recognition1TextView: TextView? = null
    protected var recognition2TextView: TextView? = null
    protected var recognitionValueTextView: TextView? = null
    protected var recognition1ValueTextView: TextView? = null
    protected var recognition2ValueTextView: TextView? = null
    protected var frameValueTextView: TextView? = null
    protected var cropValueTextView: TextView? = null
    protected var cameraResolutionTextView: TextView? = null
    protected var rotationTextView: TextView? = null
    protected var inferenceTimeTextView: TextView? = null
    protected var bottomSheetArrowImageView: ImageView? = null
    private var plusImageView: ImageView? = null
    private  var minusImageView:android.widget.ImageView? = null
    private var threadsTextView: TextView? = null
    private var numThreads = -1

    private var deviceSpinner: Spinner? = null
    private var device: Classifier.Device = Classifier.Device.CPU
    private var genderSpinner: Spinner? = null
    private var gender: Classifier.Gender = Classifier.Gender.TOTAL


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        container = binding.appBarMain.fragmentContainer

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        initPostInference()

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var connected: Boolean = false
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state == NetworkInfo.State.CONNECTED ||
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
        ) {
            //we are connected to a network
            connected = true
        }
        else {
            connected = false
        }

        //ApiUtils.getIdolList();

        try {
            setupDrawer()
            setupBottomSheet()
            setupNavController()

        } catch (e: Exception) {
            Log.d("MainActivity",""+e.toString())
            //ErrorActivity.reportUiErrorInSnackbar(this, "Setting up drawer", e);
        }
    }

    private fun initPostInference() {
        postInferenceCallback = Runnable {
            isProcessingFrame = false
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setupDrawer() {
        val toolbar: Toolbar = binding.appBarMain.toolbar
        setSupportActionBar(toolbar)
        //supportActionBar?.title = "test"

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val tvUserName: TextView =
            navView.getHeaderView(0).findViewById<TextView>(R.id.userName)
        tvUserName.text = String.format(
            "%s v%s(%d)",
            getString(R.string.app_name),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )

        //Settings and About
        navView.getMenu()
            .add(R.id.menu_tabs_group, ITEM_ID_YOUTUBE, ORDER, R.string.channel)
            .setIcon(R.drawable.ic_baseline_subscriptions_green_24);
        navView.getMenu()
            .add(R.id.menu_tabs_group, ITEM_ID_SETTINGS, ORDER, R.string.settings)
            .setIcon(R.drawable.ic_settings_black_24dp);
        navView.getMenu()
            .add(R.id.menu_tabs_group, ITEM_ID_RATE, ORDER, R.string.rate)
            .setIcon(R.drawable.ic_star_black_24dp);
        navView.getMenu()
            .add(R.id.menu_tabs_group, ITEM_ID_SHARE, ORDER, R.string.share)
            .setIcon(R.drawable.ic_share);
        navView.getMenu()
            .add(R.id.menu_tabs_group, ITEM_ID_ABOUT, ORDER, R.string.about)
            .setIcon(R.drawable.ic_info_outline_black_24dp);
        navView.getMenu()
            .add(R.id.menu_tabs_auth, ITEM_ID_MYPROFILE, ORDER, R.string.myprofile)
            .setIcon(R.drawable.ic_person_black_24dp);
        navView.getMenu()
            .add(R.id.menu_tabs_auth, ITEM_ID_SIGNIN, ORDER, R.string.signin)
            .setIcon(R.drawable.ic_login_black_24dp);
        navView.getMenu()
            .add(R.id.menu_tabs_auth, ITEM_ID_SIGNOUT, ORDER, R.string.signout)
            .setIcon(R.drawable.ic_logout_black_24dp);


        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )

        toggle?.syncState()
        drawerLayout?.addDrawerListener(toggle!!)
        drawerLayout?.addDrawerListener(object : SimpleDrawerListener() {
            private val lastService = 0
            override fun onDrawerOpened(drawerView: View) {
                Log.d("Main", "=== onDrawerOpened()")
            }

            override fun onDrawerClosed(drawerView: View) {
                Log.d("Main", "=== onDrawerClosed()")
            }
        })

        navView.setNavigationItemSelectedListener(
            NavigationView.OnNavigationItemSelectedListener { item: MenuItem? ->
                this.drawerItemSelected(item!!)
        })

        //forceUpdateDialog()
    }

    private fun drawerItemSelected(item: MenuItem): Boolean {
        val gId = item.groupId
        when (item.groupId) {
            R.id.menu_tabs_group -> try {
                tabSelected(item)
            } catch (e: Exception) {
                //ErrorActivity.reportUiErrorInSnackbar(this, "Selecting main page tab", e);
            }
            R.id.menu_tabs_auth -> try {
                authSelected(item)
            } catch (e: Exception) {
                //ErrorActivity.reportUiErrorInSnackbar(this, "Selecting main page tab", e);
            }
            else -> defaultSelected(item)
        }
        drawerLayout?.closeDrawers()
        return true
    }

    private fun defaultSelected(item: MenuItem) {
        when (item.itemId) {
            //R.id.nav_home -> showCameraFragment()
            R.id.nav_gallery -> NavigationHelper.openAlbum(this)
            R.id.nav_idols -> NavigationHelper.openIdolFragment(this)
            else -> {
            }
        }
    }

    private fun tabSelected(item: MenuItem) {
        when(item.itemId) {
            ITEM_ID_YOUTUBE -> openYoutubeChannel()
            ITEM_ID_RATE -> openRate()
            ITEM_ID_SHARE -> openShare()
            ITEM_ID_SETTINGS -> openSettings()
            ITEM_ID_ABOUT -> openAbout()
            else -> {
            }
        }
    }

    private fun authSelected(item: MenuItem) {
        when(item.itemId) {
            ITEM_ID_MYPROFILE -> openMyProfile()
            ITEM_ID_SIGNIN -> signin()
            ITEM_ID_SIGNOUT -> signout()
            else -> {
            }
        }
    }

    private fun setupNavController() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment?
        navController = navHostFragment!!.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            onDestinationChanged(destination.id)
        }
    }

    private fun onDestinationChanged(currentDestination: Int) {
//        try {
//            when(currentDestination) {
//                R.id.FSearch -> {
//                    binding.bottomNav.selectedItemId = R.id.nav_search
//                    showView()
//                    binding.fab.hide()
//                }
//                R.id.FMyProfile -> {
//                    binding.bottomNav.selectedItemId = R.id.nav_profile
//                    showView()
//                    binding.fab.hide()
//                }
//                else -> {
//                    binding.bottomNav.gone()
//                    binding.fab.gone()
//                    binding.toolbar.gone()
//                }
//            }
//            Handler(Looper.getMainLooper()).postDelayed({ //delay time for searchview
//                if (this::searchItem.isInitialized) {
//                    if (currentDestination == R.id.FMyProfile) {
//                        searchItem.collapseActionView()
//                        searchItem.isVisible = false
//                    }else
//                        searchItem.isVisible = true
//                }
//            }, 500)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    private fun openMyProfile() {
        NavigationHelper.openMyProfile(this)
    }

    private fun signin() {
        NavigationHelper.signIn(this)
    }

    private fun signout() {
        NavigationHelper.signOut(this)
    }

    fun onLoginFinished() {
        onResume()
    }

    fun onLogoutFinished() {
        onResume()
        if (findNavController(R.id.fragment_container).isValidDestination(R.id.myprofile_fragment)) {
            NavigationHelper.navigateUp(this)
        }
    }

    private fun openYoutubeChannel() {
        NavigationHelper.openYoutube(this)
    }

    private fun openRate() {
        NavigationHelper.openRate(this)
    }

    private fun openShare() {
        NavigationHelper.openShare(this)
    }

    private fun openSettings() {
        NavigationHelper.openSettings(this)
    }

    private fun openAbout() {
        NavigationHelper.openAbout(this)
    }

    private fun showCameraFragment() {
        NavigationHelper.gotoCameraFragment(
            supportFragmentManager,
            getOutputDirectory(this).absolutePath
        )
    }

    public fun hideSystemUI() {
        // Prevent jumping of the player on devices with cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        var visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        // In multiWindow mode status bar is not transparent for devices with cutout
        // if I include this flag. So without it is better in this case
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode()) {
            visibility = visibility or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        window.decorView.systemUiVisibility = visibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    fun resolveColorFromAttr(context: Context, @AttrRes attrColor: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attrColor, value, true)
        return if (value.resourceId != 0) {
            ContextCompat.getColor(context, value.resourceId)
        } else value.data
    }
    fun showSystemUi() {
        // Prevent jumping of the player on devices with cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }

        window.decorView.systemUiVisibility = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resolveColorFromAttr(this, android.R.attr.colorPrimary)
        }

    }

    public fun setDrawerLockedClosed() {
        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    public fun setDrawerUnlocked() {
        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private fun setupBottomSheet() {
        threadsTextView = binding.appBarMain.bottomSheetLayout.threads
        plusImageView = binding.appBarMain.bottomSheetLayout.plus
        minusImageView = binding.appBarMain.bottomSheetLayout.minus
        deviceSpinner = binding.appBarMain.bottomSheetLayout.deviceSpinner
        genderSpinner = binding.appBarMain.bottomSheetLayout.genderSpinner
        bottomSheetLayout = binding.appBarMain.bottomSheetLayout.bottomSheetLayout
        gestureLayout = binding.appBarMain.bottomSheetLayout.gestureLayout
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout!!)
        bottomSheetArrowImageView = binding.appBarMain.bottomSheetLayout.bottomSheetArrow

        val vto = gestureLayout!!.viewTreeObserver
        vto.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        gestureLayout!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    } else {
                        gestureLayout!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                    //                int width = bottomSheetLayout.getMeasuredWidth();
                    val height = gestureLayout!!.measuredHeight
                    sheetBehavior!!.peekHeight = height
                }
            })
        sheetBehavior!!.isHideable = false

        sheetBehavior!!.setBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            bottomSheetArrowImageView!!.setImageResource(R.drawable.icn_chevron_down)
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            bottomSheetArrowImageView!!.setImageResource(R.drawable.icn_chevron_up)
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                        }
                        BottomSheetBehavior.STATE_SETTLING -> bottomSheetArrowImageView!!.setImageResource(
                            R.drawable.icn_chevron_up
                        )
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

        recognitionTextView = binding.appBarMain.bottomSheetLayout.detectedItem
        recognitionValueTextView = binding.appBarMain.bottomSheetLayout.detectedItemValue
        recognition1TextView = binding.appBarMain.bottomSheetLayout.detectedItem1
        recognition1ValueTextView = binding.appBarMain.bottomSheetLayout.detectedItem1Value
        recognition2TextView = binding.appBarMain.bottomSheetLayout.detectedItem2
        recognition2ValueTextView = binding.appBarMain.bottomSheetLayout.detectedItem2Value

        frameValueTextView = binding.appBarMain.bottomSheetLayout.frameInfo
        cropValueTextView = binding.appBarMain.bottomSheetLayout.cropInfo
        cameraResolutionTextView = binding.appBarMain.bottomSheetLayout.viewInfo
        rotationTextView = binding.appBarMain.bottomSheetLayout.rotationInfo
        inferenceTimeTextView = binding.appBarMain.bottomSheetLayout.inferenceInfo


        // create spinneritemlist for spinner
        val deviceAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this,
            R.array.tfe_ic_devices,
            R.layout.spinner_main_layout
        )
        deviceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)

        val genderAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this,
            R.array.tfe_ic_genders,
            R.layout.spinner_main_layout
        )
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)

        deviceSpinner?.adapter = deviceAdapter
        genderSpinner?.adapter = genderAdapter
        deviceSpinner?.setOnItemSelectedListener(this)
        genderSpinner?.setOnItemSelectedListener(this)

        plusImageView?.setOnClickListener(this)
        minusImageView?.setOnClickListener(this)

        device = Classifier.Device.valueOf(deviceSpinner?.selectedItem.toString())
        numThreads = threadsTextView?.getText().toString().trim { it <= ' ' }.toInt()

        gender = Classifier.Gender.findBy(genderSpinner?.selectedItemId as Int)
    }

    public fun setBottomsheetHidden(isHidden : Boolean) {
        if (isHidden) {
            sheetBehavior!!.peekHeight = 0
        }
        else {
            sheetBehavior!!.peekHeight = gestureLayout!!.measuredHeight
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        //showSystemUi()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_settings -> {
                openSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // Before setting full screen flags, we must wait a bit to let UI settle; otherwise, we may
        // be trying to set app to immersive mode before it's ready and the flags do not stick

        // Make full screen........... commented by Gordon Ahn.
//        container.postDelayed({
//            container.systemUiVisibility = FLAGS_FULLSCREEN
//        }, IMMERSIVE_FLAG_TIMEOUT)

        val navView: NavigationView = binding.navView

        val preference = MPreference(baseContext)
        if (preference.isNotLoggedIn()) {
            navView.getMenu().findItem(ITEM_ID_MYPROFILE).setVisible(false);
            navView.getMenu().findItem(ITEM_ID_SIGNIN).setVisible(true);
            navView.getMenu().findItem(ITEM_ID_SIGNOUT).setVisible(false);
        }
        else {
            navView.getMenu().findItem(ITEM_ID_MYPROFILE).setVisible(true);
            navView.getMenu().findItem(ITEM_ID_SIGNIN).setVisible(false);
            navView.getMenu().findItem(ITEM_ID_SIGNOUT).setVisible(true);
        }
    }

    /** When key down event is triggered, relay it via local broadcast so fragments can handle it */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    abstract fun processImage(
        rgbFrameBitmap : Bitmap,
        previewWidth: Int,
        previewHeight: Int,
        rotation: Int,
        listener: ClassifierActivity.OnClassifierListener
    )
    protected abstract fun onInferenceConfigurationChanged()

    protected open fun readyForNextImage() {
        postInferenceCallback?.run()
    }

    protected open fun getScreenOrientation(): Int {
        return when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }


    @UiThread
    public open fun showResultsInBottomSheet(results: List<Classifier.Recognition?>?) {
        if (results != null && results.size >= 3) {
            val recognition: Classifier.Recognition? = results[0]
            if (recognition != null) {
                if (recognition.getTitle() != null) recognitionTextView?.setText(recognition.getTitle())
                if (recognition.getConfidence() != null) recognitionValueTextView!!.text =
                    String.format("%.2f", 100 * recognition.getConfidence()).toString() + "%"

                //EventBus.getDefault().post(recognition);
            }
            val recognition1: Classifier.Recognition? = results[1]
            if (recognition1 != null) {
                if (recognition1.getTitle() != null) recognition1TextView?.setText(recognition1.getTitle())
                if (recognition1.getConfidence() != null) recognition1ValueTextView!!.text =
                    String.format("%.2f", 100 * recognition1.getConfidence()).toString() + "%"
            }
            val recognition2: Classifier.Recognition? = results[2]
            if (recognition2 != null) {
                if (recognition2.getTitle() != null) recognition2TextView?.setText(recognition2.getTitle())
                if (recognition2.getConfidence() != null) recognition2ValueTextView!!.text =
                    String.format("%.2f", 100 * recognition2.getConfidence()).toString() + "%"
            }
        }
    }

    protected fun showFrameInfo(frameInfo: kotlin.String?) {
        frameValueTextView!!.text = frameInfo
    }

    protected fun showCropInfo(cropInfo: kotlin.String?) {
        cropValueTextView!!.text = cropInfo
    }

    protected fun showCameraResolution(cameraInfo: kotlin.String?) {
        cameraResolutionTextView!!.text = cameraInfo
    }

    protected fun showRotationInfo(rotation: kotlin.String?) {
        rotationTextView!!.text = rotation
    }

    public fun showInference(inferenceTime: kotlin.String?) {
        inferenceTimeTextView!!.text = inferenceTime
    }

    protected fun getDevice(): Classifier.Device? {
        return device
    }

    private fun setDevice(device: Classifier.Device) {
        if (this.device !== device) {
            //org.tensorflow.lite.examples.classification.CameraActivity.LOGGER.d("Updating  device: $device")
            this.device = device
            val threadsEnabled = device === Classifier.Device.CPU
            plusImageView!!.isEnabled = threadsEnabled
            minusImageView!!.isEnabled = threadsEnabled
            threadsTextView!!.text = if (threadsEnabled) numThreads.toString() else "N/A"

            onInferenceConfigurationChanged()
        }
    }


    protected fun getGender(): Classifier.Gender {
        return gender
    }

    private fun setGender(gender: Classifier.Gender) {
        if (this.gender !== gender) {
            this.gender = gender

            onInferenceConfigurationChanged()
        }
    }

    protected fun getNumThreads(): Int {
        return numThreads
    }

    private fun setNumThreads(numThreads: Int) {
        if (this.numThreads != numThreads) {
            //org.tensorflow.lite.examples.classification.CameraActivity.LOGGER.d("Updating  numThreads: $numThreads")
            this.numThreads = numThreads

            onInferenceConfigurationChanged()
        }
    }

    /**
     *  Event methods for spinner
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent === deviceSpinner) {
            setDevice(Classifier.Device.valueOf(parent!!.getItemAtPosition(position).toString()))
        }
        else if (parent === genderSpinner) {
            setGender(Classifier.Gender.findBy(position))
        }
    }

    override fun onBackPressed() {
        if (!::navController.isInitialized) {
            setupNavController()
        }
        if (navController.isValidDestination(R.id.camera_fragment)) {
            finish()
        }
//        else if (navController.isValidDestination(R.id.F) ||
//            navController.isValidDestination(R.id.FSearch)) {
//            val navOptions = NavOptions.Builder()
//                            .setPopUpTo(R.id.camera_fragment, true).build()
//            Navigation.findNavController(this, R.id.camera_fragment)
//                .navigate(R.id.camera_fragment, null, navOptions)
//        }
        else
            super.onBackPressed()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.plus -> {
                val threads = threadsTextView!!.text.toString().trim { it <= ' ' }
                var numThreads = threads.toInt()
                if (numThreads >= 9) return
                setNumThreads(++numThreads)
                threadsTextView!!.text = numThreads.toString()

            }
            R.id.minus -> {
                val threads = threadsTextView!!.text.toString().trim { it <= ' ' }
                var numThreads = threads.toInt()
                if (numThreads == 1) {
                    return
                }
                setNumThreads(--numThreads)
                threadsTextView!!.text = numThreads.toString()
            }
            else -> {

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                return
            }

            val selectedImageUri: Uri = data.data!!
//            val inputStream = getContentResolver().openInputStream(selectedImageUri)
//            var bitmap : Bitmap = BitmapFactory.decodeStream(inputStream)

            val selectedImagePath: kotlin.String? = getPathFromUri(data.data)
            NavigationHelper.openWideGalleryFragment(
                supportFragmentManager,
                selectedImageUri
            )
        }
        else {
            /**
             *  To receive onActivityResult on Every Fragments.
             */
            val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
            if (navHostFragment != null) {
                val childFragments = navHostFragment.childFragmentManager.fragments
                childFragments.forEach { fragment ->
                    fragment.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    fun getPathFromUri(uri: Uri?): kotlin.String? {
        val cursor: Cursor? = uri?.let {
            getContentResolver().query(it, null, null, null, null)
        }
        cursor?.moveToNext()
        val path = cursor?.getString(cursor.getColumnIndex("_data"))
        cursor?.close()
        return path
    }

    fun forceUpdateDialog() {
        //if (!App.isForceUpdate()) return
        val packageManager: PackageManager = getPackageManager()
        val packageInfo: PackageInfo
        try {
            packageInfo = packageManager.getPackageInfo(getPackageName(), 0)
            val currentVersion = packageInfo.versionName
            ForceUpdateAsync(currentVersion, this).execute()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("ERR", e.toString())
        }
    }

    companion object {

        public const val PICK_IMAGE = 1111
        private const val ITEM_ID_YOUTUBE = 0
        private const val ITEM_ID_RATE = 1
        private const val ITEM_ID_SHARE = 2
        private const val ITEM_ID_SETTINGS = 3
        private const val ITEM_ID_ABOUT = 4
        private const val ITEM_ID_MYPROFILE = 5
        private const val ITEM_ID_SIGNIN = 6
        private const val ITEM_ID_SIGNOUT = 7

        public const val YOUTUBE_CHANNEL = "https://www.youtube.com/channel/UCHTNaLtro_1I6Y3SSywo3Cg"

        private const val ORDER = 0

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }
}
