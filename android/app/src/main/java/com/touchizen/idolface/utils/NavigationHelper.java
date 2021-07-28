package com.touchizen.idolface.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.touchizen.idolface.ClassifierActivity;
import com.touchizen.idolface.MainActivity;
import com.touchizen.idolface.R;
import com.touchizen.idolface.about.AboutActivity;
import com.touchizen.idolface.databinding.AlertLogoutBinding;
import com.touchizen.idolface.model.IdolImage;
import com.touchizen.idolface.model.IdolProfile;
import com.touchizen.idolface.model.UserStatus;
import com.touchizen.idolface.preference.SettingsActivity;
import com.touchizen.idolface.ui.CameraFragmentDirections;
import com.touchizen.idolface.ui.WideGalleryFragment;
import com.touchizen.idolface.ui.idols.IdolFirestoreFragment;
import com.touchizen.idolface.ui.logout.LogoutFragment;

import org.greenrobot.eventbus.EventBus;

public class NavigationHelper {

	public static final String CAMERA_FRAGMENT_TAG = "camera_fragment_tag";
	public static final String GALLERY_FRAGMENT_TAG = "gallery_fragment_tag";
	public static final String SLIDESHOW_FRAGMENT_TAG = "slideshow_fragment_tag";

	public static final String IDOLPROFILE = "idolProfile";
	public static final String MYIDOLPROFILE = "myidolProfile";
	public static final String IDOLGALLERY = "idolGallery";
	public static final String IDOLIMAGE = "idolImage";
	public static final String CAMERA = "camera";

    /*//////////////////////////////////////////////////////////////////////////
    // Through FragmentManager
    //////////////////////////////////////////////////////////////////////////*/

	@SuppressLint("CommitTransaction")
	private static FragmentTransaction defaultTransaction(final FragmentManager fragmentManager) {
		return fragmentManager.beginTransaction()
				.setCustomAnimations(R.animator.custom_fade_in, R.animator.custom_fade_out,
						R.animator.custom_fade_in, R.animator.custom_fade_out);
	}

	public static void gotoCameraFragment(final FragmentManager fragmentManager, String outputDirectory) {
		//ImageLoader.getInstance().clearMemoryCache();
//
//		final boolean popped = fragmentManager.popBackStackImmediate(CAMERA_FRAGMENT_TAG, 0);
//		if (!popped) {
//			openHomeFragment(fragmentManager, outputDirectory);
//		}
	}

	public static void openHomeFragment(final FragmentManager fragmentManager, String outputDirectory) {
		//InfoCache.getInstance().trimCache();

		Bundle args = new Bundle();
		WideGalleryFragment galleryFragment = new WideGalleryFragment();

		args.putString("root_directory", outputDirectory);
		galleryFragment.setArguments(args);
		fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		defaultTransaction(fragmentManager)
				.replace(R.id.fragment_container, galleryFragment)
				.addToBackStack(CAMERA_FRAGMENT_TAG)
				.commit();
	}

	public static void gotoGalleryFragment(final FragmentManager fragmentManager) {
		//ImageLoader.getInstance().clearMemoryCache();

		final boolean popped = fragmentManager.popBackStackImmediate(GALLERY_FRAGMENT_TAG, 0);
		if (!popped) {
			openWideGalleryFragment(fragmentManager);
		}
	}

	public static void openWideGalleryFragment(
			final FragmentManager fragmentManager,
			final Uri oneImageUri
	){
		//InfoCache.getInstance().trimCache();
		Bundle args = new Bundle();
		WideGalleryFragment galleryFragment = new WideGalleryFragment();

		args.putString("root_directory", "");
		args.putParcelable("image_uri", oneImageUri);
		galleryFragment.setArguments(args);

		defaultTransaction(fragmentManager)
				.replace(R.id.fragment_container, galleryFragment)
				.addToBackStack(null)
				.commit();
	}

	public static void openWideGalleryFragment(final FragmentManager fragmentManager) {
		//InfoCache.getInstance().trimCache();

		defaultTransaction(fragmentManager)
				.replace(R.id.fragment_container, new WideGalleryFragment())
				.addToBackStack(null)
				.commit();
	}


