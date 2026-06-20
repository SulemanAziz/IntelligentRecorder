package com.intelligentrecorder.MotionDetection

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.collectAsState
import com.intelligentrecorder.DetectionMode
import com.intelligentrecorder.MirrorPoint
import com.intelligentrecorder.RecorderViewModel
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

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
            val mode = viewModel.currentMode.value
            val mirrorPoints = viewModel.mirrorPoints.value
            val threshold = viewModel.threshold.value
            
            val isMoving = when (mode) {
                DetectionMode.FOREGROUND -> {
                    // Original full-frame detection
                    detectMotionFullFrame(currentYData, previous, threshold)
                }
                DetectionMode.MIRROR -> {
                    // Detect motion only in mirror region
                    if (mirrorPoints.size == 4) {
                        detectMotionInRegion(currentYData, previous, threshold, mirrorPoints, width, height)
                    } else {
                        // Fallback to full frame if mirror not configured
                        detectMotionFullFrame(currentYData, previous, threshold)
                    }
                }
                DetectionMode.HYBRID -> {
                    // Detect motion in both regions (mirror + foreground)
                    if (mirrorPoints.size == 4) {
                        val mirrorMotion = detectMotionInRegion(currentYData, previous, threshold, mirrorPoints, width, height)
                        val foregroundMotion = detectMotionFullFrame(currentYData, previous, threshold)
                        mirrorMotion || foregroundMotion
                    } else {
                        detectMotionFullFrame(currentYData, previous, threshold)
                    }
                }
            }
            
            viewModel.updateMotion(isMoving)

            if (isMoving && viewModel.isRecording.value == true) {
                val rawimage: Bitmap = imageProxy.toBitmap()
                val matrix = Matrix().apply{
                    postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                }

                val image = Bitmap.createBitmap(
                    rawimage, 0, 0,
                    rawimage.width, rawimage.height,
                    matrix, true
                )

                viewModel.addFrameToBuffer(image)
            }
        }

        previousYPlane = currentYData
        previousWidth = width
        previousHeight = height
        imageProxy.close()
    }
    
    private fun detectMotionFullFrame(
        currentYData: ByteArray,
        previousYData: ByteArray,
        threshold: Float
    ): Boolean {
        var changedPixels = 0
        val totalPixels = currentYData.size
        
        for (i in currentYData.indices) {
            val diff = (currentYData[i].toInt() and 0xFF) - (previousYData[i].toInt() and 0xFF)
            if (diff != 0) {
                changedPixels++
            }
        }
        
        val percentChanged = (changedPixels.toFloat() / totalPixels) * 100f
        return percentChanged > threshold
    }
    
    private fun detectMotionInRegion(
        currentYData: ByteArray,
        previousYData: ByteArray,
        threshold: Float,
        points: List<MirrorPoint>,
        width: Int,
        height: Int
    ): Boolean {
        // Calculate bounding box of the mirror region
        val minX = points.minOf { it.x }.toInt().coerceIn(0, width - 1)
        val maxX = points.maxOf { it.x }.toInt().coerceIn(0, width - 1)
        val minY = points.minOf { it.y }.toInt().coerceIn(0, height - 1)
        val maxY = points.maxOf { it.y }.toInt().coerceIn(0, height - 1)
        
        var changedPixels = 0
        var totalRegionPixels = 0
        
        // Check only pixels within the bounding box
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val index = y * width + x
                if (index < currentYData.size && index < previousYData.size) {
                    totalRegionPixels++
                    val diff = (currentYData[index].toInt() and 0xFF) - (previousYData[index].toInt() and 0xFF)
                    if (diff != 0) {
                        changedPixels++
                    }
                }
            }
        }
        
        if (totalRegionPixels == 0) return false
        
        val percentChanged = (changedPixels.toFloat() / totalRegionPixels) * 100f
        return percentChanged > threshold
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
