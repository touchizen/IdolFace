package com.touchizen.idolface.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.touchizen.idolface.MainActivity;

import de.mateware.snacky.Snacky;

public class SnackyUtils {
    public static void showSnackTemporarily(Activity activity, String text) {
        Snackbar sb = Snacky.builder()
                .setActivity(activity)
                .setBackgroundColor(Color.parseColor("#0077CC"))
                .setTextColor(Color.parseColor("#FFFFFF"))
                //.setText(getString(R.string.click_another_one))
                .setText(text)
                .centerText()
                .setDuration(8000)
                .info();
                //.show();
        sb.addCallback( new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                ((MainActivity) activity).hideSystemUI();
            }

            @Override
            public void onDismissed(Snackbar sb, int event) {
                ((MainActivity) activity).showSystemUi();
            }
        });

        sb.show();

    }

    public static void showSnackWithOneButton(
            Activity activity,
            String message,
            String btnText,
            View.OnClickListener onClickListener
    ) {
        Snackbar sb =Snacky.builder()
                .setActionText(btnText)
                .setActivity(activity)
                .setActionTextColor(Color.parseColor("#fdee48"))
                .setBackgroundColor(Color.parseColor("#0077CC"))
                .setTextColor(Color.parseColor("#FFFFFF"))
                .setActionClickListener(onClickListener)
                .setText(message)
                .setDuration(Snacky.LENGTH_INDEFINITE)
                .build();
//                .show();

        sb.addCallback( new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                ((MainActivity) activity).hideSystemUI();
            }

            @Override
            public void onDismissed(Snackbar sb, int event) {
                ((MainActivity) activity).showSystemUi();
            }
        });

        sb.show();
    }
}