	public static void openAlbum(final MainActivity activity) {
		FragmentManager fm = activity.getSupportFragmentManager();
		Intent intent=new Intent(Intent.ACTION_PICK);
		//setData = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		intent.setType("image/*");
		String[] mimeTypes = {"image/jpeg", "image/png"};
		intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
		activity.startActivityForResult(intent,MainActivity.PICK_IMAGE);	}

	public static void openIdolFragment(final FragmentManager fragmentManager) {
		defaultTransaction(fragmentManager)
				.replace(R.id.fragment_container, new IdolFirestoreFragment())
				.addToBackStack(null)
				.commit();
	}

	public static void openIdolFragment(final Activity activity) {
		Navigation.findNavController(activity, R.id.fragment_container).navigate(
					CameraFragmentDirections.actionCameraToIdols()
		);
	}

	public static void gotoSlideshowFragment(final FragmentManager fragmentManager) {
		//ImageLoader.getInstance().clearMemoryCache();

		final boolean popped = fragmentManager.popBackStackImmediate(SLIDESHOW_FRAGMENT_TAG, 0);
		if (!popped) {
			openSlideshowFragment(fragmentManager);
		}
	}

	public static void openSlideshowFragment(final FragmentManager fragmentManager) {

//		defaultTransaction(fragmentManager)
//				.replace(R.id.fragment_container, new SlideshowFragment())
//				.addToBackStack(null)
//				.commit();
	}

	private static Uri openMarketUrl(final String packageName) {
		return Uri.parse("market://details")
				.buildUpon()
				.appendQueryParameter("id", packageName)
				.build();
	}

	private static Uri openGalaxyMarketUrl(final String packageName) {
		return Uri.parse("samsungapps://ProductDetail/"+ packageName)
				.buildUpon()
				.build();
	}

	private static Uri openOnestoreMarketUrl() {
		return Uri.parse("onestore://common/product/0000753468?view_type=1")
				.buildUpon()
				.build();
	}


	private static Uri getGooglePlayUrl(final String packageName) {
		return Uri.parse("https://play.google.com/store/apps/details")
				.buildUpon()
				.appendQueryParameter("id", packageName)
				.build();
	}

	private static Uri getGalaxyPlayUrl(final String packageName) {
		return Uri.parse("https://galaxystore.samsung.com/geardetail/"+packageName)
				.buildUpon()
				.build();
	}

	private static Uri getOnestorePlayUrl() {
		return Uri.parse("https://www.onestore.co.kr/userpoc/game/view?pid=0000753468")
				.buildUpon()
				.build();
	}

	private static void installApp(final AppCompatActivity activity, final String packageName) {
		if (Preferences.UPLOAD_STORE == Preferences.Store.GOOGLE_PLAY) {
			try {
				// Try market:// scheme
				activity.startActivity(new Intent(Intent.ACTION_VIEW, openMarketUrl(packageName)));
			} catch (final ActivityNotFoundException e) {
				// Fall back to google play URL (don't worry F-Droid can handle it :)
				activity.startActivity(new Intent(Intent.ACTION_VIEW, getGooglePlayUrl(packageName)));
			}
		}
		else if (Preferences.UPLOAD_STORE == Preferences.Store.GALAXY_APPS) {
//			try {
//				// Try market:// scheme
//				context.startActivity(new Intent(Intent.ACTION_VIEW, openGalaxyMarketUrl(packageName)));
//			} catch (final ActivityNotFoundException e) {
//				// Fall back to google play URL (don't worry F-Droid can handle it :)
//				context.startActivity(new Intent(Intent.ACTION_VIEW, getGalaxyPlayUrl(packageName)));
//			}
		}
		else if (Preferences.UPLOAD_STORE == Preferences.Store.ONE_STORE) {
//			try {
//				// Try market:// scheme
//				context.startActivity(new Intent(Intent.ACTION_VIEW, openOnestoreMarketUrl()));
//			} catch (final ActivityNotFoundException e) {
//				// Fall back to google play URL (don't worry F-Droid can handle it :)
//				context.startActivity(new Intent(Intent.ACTION_VIEW, getOnestorePlayUrl()));
//			}
		}
	}

	public static void openRate(final AppCompatActivity activity) {
		installApp(activity, activity.getPackageName());
	}

	public static void openShare(final Context context) {
		String appName = context.getString(R.string.app_name);
		ShareUtils.shareUrl(
				context,
				context.getString(R.string.share_subject, appName),
				context.getString(
						R.string.share_message,
						appName,
						context.getPackageName()
				)
		);
	}

