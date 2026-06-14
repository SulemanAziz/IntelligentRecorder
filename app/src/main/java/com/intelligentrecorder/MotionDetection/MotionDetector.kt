package com.intelligentrecorder.MotionDetection

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.intelligentrecorder.RecorderViewModel
import java.nio.ByteBuffer

class MotionDetector(private val viewModel: RecorderViewModel) : ImageAnalysis.Analyzer {

    private var previousYPlane: ByteArray? = null
    private var previousWidth: Int = 0
    private var previousHeight: Int = 0

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
            if (isMoving) {
                val frameData = imageProxyToByteArray(imageProxy)
                if (frameData != null) {
                    viewModel.addFrameToBuffer(frameData)
                }
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

    private fun imageProxyToByteArray(imageProxy: ImageProxy): ByteArray? {
        return try {
            val yBuffer = imageProxy.planes[0].buffer
            val uvBuffer = imageProxy.planes[1].buffer
            val vBuffer = if (imageProxy.planes.size > 2) {
                imageProxy.planes[2].buffer
            } else {
                null
            }

            val ySize = yBuffer.remaining()
            val uvSize = uvBuffer.remaining()
            val vSize = vBuffer?.remaining() ?: 0
            val totalSize = ySize + uvSize + vSize + 8

            val result = ByteBuffer.allocate(totalSize)
            result.putInt(imageProxy.width)
            result.putInt(imageProxy.height)
            yBuffer.rewind()
            uvBuffer.rewind()
            vBuffer?.rewind()

            result.put(yBuffer)
            result.put(uvBuffer)
            vBuffer?.let { result.put(it) }

            result.array()
        } catch (e: Exception) {
            null
        }
    }
}
