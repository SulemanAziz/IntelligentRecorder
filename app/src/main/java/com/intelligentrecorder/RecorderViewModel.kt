package com.intelligentrecorder

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class RecorderViewModel : ViewModel() {

    companion object {
        private const val MAX_BUFFER_FRAMES = 3000 // ~100 seconds at 30fps
        private const val MAX_FRAME_DIMENSION = 1280 // downscale larger frames to fit 720p
    }

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isMoving = MutableStateFlow(false)
    val isMoving: StateFlow<Boolean> = _isMoving.asStateFlow()

    private val _isMirrorMoving = MutableStateFlow(false)
    val isMirrorMoving: StateFlow<Boolean> = _isMirrorMoving.asStateFlow()

    private val _isForegroundMoving = MutableStateFlow(false)
    val isForegroundMoving: StateFlow<Boolean> = _isForegroundMoving.asStateFlow()

    private val _threshold = MutableStateFlow(5f)
    val threshold: StateFlow<Float> = _threshold.asStateFlow()

    private val _mirrorRegionThreshold = MutableStateFlow(8f)
    val mirrorRegionThreshold: StateFlow<Float> = _mirrorRegionThreshold.asStateFlow()

    private val _currentMode = MutableStateFlow(DetectionMode.FOREGROUND)
    val currentMode: StateFlow<DetectionMode> = _currentMode.asStateFlow()

    private val _mirrorPoints = MutableStateFlow<List<MirrorPoint>>(emptyList())
    val mirrorPoints: StateFlow<List<MirrorPoint>> = _mirrorPoints.asStateFlow()

    private val videoBuffer = mutableListOf<Bitmap>()
    private val bufferLock = Object()
    val bufferfilled = MutableStateFlow(false)

    private val _savedVideoUri = MutableStateFlow<String?>(null)
    val savedVideoUri: StateFlow<String?> = _savedVideoUri.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun clearSavedUri() {
        _savedVideoUri.value = null
    }

    fun setMode(mode: DetectionMode) {
        _currentMode.value = mode
    }

    fun setMirrorPoints(points: List<MirrorPoint>) {
        _mirrorPoints.value = points
    }

    fun startRecording(context: Context) {
        _isRecording.value = true
    }

    fun stopRecording(context: Context) {
        _isRecording.value = false
    }

    fun setThreshold(value: Float) {
        _threshold.value = value
    }

    fun setMirrorRegionThreshold(value: Float) {
        _mirrorRegionThreshold.value = value
    }

    fun updateMotion(isMoving: Boolean) {
        _isMoving.value = isMoving
    }

    fun updateMirrorMotion(isMoving: Boolean) {
        _isMirrorMoving.value = isMoving
    }

    fun updateForegroundMotion(isMoving: Boolean) {
        _isForegroundMoving.value = isMoving
    }

    private fun downscaleBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= MAX_FRAME_DIMENSION && height <= MAX_FRAME_DIMENSION) {
            return bitmap
        }
        val scale = MAX_FRAME_DIMENSION.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        if (scaled !== bitmap) {
            bitmap.recycle()
        }
        return scaled
    }

    fun addFrameToBuffer(image: Bitmap) {
        if (_isRecording.value) {
            val scaled = downscaleBitmap(image)
            synchronized(bufferLock) {
                if (videoBuffer.size < MAX_BUFFER_FRAMES) {
                    videoBuffer.add(scaled)
                    if (!bufferfilled.value) {
                        bufferfilled.value = true
                    }
                } else {
                    scaled.recycle()
                }
            }
        } else {
            image.recycle()
        }
    }

    fun clearBuffer() {
        synchronized(bufferLock) {
            videoBuffer.forEach { it.recycle() }
            videoBuffer.clear()
            bufferfilled.value = false
        }
    }

    fun convertFramesToVideo(
        framebuffer: List<Bitmap>,
        outputPath: String,
        fps: Int = 30,
        bitrate: Int = 4_000_000
    ) {
        if (framebuffer.isEmpty()) return

        val width = framebuffer[0].width
        val height = framebuffer[0].height

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = codec.createInputSurface()
        codec.start()

        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val bufferInfo = MediaCodec.BufferInfo()
        var trackIndex = -1
        var muxerStarted = false
        var currentFramePts = 0L
        val frameDurationUs = 1_000_000L / fps

        fun drainEncoder(endOfStream: Boolean) {
            if (endOfStream) codec.signalEndOfInputStream()
            while (true) {
                val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                when {
                    outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                    outIndex >= 0 -> {
                        val encodedData = codec.getOutputBuffer(outIndex)!!
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            bufferInfo.presentationTimeUs = currentFramePts
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }
                        codec.releaseOutputBuffer(outIndex, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                    }
                    else -> if (!endOfStream) return else continue
                }
            }
        }

        framebuffer.forEachIndexed { index, bitmap ->
            currentFramePts = index.toLong() * frameDurationUs
            val canvas = surface.lockCanvas(null)
            try {
                canvas.drawBitmap(bitmap, 0f, 0f, null)
            } finally {
                surface.unlockCanvasAndPost(canvas)
            }
            drainEncoder(false)
        }

        drainEncoder(true)
        codec.stop()
        codec.release()
        surface.release()
        if (muxerStarted) {
            muxer.stop()
        }
        muxer.release()

        Log.d("Video Encoder", "Video Encoded!")
    }

    fun saveVideo(context: Context) {
        if (_isSaving.value) return
        _isSaving.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bufferSnapshot: List<Bitmap>
                synchronized(bufferLock) {
                    bufferSnapshot = videoBuffer.toList()
                }

                if (bufferSnapshot.isEmpty()) {
                    _isSaving.value = false
                    return@launch
                }

                val fileName = "IntelligentRecorder_${System.currentTimeMillis()}.mp4"
                val tempFile = File(context.filesDir, fileName)

                convertFramesToVideo(
                    framebuffer = bufferSnapshot,
                    outputPath = tempFile.absolutePath
                )

                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                )
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        tempFile.inputStream().copyTo(outputStream)
                    }
                    _savedVideoUri.value = uri.toString()
                } else {
                    Log.e("RecorderViewModel", "Failed to create MediaStore entry")
                }

                tempFile.delete()
                clearBuffer()
            } catch (e: Exception) {
                Log.e("RecorderViewModel", "Failed to save video", e)
            } finally {
                _isSaving.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        synchronized(bufferLock) {
            videoBuffer.forEach { it.recycle() }
            videoBuffer.clear()
        }
    }
}
