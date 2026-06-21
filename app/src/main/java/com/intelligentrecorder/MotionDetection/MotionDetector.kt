package com.intelligentrecorder.MotionDetection

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.intelligentrecorder.DetectionMode
import com.intelligentrecorder.MirrorPoint
import com.intelligentrecorder.RecorderViewModel
import java.nio.ByteBuffer
import kotlin.math.abs

class MotionDetector(private val viewModel: RecorderViewModel) : ImageAnalysis.Analyzer {

    companion object {
        /** Per-pixel luminance change required to count as "changed" (filters sensor noise) */
        private const val PIXEL_CHANGE_THRESHOLD = 30
        /** Number of consecutive frames motion must be detected before triggering */
        private const val MOTION_FRAME_THRESHOLD = 2
    }

    private var previousYPlane: ByteArray? = null
    private var previousWidth: Int = 0
    private var previousHeight: Int = 0
    
    // Temporal filtering: track consecutive motion frames
    private var consecutiveMotionFrames = 0
    private var consecutiveNoMotionFrames = 0

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
            val mirrorThreshold = viewModel.mirrorRegionThreshold.value
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val rawMotionDetected = when (mode) {
                DetectionMode.FOREGROUND -> {
                    // Full-frame detection using foreground threshold
                    detectMotionFullFrame(currentYData, previous, threshold)
                }
                DetectionMode.MIRROR -> {
                    // Detect motion only in mirror region using mirror threshold
                    if (mirrorPoints.size == 4) {
                        detectMotionInRegion(
                            currentYData, previous, mirrorThreshold,
                            mirrorPoints, width, height, rotationDegrees
                        )
                    } else {
                        // Fallback to full frame using mirror threshold for consistency
                        detectMotionFullFrame(currentYData, previous, mirrorThreshold)
                    }
                }
                DetectionMode.HYBRID -> {
                    // Detect motion in both regions with separate thresholds
                    if (mirrorPoints.size == 4) {
                        val mirrorMotion = detectMotionInRegion(
                            currentYData, previous, mirrorThreshold,
                            mirrorPoints, width, height, rotationDegrees
                        )
                        val foregroundMotion = detectMotionFullFrame(
                            currentYData, previous, threshold
                        )
                        // Report individual region results for dual indicators
                        viewModel.updateMirrorMotion(mirrorMotion)
                        viewModel.updateForegroundMotion(foregroundMotion)
                        mirrorMotion || foregroundMotion
                    } else {
                        val foregroundMotion = detectMotionFullFrame(currentYData, previous, threshold)
                        viewModel.updateMirrorMotion(false)
                        viewModel.updateForegroundMotion(foregroundMotion)
                        foregroundMotion
                    }
                }
            }

            // Reset individual indicators for non-hybrid modes
            if (mode != DetectionMode.HYBRID) {
                viewModel.updateMirrorMotion(false)
                viewModel.updateForegroundMotion(false)
            }

            // Apply temporal filtering to reduce false positives
            val isMoving = applyTemporalFilter(rawMotionDetected)
            
            viewModel.updateMotion(isMoving)

            if (isMoving && viewModel.isRecording.value) {
                val rawImage: Bitmap = imageProxy.toBitmap()
                val matrix = Matrix().apply {
                    postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                }

                val image = Bitmap.createBitmap(
                    rawImage, 0, 0,
                    rawImage.width, rawImage.height,
                    matrix, true
                )
                // Recycle the unrotated bitmap if a new one was created
                if (image !== rawImage) {
                    rawImage.recycle()
                }

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
        
        // Sample pixels at intervals for performance (every 4th pixel)
        val sampleInterval = 4
        var sampledPixels = 0

        for (i in currentYData.indices step sampleInterval) {
            sampledPixels++
            val diff = abs(
                (currentYData[i].toInt() and 0xFF) - (previousYData[i].toInt() and 0xFF)
            )
            if (diff > PIXEL_CHANGE_THRESHOLD) {
                changedPixels++
            }
        }

        val percentChanged = (changedPixels.toFloat() / sampledPixels) * 100f
        return percentChanged > threshold
    }

    /**
     * Apply temporal filtering to reduce false positives from single-frame noise.
     * Motion must be detected in multiple consecutive frames to trigger.
     */
    private fun applyTemporalFilter(currentFrameHasMotion: Boolean): Boolean {
        if (currentFrameHasMotion) {
            consecutiveMotionFrames++
            consecutiveNoMotionFrames = 0
            
            // Require motion in multiple consecutive frames
            return consecutiveMotionFrames >= MOTION_FRAME_THRESHOLD
        } else {
            consecutiveNoMotionFrames++
            consecutiveMotionFrames = 0
            
            // Stop motion after a few frames of no motion
            return consecutiveNoMotionFrames < MOTION_FRAME_THRESHOLD
        }
    }

    /**
     * Transform normalized display coordinates (0..1) to sensor image pixel coordinates,
     * accounting for the rotation between sensor and display orientations.
     */
    private fun displayToSensorCoords(
        normX: Float,
        normY: Float,
        sensorWidth: Int,
        sensorHeight: Int,
        rotationDegrees: Int
    ): Pair<Int, Int> {
        val (sx, sy) = when (rotationDegrees) {
            90 -> Pair(normY * sensorWidth, (1f - normX) * sensorHeight)
            180 -> Pair((1f - normX) * sensorWidth, (1f - normY) * sensorHeight)
            270 -> Pair((1f - normY) * sensorWidth, normX * sensorHeight)
            else -> Pair(normX * sensorWidth, normY * sensorHeight)
        }
        return Pair(
            sx.toInt().coerceIn(0, sensorWidth - 1),
            sy.toInt().coerceIn(0, sensorHeight - 1)
        )
    }

    private fun detectMotionInRegion(
        currentYData: ByteArray,
        previousYData: ByteArray,
        threshold: Float,
        points: List<MirrorPoint>,
        width: Int,
        height: Int,
        rotationDegrees: Int
    ): Boolean {
        // Convert normalized display-space points to sensor-space pixel coordinates
        val sensorPoints = points.map { point ->
            displayToSensorCoords(point.x, point.y, width, height, rotationDegrees)
        }

        // Calculate bounding box of the mirror region in sensor space
        val minX = sensorPoints.minOf { it.first }.coerceIn(0, width - 1)
        val maxX = sensorPoints.maxOf { it.first }.coerceIn(0, width - 1)
        val minY = sensorPoints.minOf { it.second }.coerceIn(0, height - 1)
        val maxY = sensorPoints.maxOf { it.second }.coerceIn(0, height - 1)

        var changedPixels = 0
        var totalRegionPixels = 0
        
        // Sample pixels at intervals for performance
        val sampleInterval = 4

        // Check only pixels within the bounding box
        for (y in minY..maxY step sampleInterval) {
            for (x in minX..maxX step sampleInterval) {
                val index = y * width + x
                if (index >= 0 && index < currentYData.size && index < previousYData.size) {
                    totalRegionPixels++
                    val diff = abs(
                        (currentYData[index].toInt() and 0xFF) - (previousYData[index].toInt() and 0xFF)
                    )
                    if (diff > PIXEL_CHANGE_THRESHOLD) {
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
