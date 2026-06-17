package com.intelligentrecorder.MotionDetection

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.collectAsState
import com.intelligentrecorder.RecorderViewModel
import java.nio.ByteBuffer

class MotionDetector(private val viewModel: RecorderViewModel) : ImageAnalysis.Analyzer {

    private var previousYPlane: ByteArray? = null
    private var previousWidth: Int = 0
    private var previousHeight: Int = 0

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val yBuffer = imageProxy.planes[0].buffer
        val yRowStride = imageProxy.planes[0].rowStride
        val width = imageProxy.width
        val height = imageProxy.height

        val currentYData = extractYPlane(yBuffer, yRowStride, width, height)

        val previous = previousYPlane
        if (previous != null && previousWidth == width && previousHeight == height) {
            var changedPixels = 0
            val totalPixels = currentYData.size
            for (i in currentYData.indices) {
                val diff = (currentYData[i].toInt() and 0xFF) - (previous[i].toInt() and 0xFF)
                if (diff != 0) {
                    changedPixels++
                }
            }

            // Determining Motion Here
            ////////////////////////////////////////////////////////////////////////////////////////

            val percentChanged = (changedPixels.toFloat() / totalPixels) * 100f
            val threshold = viewModel.threshold.value
            val isMoving = percentChanged > threshold
            viewModel.updateMotion(isMoving)

            ////////////////////////////////////////////////////////////////////////////////////////
            if (isMoving && viewModel.isRecording.value == true) { // only add to buffer when recording and movement is going on,
                                                                  // otherwise just update the icon.
                val rawimage: Bitmap = imageProxy.toBitmap();
                val matrix = Matrix().apply{
                    postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                }

                val image = Bitmap.createBitmap(
                    rawimage, 0, 0,
                    rawimage.width, rawimage.height,
                    matrix, true
                )

                // Need to do all the above to fix rotated video.

                viewModel.addFrameToBuffer(image)
            }
        }

        previousYPlane = currentYData
        previousWidth = width
        previousHeight = height
        imageProxy.close()
    }

    private fun extractYPlane(
        buffer: ByteBuffer,
        rowStride: Int,
        width: Int,
        height: Int
    ): ByteArray {
        val data = ByteArray(width * height)
        buffer.rewind()
        if (rowStride == width) {
            buffer.get(data)
        } else {
            for (row in 0 until height) {
                buffer.position(row * rowStride)
                buffer.get(data, row * width, width)
            }
        }
        return data
    }
}
