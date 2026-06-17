package com.intelligentrecorder

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RecorderViewModel : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isMoving = MutableStateFlow(false)
    val isMoving: StateFlow<Boolean> = _isMoving.asStateFlow()

    private val _threshold = MutableStateFlow(65f) // This works better with indoor lighting
    val threshold: StateFlow<Float> = _threshold.asStateFlow()
    private val videoBuffer = mutableListOf<Bitmap>()
    val bufferfilled = MutableStateFlow(false)

    private val _savedVideoUri = MutableStateFlow<String?>(null)
    val savedVideoUri: StateFlow<String?> = _savedVideoUri.asStateFlow()

    fun clearSavedUri() {
        _savedVideoUri.value = null
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

    fun updateMotion(isMoving: Boolean) {
        _isMoving.value = isMoving
    }

    fun addFrameToBuffer(image: Bitmap) {
        if (_isRecording.value == true) {
            videoBuffer.add(image)
        }
        if(bufferfilled.value!=true){
            bufferfilled.value = true;
        }
    }

    fun clearBuffer() {
        videoBuffer.clear()
        bufferfilled.value = false
    }

    fun convertFramesToVideo(
        framebuffer: List<Bitmap>,
        outputPath: String,
        fps: Int = 30,
        bitrate: Int = 4_000_000
    ){
        if(framebuffer.isEmpty()) return

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
        var presentationTimeUs = 0L
        val frameDurationUs = 1_000_000L / fps

        fun drainEncoder(endOfStream: Boolean) {
            if (endOfStream) codec.signalEndOfInputStream()
            while (true) {
                val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                when {
                    outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                    }
                    outIndex >= 0 -> {
                        val encodedData = codec.getOutputBuffer(outIndex)!!
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size > 0) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            bufferInfo.presentationTimeUs = presentationTimeUs
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                            presentationTimeUs += frameDurationUs
                        }
                        codec.releaseOutputBuffer(outIndex, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                    }
                    else -> if (!endOfStream) return else continue
                }
            }
        }

        framebuffer.forEach { bitmap ->
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
        muxer.stop()
        muxer.release()

        Log.d("Video Encoder","Video Encoded!")
    }

    fun saveVideo(context: Context): Boolean {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val fileName = "IntelligentRecorder_${System.currentTimeMillis()}.mp4"
                val tempFile = File(context.filesDir, fileName)

                convertFramesToVideo(
                    framebuffer = videoBuffer,
                    outputPath = tempFile.absolutePath
                )

                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, "output.mp4")
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                }
                val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                context.contentResolver.openOutputStream(uri!!)?.use { outputStream ->
                    tempFile.inputStream().copyTo(outputStream)
                }

                tempFile.delete()
                clearBuffer()
                _savedVideoUri.value = uri.toString()

            }
            return true
        } catch (e: Exception) {
            Log.e("RecorderViewModel", "Failed to save", e)
            return false
        }
    }
}
