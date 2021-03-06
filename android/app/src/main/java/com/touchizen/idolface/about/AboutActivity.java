package com.touchizen.idolface.about;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;

import com.touchizen.idolface.BuildConfig;
import com.touchizen.idolface.R;
import com.touchizen.idolface.databinding.ActivityAboutBinding;
import com.touchizen.idolface.databinding.FragmentAboutBinding;
import com.touchizen.idolface.utils.NavigationHelper;

import static com.touchizen.idolface.utils.ShareUtils.openUrlInBrowser;

public class AboutActivity extends AppCompatActivity {

    /**
     * List of all software components.
     */
    private static final SoftwareComponent[] SOFTWARE_COMPONENTS = {
//            new SoftwareComponent("ACRA", "2013", "Kevin Gaudin",
//                    "https://github.com/ACRA/acra", StandardLicenses.APACHE2),
            new SoftwareComponent("AndroidX", "2005 - 2011", "The Android Open Source Project",
                    "https://developer.android.com/jetpack", StandardLicenses.APACHE2),
            new SoftwareComponent("Android Image Cropper", "2013 - 2018", "Arthur Teplitzki",
                    "https://github.com/ArthurHub/Android-Image-Cropper", StandardLicenses.APACHE2),
            new SoftwareComponent("AndroidSwipeLayout", "2015-2015", "daimajia",
                    "https://github.com/daimajia/AndroidSwipeLayout", StandardLicenses.MIT),
            new SoftwareComponent("Toggle", "2017-2018", "Angad Singh",
                    "https://github.com/Angads25/android-toggle", StandardLicenses.APACHE2),
            new SoftwareComponent("CircleImageView", "2020-2021", "Lopez Mikhael",
                    "https://github.com/lopspower/CircularImageView", StandardLicenses.APACHE2),
            new SoftwareComponent("EventBus", "2015-2021", "Markus Junginger",
                    "https://github.com/greenrobot/EventBus", StandardLicenses.APACHE2),
            new SoftwareComponent("Firebase", "2017-2021", "Google",
                    "https://github.com/firebase/", StandardLicenses.MIT),
            new SoftwareComponent("Glide", "2018-2021", "Sam Judd",
                    "https://github.com/sjudd/glide", StandardLicenses.MIT),
//            new SoftwareComponent("ExoPlayer", "2014 - 2020", "Google, Inc.",
//                    "https://github.com/google/ExoPlayer", StandardLicenses.APACHE2),
//            new SoftwareComponent("GigaGet", "2014 - 2015", "Peter Cai",
//                    "https://github.com/PaperAirplane-Dev-Team/GigaGet", StandardLicenses.GPL3),
//            new SoftwareComponent("Groupie", "2016", "Lisa Wray",
//                    "https://github.com/lisawray/groupie", StandardLicenses.MIT),
//            new SoftwareComponent("Icepick", "2015", "Frankie Sardo",
//                    "https://github.com/frankiesardo/icepick", StandardLicenses.EPL1),
            new SoftwareComponent("Jsoup", "2009 - 2020", "Jonathan Hedley",
                    "https://github.com/jhy/jsoup", StandardLicenses.MIT),
            new SoftwareComponent("LetsChat", "2020 - 2021", "Gowtham Balamurugan",
                    "https://github.com/a914-gowtham/LetsChat", StandardLicenses.MIT),
//            new SoftwareComponent("Markwon", "2019", "Dimitry Ivanov",
//                    "https://github.com/noties/Markwon", StandardLicenses.APACHE2),
            new SoftwareComponent("Material Components for Android", "2016 - 2020", "Google, Inc.",
                    "https://github.com/material-components/material-components-android", StandardLicenses.APACHE2),
            new SoftwareComponent("NewPipe", "2016 - 2020", "Christian Schabesberger",
                    "https://github.com/TeamNewPipe/NewPipe/", StandardLicenses.GPL3),
//            new SoftwareComponent("NoNonsense-FilePicker", "2016", "Jonas Kalderstam",
//                    "https://github.com/spacecowboy/NoNonsense-FilePicker",StandardLicenses.MPL2),
            new SoftwareComponent("OkHttp", "2019", "Square, Inc.",
                    "https://square.github.io/okhttp/", StandardLicenses.APACHE2),
//            new SoftwareComponent("PrettyTime", "2012 - 2020", "Lincoln Baxter, III",
//                    "https://github.com/ocpsoft/prettytime", StandardLicenses.APACHE2),
            new SoftwareComponent("RxAndroid", "2015", "The RxAndroid authors",
                    "https://github.com/ReactiveX/RxAndroid", StandardLicenses.APACHE2),
            new SoftwareComponent("RxBinding", "2015", "Jake Wharton",
                    "https://github.com/JakeWharton/RxBinding", StandardLicenses.APACHE2),
            new SoftwareComponent("RxJava", "2016 - 2020", "RxJava Contributors",
                    "https://github.com/ReactiveX/RxJava", StandardLicenses.APACHE2),
            new SoftwareComponent("TensorFlow ", "2016 - 2021", "Google Brain Team",
                    "https://github.com/tensorflow", StandardLicenses.APACHE2),
            new SoftwareComponent("Snacky ", "2016 - 2020", "Mate Siede",
                    "https://github.com/matecode/Snacky", StandardLicenses.APACHE2),
//            new SoftwareComponent("Universal Image Loader", "2011 - 2015", "Sergey Tarasevich",
//                    "https://github.com/nostra13/Android-Universal-Image-Loader", StandardLicenses.APACHE2),
    };

