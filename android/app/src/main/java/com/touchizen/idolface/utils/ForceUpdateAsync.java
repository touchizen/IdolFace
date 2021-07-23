package com.touchizen.idolface.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import com.touchizen.idolface.R;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;

public class ForceUpdateAsync extends AsyncTask<String, String, JSONObject> {

    private Version latestVersion;
    private Version currentVersion;
    private AppCompatActivity activity;

    public ForceUpdateAsync(String currentVersion, AppCompatActivity activity){
        this.currentVersion = new Version(currentVersion);
        this.activity = activity;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            String _latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id="+activity.getPackageName())
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com").get() //.select("div[itemprop=softwareVersion]") //오류발생
                    .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                    .first()
                    .ownText();
            latestVersion = new Version(_latestVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if(latestVersion!= null){
            //if(!currentVersion.equalsIgnoreCase(latestVersion))
            if (currentVersion.compareTo(latestVersion) < 0) {
                //새로운 버전 있음 // 업데이트 가능 */Util.sLog.d("VERSION", "currentVersion="+currentVersion + " , latestVersion=" + latestVersion);
                showForceUpdateDialog();
            }
        }

        super.onPostExecute(jsonObject);
    }

    //강제 업데이트 다이얼로그
    private void showForceUpdateDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(activity.getString(R.string.app_update_notification_channel_id));
        alertDialogBuilder.setMessage(activity.getString(R.string.app_update_notification_content_title));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(activity.getString(R.string.updates_setting_title),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NavigationHelper.openRate(activity);
                        //context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                        dialog.cancel();
                    }
        });

        alertDialogBuilder.show();
    }
}

