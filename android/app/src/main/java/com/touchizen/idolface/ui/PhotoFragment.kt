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

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.theartofdev.edmodo.cropper.CropImage
import com.touchizen.idolface.ClassifierActivity
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.R
import com.touchizen.idolface.databinding.FragmentPhotoBinding
import com.touchizen.idolface.facedetector.FaceDetectorProcessor
import com.touchizen.idolface.facedetector.GraphicOverlay
import com.touchizen.idolface.facedetector.PreferenceUtils
import com.touchizen.idolface.facedetector.VisionImageProcessor
import com.touchizen.idolface.faceswap.FaceDetectorEngine
import com.touchizen.idolface.faceswap.Landmarks
import com.touchizen.idolface.faceswap.Swap
import com.touchizen.idolface.tflite.Classifier
import com.touchizen.idolface.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.util.*


/** Fragment used for each individual page showing a photo inside of [GalleryFragment] */
@AndroidEntryPoint
open class PhotoFragment internal constructor() :
    Fragment()
    ,RequestListener<Drawable>
    ,FaceDetectorProcessor.OnFaceDetectListener,
    ClassifierActivity.OnClassifierListener {
    private lateinit var binding: FragmentPhotoBinding
    protected lateinit var fullImage: ImageView
    private lateinit var faceImage: ShapeableImageView
    protected lateinit var txtDesc: TextView

    private lateinit var graphicOverlay: GraphicOverlay
    private lateinit var imageProcessor: VisionImageProcessor
    private lateinit var originalBitmap: Bitmap
    public lateinit var recognitions: List<Classifier.Recognition>
    public var inferenceTimeMs: Long = 0
    public var fragmentId: Int = 0
    private lateinit var progressView: CustomProgressView
    private lateinit var swapingProgressBar: ProgressBar
    private var faceIndex: Int = 0
    private var okToSwap = false
    private var hasSwapped = false
    private var faceFullDone = false
    private var faceFaceDone = false

    private lateinit var bitmapFull: Bitmap
    private lateinit var bitmapFace: Bitmap
    private lateinit var bitmapCropped: Bitmap

    private lateinit var bitmapFullSwapped: Bitmap
    private lateinit var bitmapFaceSwapped: Bitmap
    private lateinit var facesFullImage: List<Face>
    private lateinit var facesFaceImage: List<Face>
    private val faceDetectorEngine = FaceDetectorEngine()

    val faceSwapingState = MutableLiveData<SwapState>()
    val faceDetectingState = MutableLiveData<DetectState>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullImage = binding.photoImage
        faceImage = binding.faceImage
        txtDesc = binding.txtDesc
        graphicOverlay = binding.graphicOverlay
        progressView = CustomProgressView(requireActivity())
        //swapingProgressBar = binding.swapingProgressBar

        fullImage.scaleType = ImageView.ScaleType.CENTER

        faceImage.setOnClickListener{ ImageUtils.askPermission(this) }

        onImageLoadFromArguments()

        //progressView.show()
        //swapingProgressBar.visibility = View.GONE

        subscribeObservers()

    }

    private fun subscribeObservers() {
        faceSwapingState.observe(viewLifecycleOwner, {
            when (it) {
                is SwapState.OnSuccess -> {
                    progressView.dismiss()
                }
                is SwapState.OnFailure -> {
                    progressView.dismiss()
                }
                is SwapState.OnSwaping -> {
                    progressView.show()
                }
                else -> {}
            }
        })

        faceDetectingState.observe(viewLifecycleOwner, {
            when (it) {
                is DetectState.OnSuccess -> {
                    progressView.dismiss()
                }
                is DetectState.OnFailure -> {
                    progressView.dismiss()
                }
                is DetectState.OnDetecting -> {
                    progressView.show()
                }
                is DetectState.OnIdolDetecting -> {
                    progressView.show()
                }
                is DetectState.OnRecognizing -> {
                    progressView.show()
                }
                is DetectState.OnReady -> {
                    progressView.show()
                }
                else -> {}
            }
        })

    }

    open fun onImageLoadFromArguments() {
        val args = arguments ?: return
        val resource = args.getString(FILE_NAME_KEY)?.let { File(it) } ?: R.drawable.ic_photo

        fragmentId = args.getInt(FRAGMENT_ID)

        faceIndex = 0
        Glide.with(this)
            .load(resource)
            .listener(this)
            .into(fullImage)
    }

    /**
     * The events of Glide for loading images.
     */

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {

        hasSwapped = false

        val bitmap = resource?.toBitmap()
        when(faceIndex) {
            FACE_FULLIMAGE -> {
                if (bitmap != null) {
                    bitmapFull = bitmap
                    processFullImageForSwap(bitmap)
                }
            }
            FACE_FACEIMAGE -> {
                if (bitmap != null) {
                    bitmapFace = bitmap
                    processFaceImage(bitmap)
                }
            }
            else -> {
                processFullImageForOverlay(bitmap!!)
            }
        }

        return false
    }

    override fun onResume() {
        super.onResume()

        if (!::imageProcessor.isInitialized) {
            createImageProcessor()
        }

        if (::recognitions.isInitialized) {
            val activity = requireActivity() as MainActivity
            activity.showResultsInBottomSheet(recognitions)
            activity.showInference(inferenceTimeMs.toString() + "ms")
        }

    }
    public override fun onPause() {
        super.onPause()
        imageProcessor.run {
            this.stop()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        if (!::imageProcessor.isInitialized) {
            createImageProcessor()
        }
        imageProcessor.run {
            this.stop()
        }
    }

    private fun createImageProcessor() {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val displayMode = sharedPreferences.getString(
            requireContext().getString(R.string.pref_key_face_detection_display_mode),
            "2"
        )

        val isFaceVisible = displayMode != "1"

        try {
            val faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(requireContext())
            imageProcessor = FaceDetectorProcessor(requireContext(), faceDetectorOptions)
            (imageProcessor as FaceDetectorProcessor).setOnFaceDetectListener(this)
            (imageProcessor as FaceDetectorProcessor).setFaceVisible(isFaceVisible)
        } catch (e: Exception) {
            Toast.makeText(
                requireActivity(),
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showFaceDetectOveray(bitmap: Bitmap, rotationDegrees:Int) {

        originalBitmap = bitmap

        //val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
        if (rotationDegrees == 0 || rotationDegrees == 180) {
            graphicOverlay.setImageSourceInfo(bitmap.width, bitmap.height, false)
        } else {
            graphicOverlay.setImageSourceInfo(bitmap.height, bitmap.width, false)
        }

        try {
            // Clear the overlay first
            //graphicOverlay!!.clear()
            faceDetectingState.value = DetectState.OnDetecting
            imageProcessor.processBitmap(bitmap , graphicOverlay)
        } catch (e: IOException) {
            Log.e(TAG,"Error retrieving saved image")
        }

    }

    fun sendNoFaceMessage() {

        val unknownFaces : ArrayList<Classifier.Recognition> = ArrayList<Classifier.Recognition>();

        unknownFaces.add(Classifier.Recognition("0",getString(R.string.no_face),1.0f,null) )
        unknownFaces.add(Classifier.Recognition("0",getString(R.string.no_face),1.0f,null) )
        unknownFaces.add(Classifier.Recognition("0",getString(R.string.no_face),1.0f,null) )

        //unknownFaces.ap
        onRecognitionReceived(unknownFaces,100)
    }

    override fun onFaceAvailable(faces: List<Face>, graphicOverlay: GraphicOverlay) {

        if (faces.size == 0) {
            sendNoFaceMessage()
            return
        }

        if ((activity as MainActivity).isProcessingFrame) {
            return
        }

        (activity as MainActivity).isProcessingFrame = true

        for (face in faces) {

            val left = face.boundingBox.left
            val right = face.boundingBox.right
            val top = face.boundingBox.top
            val bottom = face.boundingBox.bottom

            val width = right - left
            val height = bottom - top

            if (left < 0 || top < 0 ||
                left + width > originalBitmap.width ||
                top + height> originalBitmap.height) {
                (activity as MainActivity).isProcessingFrame = false
                return
            }

            val croppedBmp = Bitmap.createBitmap(originalBitmap,
                left, top,
                width, height
            )

            bitmapCropped = croppedBmp

            faceDetectingState.value = DetectState.OnRecognizing
            (activity as MainActivity?)!!.processImage(
                croppedBmp,
                croppedBmp.width,
                croppedBmp.height,
                0,
                this
            )

            break
        }
    }

    private fun processFullImageForSwap(bitmap: Bitmap): Task<List<Face>> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        return faceDetectorEngine.detectInImage(inputImage)
    }

    private fun processFullImageForOverlay(bitmap: Bitmap) {
        faceDetectingState.value = DetectState.OnReady

        if (!::imageProcessor.isInitialized) {
            createImageProcessor()
        }

        showFaceDetectOveray(bitmap, 0)
    }

    private fun processFaceImage(bitmap: Bitmap) {

        //faceDetectingState.value = DetectState.OnIdolDetecting

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        faceDetectorEngine.detectInImage(inputImage)
            .addOnSuccessListener { faces ->
                facesFaceImage = faces

                faceFaceDone = false
                if (faces.isNotEmpty()) {
                    faceFaceDone = true
                }

//                okToSwap = faceFullDone && faceFaceDone
//
//                try {
//                    (parentFragment as GalleryFragment).swapButton.isEnabled = okToSwap
//                } catch(e: Exception) {
//                    faceDetectingState.value = DetectState.OnFailure(e)
//                }

                faceDetectingState.value = DetectState.OnSuccess()
            }
    }

    override fun onRecognitionReceived(
        results: List<Classifier.Recognition>,
        inferenceTimeMs: Long
    ) {
        this.recognitions = results
        this.inferenceTimeMs = inferenceTimeMs

        try {
            (parentFragment as GalleryFragment).mapResults.put(fragmentId, results)
            (parentFragment as GalleryFragment).mapTimes.put(fragmentId, inferenceTimeMs)
            (parentFragment as GalleryFragment).isFragmentCreated.value = true
        } catch(e: Exception) {
            faceDetectingState.value = DetectState.OnFailure(e)
            return
        }

        faceDetectingState.value = DetectState.OnIdolDetecting
        faceIndex = FACE_FACEIMAGE
        val nIdolImage = arrayIdolImage[recognitions[0].id.toInt()]
        //faceImage.setImageResource(nIdolImage)
        Glide.with(this)
            .load(nIdolImage)
            .listener(this)
            .into(faceImage)

        txtDesc.text = "[" + resources.getText(R.string.app_name).toString() + "] " +
                results[0].title + " --- " +
                String.format("%.2f", 100 * results[0].confidence).toString() + "%"

        //faceDetectingState.value = DetectState.OnSuccess()
    }

    public fun onSwap() {

        if (!::bitmapFull.isInitialized) {
            bitmapFull = originalBitmap
        }

        if (hasSwapped) {
            fullImage.setImageBitmap(bitmapFull)
            faceImage.setImageBitmap(bitmapFace)
            hasSwapped = false
            return
        }

        faceSwapingState.value = SwapState.OnSwaping
        processFullImageForSwap(bitmapFull).addOnSuccessListener{ faces ->

            facesFullImage = faces

            faceFullDone = false
            if (faces.isNotEmpty()) {
                faceFullDone = true
            }

            okToSwap = faceFullDone && faceFaceDone
            try {
                (parentFragment as GalleryFragment).swapButton.isEnabled = okToSwap
            } catch(e:Exception) {
                faceSwapingState.value = SwapState.OnFailure(e)
            }

            requireActivity().runOnUiThread {
                if (okToSwap) {
                    Log.d(tag, "Ready to swap!")
                    try {
                        val landmarksForFaces1 = Landmarks.arrangeLandmarksForFaces(facesFullImage)
                        val landmarksForFaces2 = Landmarks.arrangeLandmarksForFaces(facesFaceImage)

                        bitmapFaceSwapped =
                            Swap.faceSwapAll(
                                bitmapFull,
                                bitmapFace,
                                landmarksForFaces1,
                                landmarksForFaces2
                            )
                        bitmapFullSwapped =
                            Swap.faceSwapAll(
                                bitmapFace,
                                bitmapFull,
                                landmarksForFaces2,
                                landmarksForFaces1
                            )

                        fullImage.setImageBitmap(bitmapFullSwapped)
                        faceImage.setImageBitmap(bitmapCropped)

                        faceSwapingState.value = SwapState.OnSuccess()
                        hasSwapped = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        faceSwapingState.value = SwapState.OnFailure(e)
                    }
                }
                else {
                    faceSwapingState.value = SwapState.OnFailure("okToSwap is false" as Exception)
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            onCropResult(data)
        else
            ImageUtils.cropImage(requireActivity(), data, true)
    }

    private fun onCropResult(data: Intent?) {
        try {
            val imagePath: Uri? = ImageUtils.getCroppedImage(data)

            faceIndex = FACE_FACEIMAGE
            Glide.with(this)
                .load(imagePath)
                .listener(this)
                .into(faceImage)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val FILE_NAME_KEY = "file_name"
        private const val FRAGMENT_ID = "fragmentId"
        private const val TAG = "PhotoFragment"
        private const val FACE_FULLIMAGE = 1
        private const val FACE_FACEIMAGE = 2

        const val RESIZED_WIDTH = 640
        const val RESIZED_HEIGHT = 480

        var arrayIdolImage = arrayOf(0,
            R.raw.idol01, R.raw.idol02, R.raw.idol03, R.raw.idol04, R.raw.idol05,
            R.raw.idol06, R.raw.idol07, R.raw.idol08, R.raw.idol09, R.raw.idol10,
            R.raw.idol11, R.raw.idol12, R.raw.idol13, R.raw.idol14, R.raw.idol15,
            R.raw.idol16, R.raw.idol17, R.raw.idol18, R.raw.idol19, R.raw.idol20,
            R.raw.idol21, R.raw.idol22, R.raw.idol23, R.raw.idol24, R.raw.idol25,
            R.raw.idol26, R.raw.idol27, R.raw.idol28, R.raw.idol29, R.raw.idol30,
            R.raw.idol31, R.raw.idol32, R.raw.idol33, R.raw.idol34, R.raw.idol35,
            R.raw.idol36, R.raw.idol37
        )

        fun create(image: File, position: Int) = PhotoFragment().apply {
            arguments = Bundle().apply {
                putInt(FRAGMENT_ID, position)
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }
    }
}