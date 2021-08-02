package com.touchizen.idolface

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Size
import com.touchizen.idolface.env.BorderedText
import com.touchizen.idolface.env.Logger
import com.touchizen.idolface.tflite.Classifier
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class ClassifierActivity : MainActivity() {
    private val LOGGER: Logger = Logger()
    private val DESIRED_PREVIEW_SIZE = Size(640, 480)
    private val TEXT_SIZE_DIP = 10f
    private var rgbFrameBitmap: Bitmap? = null
    private var lastProcessingTimeMs: Long = 0
    private var sensorOrientation: Int? = null
    private var classifier: Classifier? = null
    private val borderedText: BorderedText? = null


    /** Input image size of the model along x axis.  */
    private var imageSizeX = 0

    /** Input image size of the model along y axis.  */
    private var imageSizeY = 0

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    public interface OnClassifierListener {
        fun onRecognitionReceived(
            results:List<Classifier.Recognition>,
            inferenceTimeMs: Long
        )
    }

    @Synchronized
    override fun onResume() {
        LOGGER.d("onResume $this")
        super.onResume()
        handlerThread = HandlerThread("inference")
        handlerThread?.start()
        handler = Handler(handlerThread!!.getLooper())
    }

    @Synchronized
    override fun onPause() {
        LOGGER.d("onPause $this")
        handlerThread?.quitSafely()
        try {
            handlerThread?.join()
            handlerThread = null
            handler = null
        } catch (e: InterruptedException) {
            LOGGER.e(e, "Exception!")
        }
        super.onPause()
    }

    override fun processImage(
        rgbFrameBitmap: Bitmap,
        previewWidth: Int,
        previewHeight: Int,
        rotation: Int,
        listener: OnClassifierListener?
    ) {

        this.rgbFrameBitmap = rgbFrameBitmap

        recreateClassifier(getDevice(), getGender(), getNumThreads())
        if (classifier == null) {
            LOGGER.e("No classifier on preview!")
            return
        }
        sensorOrientation = rotation - getScreenOrientation()
        LOGGER.i(
            "Camera orientation relative to screen canvas: %d",
            sensorOrientation
        )

//        rgbFrameBitmap!!.setPixels(
//            getRgbBytes(),
//            0,
//            previewWidth,
//            0,
//            0,
//            previewWidth,
//            previewHeight
//        )

        val cropSize: Int = Math.min(previewWidth, previewHeight)
        runInBackground {
            if (classifier != null) {
                val startTime = SystemClock.uptimeMillis()
                val results: List<Classifier.Recognition> = classifier!!.recognizeImage(
                    rgbFrameBitmap,
                    sensorOrientation!!
                )
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime
                LOGGER.v("Detect: %s", results)
                runOnUiThread {
                    if (listener != null) {
                        listener.onRecognitionReceived(results, lastProcessingTimeMs)
                    }
                    else {
                        showResultsInBottomSheet(results)
                        showFrameInfo(previewWidth.toString() + "x" + previewHeight)
                        showCropInfo(imageSizeX.toString() + "x" + imageSizeY)
                        showCameraResolution(cropSize.toString() + "x" + cropSize)
                        showRotationInfo(sensorOrientation.toString())
                        showInference(lastProcessingTimeMs.toString() + "ms")
                    }
                }
            }
            readyForNextImage()
        }

    }

    override fun onInferenceConfigurationChanged() {
        if (rgbFrameBitmap == null) {
            // Defer creation until we're getting camera frames.
            return
        }
        val device: Classifier.Device? = getDevice()
        val gender: Classifier.Gender? = getGender()

        val numThreads = getNumThreads()
        runInBackground { recreateClassifier(device, gender, numThreads) }
    }

    @Synchronized
    protected fun runInBackground(r: Runnable?) {
        if (handler != null) {
            handler?.post(r!!)
        }
    }

    private fun recreateClassifier(
        device: Classifier.Device?,
        gender: Classifier.Gender?,
        numThreads: Int
    ) {
        if (classifier != null) {
            LOGGER.d("Closing classifier.")
            classifier?.close()
            classifier = null
        }
        try {
            LOGGER.d(
                "Creating classifier (device=%s, gender=%s, numThreads=%d)",
                device, gender, numThreads
            )
            classifier = Classifier.create(this, device, gender, numThreads)
        } catch (e: IOException) {
            LOGGER.e(e,"Failed to create classifier.")
        }

        // Updates the input image size.
        imageSizeX = classifier!!.imageSizeX
        imageSizeY = classifier!!.imageSizeY
    }

}