    private static final int POS_ABOUT = 0;
    private static final int POS_LICENSE = 1;
    private static final int TOTAL_COUNT = 2;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //assureCorrectAppLanguage(this);
        super.onCreate(savedInstanceState);
        //ThemeHelper.setTheme(this);
        setTitle(getString(R.string.title_activity_about));

        final ActivityAboutBinding aboutBinding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(aboutBinding.getRoot());

        setSupportActionBar(aboutBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        final SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(this);

        // Set up the ViewPager with the sections adapter.
        aboutBinding.container.setAdapter(mSectionsPagerAdapter);

        new TabLayoutMediator(aboutBinding.tabs, aboutBinding.container, (tab, position) -> {
            switch (position) {
                default:
                case POS_ABOUT:
                    tab.setText(R.string.tab_about);
                    break;
                case POS_LICENSE:
                    tab.setText(R.string.tab_licenses);
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavigationHelper.openClassifier(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationHelper.openClassifier(this);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AboutFragment extends Fragment {
        public AboutFragment() {
        }

        /**
         * Created a new instance of this fragment for the given section number.
         *
         * @return New instance of {@link AboutFragment}
         */
        public static AboutFragment newInstance() {
            return new AboutFragment();
        }

        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            final FragmentAboutBinding aboutBinding =
                    FragmentAboutBinding.inflate(inflater, container, false);
            final Context context = getContext();

            aboutBinding.appVersion.setText(BuildConfig.VERSION_NAME);

            aboutBinding.githubLink.setOnClickListener(nv ->
                    openUrlInBrowser(context, context.getString(R.string.github_url), false));

//            aboutBinding.donationLink.setOnClickListener(v ->
//                    openUrlInBrowser(context, context.getString(R.string.donation_url), false));

            aboutBinding.websiteLink.setOnClickListener(nv ->
                    openUrlInBrowser(context, context.getString(R.string.website_url), false));

            aboutBinding.privacyPolicyLink.setOnClickListener(v ->
                    openUrlInBrowser(context, context.getString(R.string.privacy_policy_url),
                            false));

            return aboutBinding.getRoot();
        }
    }

    /**
     * A {@link FragmentStateAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public static class SectionsPagerAdapter extends FragmentStateAdapter {
        public SectionsPagerAdapter(final FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(final int position) {
            switch (position) {
                default:
                case POS_ABOUT:
                    return AboutFragment.newInstance();
                case POS_LICENSE:
                    return LicenseFragment.newInstance(SOFTWARE_COMPONENTS);
            }
        }

        @Override
        public int getItemCount() {
            // Show 2 total pages.
            return TOTAL_COUNT;
        }
    }
}
