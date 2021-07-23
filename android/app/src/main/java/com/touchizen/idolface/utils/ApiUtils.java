package com.touchizen.idolface.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.touchizen.idolface.model.Idol;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiUtils {

	public static boolean getIdolList(final Context context) {

		try {
			OkHttpClient client = new OkHttpClient();

			String url = "https://deepshadowing.com/api/data.json";

			Request request = new Request.Builder()
					.addHeader("Authorization", "TEST AUTH")
					.url(url)
					.build();
			Response response = client.newCall(request)
					.execute();

			String result = response.body().string();

			Gson gson = new Gson();
			Idol info = gson.fromJson(result, Idol.class);

			Log.i("result:", "id: " + info.getId());
			Log.i("result:", "name: " + info.getName());

			return true;
		} catch(Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static void getIdolList() {
		OkHttpClient client = new OkHttpClient();
		// GET request
		Request request = new Request.Builder()
				.url("https://deepshadowing.com/api/data.json")
				.build();

		client.newCall(request).enqueue(
			new Callback() {
				@Override
				public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
					Log.w("LOG_TAG", response.body().string());
					Log.i("LOG_TAG", response.toString());
				}

				@Override
				public void onFailure(@NotNull Call call, @NotNull IOException e) {
					Log.e("LOG_TAG", e.toString());
				}
            }
		);
	}
}
