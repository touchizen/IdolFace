/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.touchizen.idolface.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.touchizen.idolface.R;

import java.util.Date;


public class ImageUtil {

    public static final String TAG = ImageUtil.class.getSimpleName();

//    public static String generateImageTitle(UploadImagePrefix prefix, String parentId) {
//        if (parentId != null) {
//            return prefix.toString() + parentId;
//        }
//
//        return prefix.toString() + new Date().getTime();
//    }

    public static void loadImage(RequestManager glideRequest, String url, ImageView imageView) {
        loadImage(glideRequest, url, imageView, DiskCacheStrategy.ALL);
    }

    public static void loadImage(
            RequestManager glideRequest,
            String url,
            ImageView imageView,
            DiskCacheStrategy diskCacheStrategy
    ) {
        glideRequest.load(url)
                .diskCacheStrategy(diskCacheStrategy)
                .error(R.drawable.ic_other_person)
                .into(imageView);
    }

    public static void loadImage(
            RequestManager glideRequest,
            String url,
            ImageView imageView,
            RequestListener<Drawable> listener
    ) {
        glideRequest.load(url)
                .error(R.drawable.ic_other_person)
                .listener(listener)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(imageView);
    }

    public static void loadImageCenterCrop(
            RequestManager glideRequest,
            String url,
            ImageView imageView,
            int width,
            int height
    ) {
        glideRequest.load(url)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_other_person)
                .into(imageView);
    }

    public static void loadImageCenterCrop(
            RequestManager glideRequest,
            String url,
            ImageView imageView,
            int width,
            int height,
            RequestListener<Drawable> listener
    ) {
        glideRequest.load(url)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_other_person)
                .listener(listener)
                .into(imageView);
    }

    public static void loadImageCenterCrop(
            RequestManager glideRequest,
            String url,
            ImageView imageView,
            RequestListener<Drawable> listener
    ) {
        glideRequest.load(url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_other_person)
                .listener(listener)
                .into(imageView);
    }

    public static void loadImageCenterCrop(
            RequestManager glideRequest,
            String url,
            ImageView imageView,
            int width,
            int height,
            long timePositionUs
    ) {

        RequestOptions options = new RequestOptions().frame(timePositionUs);
        glideRequest.asBitmap()
                .load(url)
                .apply(options)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_other_person)
                .into(imageView);
    }

    @Nullable
    public static Bitmap loadBitmap(
            RequestManager glideRequest,
            String url,
            int width,
            int height
    ) {
        try {
            return glideRequest.asBitmap()
                    .load(url)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .submit(width, height)
                    .get();
        } catch (Exception e) {
            LogUtil.logError(TAG, "getBitmapfromUrl", e);
            return null;
        }
    }

    public static void loadImageWithSimpleTarget(
            RequestManager glideRequest,
            String url,
            SimpleTarget<Bitmap> simpleTarget
    ) {
        glideRequest.asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(simpleTarget);
    }

    public static void loadLocalImage(
            RequestManager glideRequest,
            Uri uri,
            ImageView imageView,
            RequestListener<Drawable> listener
    ) {
        glideRequest.load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .fitCenter()
                .listener(listener)
                .into(imageView);
    }
}