	public static void openYoutube(final AppCompatActivity activity) {
		activity.startActivity(new Intent(Intent.ACTION_VIEW)
					.setData(Uri.parse(MainActivity.YOUTUBE_CHANNEL)) // edit this url
					.setPackage("com.google.android.youtube"));	// do not edit
	}

	public static void navigateUp(final Activity activity) {
		Navigation.findNavController(activity, R.id.fragment_container)
				.navigateUp();
	}

	public static void signIn(final Activity activity) {
		MPreference preference = new MPreference(activity.getBaseContext());

		if (preference.isNotLoggedIn()) {
			preference.setFragmentAfterLogin(CAMERA);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_to_flogin);
		}
	}

	public static void signOut(final AppCompatActivity activity) {
		LogoutFragment signout = new LogoutFragment();
		signout.show(activity.getSupportFragmentManager(),"LogoutFragment");
	}

	public static void openIdolProfile(final Activity activity, IdolProfile idol) {
		MPreference preference = new MPreference(activity.getBaseContext());

		if (preference.isNotLoggedIn()) {
			preference.setFragmentAfterLogin(IDOLPROFILE);
			if (idol != null)
				preference.saveIdolProfile(idol);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_idols_to_flogin);
		} else {
			Bundle bundle = new Bundle();
			bundle.putParcelable(NavigationHelper.IDOLPROFILE, idol);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_to_FIdolProfile, bundle);
		}
	}

	public static void openIdolGalllery(final Activity activity, IdolProfile idol) {
		MPreference preference = new MPreference(activity.getBaseContext());

		if (preference.isNotLoggedIn()) {
			preference.setFragmentAfterLogin(IDOLGALLERY);
			preference.saveIdolProfile(idol);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_idols_to_flogin);
		} else {
			Bundle bundle = new Bundle();
			bundle.putParcelable(NavigationHelper.IDOLPROFILE, idol);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_to_idolgallery_fragment, bundle);
		}
	}

	public static void openMyProfile(final Activity activity) {
		Navigation.findNavController(activity, R.id.fragment_container)
				.navigate(R.id.action_to_my_profile);
	}

	public static void openIdolImageProfile(
			final Activity activity,
			IdolImage idolImage,
			IdolProfile idolProfile
	) {
		MPreference preference = new MPreference(activity.getBaseContext());

		if (preference.isNotLoggedIn()) {
			preference.setFragmentAfterLogin(IDOLIMAGE);
			preference.saveIdolImage(idolImage);
			preference.saveIdolProfile(idolProfile);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_idols_to_flogin);
		} else {
			Bundle bundle = new Bundle();
			bundle.putParcelable(IDOLIMAGE, idolImage);
			bundle.putParcelable(IDOLPROFILE, idolProfile);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_to_idolimageprofile_fragment,bundle);

		}
	}

	public static void openMyIdolProfile(
			final Activity activity,
			IdolProfile idolProfile,
			IdolImage idolImage
	) {
		MPreference preference = new MPreference(activity.getBaseContext());

		if (preference.isNotLoggedIn()) {
			preference.setFragmentAfterLogin(MYIDOLPROFILE);
			preference.saveIdolImage(idolImage);
			preference.saveIdolProfile(idolProfile);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_idols_to_flogin);
		} else {
			Bundle bundle = new Bundle();
			bundle.putParcelable(IDOLPROFILE, idolProfile);
			bundle.putParcelable(IDOLIMAGE, idolImage);
			Navigation.findNavController(activity, R.id.fragment_container)
					.navigate(R.id.action_to_myidolprofile_fragment,bundle);

		}
	}

	public static void openSettings(final Activity activity) {
		final Intent intent = new Intent(activity, SettingsActivity.class);
		intent.putExtra(
				SettingsActivity.EXTRA_LAUNCH_SOURCE,
				SettingsActivity.LaunchSource.STILL_IMAGE
		);
		activity.startActivity(intent);
		activity.finish();
	}

	public static void openAbout(final Activity activity) {
		final Intent intent = new Intent(activity, AboutActivity.class);
		activity.startActivity(intent);
		activity.finish();
	}

	public static void openClassifier(final Activity activity) {
		final Intent intent = new Intent(activity, ClassifierActivity.class);
		activity.startActivity(intent);
		activity.finish();
	}

}
