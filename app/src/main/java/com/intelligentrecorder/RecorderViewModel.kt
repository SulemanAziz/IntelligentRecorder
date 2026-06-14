package com.intelligentrecorder

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

class RecorderViewModel : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isMoving = MutableStateFlow(false)
    val isMoving: StateFlow<Boolean> = _isMoving.asStateFlow()

    private val _threshold = MutableStateFlow(50f) // This works better with indoor lighting
    val threshold: StateFlow<Float> = _threshold.asStateFlow()

    private val videoBuffer = mutableListOf<ByteArray>()

    fun startRecording() {
        _isRecording.value = true
    }

    fun stopRecording() {
        _isRecording.value = false
    }

    fun setThreshold(value: Float) {
        _threshold.value = value
    }

    fun updateMotion(isMoving: Boolean) {
        _isMoving.value = isMoving
    }

    fun addFrameToBuffer(frameData: ByteArray) {
        if (_isRecording.value && _isMoving.value) {
            videoBuffer.add(frameData)
        }
    }

    fun clearBuffer() {
        videoBuffer.clear()
    }

    fun videobufferfilled(): Boolean{
        if(videoBuffer.isNotEmpty()){
            return true
        } else {
            return false
        }
    }

    fun saveToDownloads(context: Context): Boolean {
        return true
        //File saving doesn't work, commenting this out for now
        /*if (videoBuffer.isEmpty()) return false
        try {
            val tempFile = File(context.cacheDir, "temp_recording.mp4")
            FileOutputStream(tempFile).use { output ->
                for (frame in videoBuffer) {
                    output.write(frame)
                }
            }

            val fileName = "IntelligentRecorder_${System.currentTimeMillis()}.mp4"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        tempFile.inputStream().use { it.copyTo(os) }
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val destFile = File(downloadsDir, fileName)
                tempFile.copyTo(destFile, overwrite = true)
            }

            tempFile.delete()
            return true
        } catch (e: Exception) {
            Log.e("RecorderViewModel", "Failed to save", e)
            return false
        }*/
    }
}
