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

package com.touchizen.idolface.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.touchizen.idolface.R
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


/** Fragment used for each individual page showing a photo inside of [GalleryFragment] */
@AndroidEntryPoint
class UriPhotoFragment internal constructor() : PhotoFragment() {

    override fun onImageLoadFromArguments() {
        val args = arguments ?: return
        val resource = args.getParcelable<Uri>(FILE_NAME_KEY) ?: R.drawable.ic_photo
        Glide.with(this)
            .load(resource)
            .listener(this)
            .into(fullImage)
    }
    
    companion object {
        private const val FILE_NAME_KEY = "file_name"

        fun create(imageUri: Uri) = UriPhotoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(FILE_NAME_KEY, imageUri)
            }
        }
    }
}