package com.intelligentrecorder

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DetectionMode {
    MIRROR,
    FOREGROUND,
    HYBRID
}

data class MirrorPoint(
    val x: Float,
    val y: Float
)

class MirrorViewModel : ViewModel() {
    
    private val _mirrorPoints = MutableStateFlow<List<MirrorPoint>>(emptyList())
    val mirrorPoints: StateFlow<List<MirrorPoint>> = _mirrorPoints.asStateFlow()
    
    private val _selectedMode = MutableStateFlow(DetectionMode.MIRROR)
    val selectedMode: StateFlow<DetectionMode> = _selectedMode.asStateFlow()
    
    private val _isConfigurationComplete = MutableStateFlow(false)
    val isConfigurationComplete: StateFlow<Boolean> = _isConfigurationComplete.asStateFlow()
    
    // Separate thresholds for each mode
    private val _mirrorThreshold = MutableStateFlow(65f)
    val mirrorThreshold: StateFlow<Float> = _mirrorThreshold.asStateFlow()
    
    private val _foregroundThreshold = MutableStateFlow(65f)
    val foregroundThreshold: StateFlow<Float> = _foregroundThreshold.asStateFlow()
    
    private val _hybridMirrorThreshold = MutableStateFlow(65f)
    val hybridMirrorThreshold: StateFlow<Float> = _hybridMirrorThreshold.asStateFlow()
    
    private val _hybridForegroundThreshold = MutableStateFlow(65f)
    val hybridForegroundThreshold: StateFlow<Float> = _hybridForegroundThreshold.asStateFlow()
    
    fun addPoint(x: Float, y: Float) {
        if (_mirrorPoints.value.size < 4) {
            val newPoint = MirrorPoint(x, y)
            _mirrorPoints.value = _mirrorPoints.value + newPoint
            updateConfigurationStatus()
        }
    }
    
    fun clearPoints() {
        _mirrorPoints.value = emptyList()
        updateConfigurationStatus()
    }
    
    fun setMode(mode: DetectionMode) {
        _selectedMode.value = mode
    }
    
    fun setMirrorThreshold(value: Float) {
        _mirrorThreshold.value = value
    }
    
    fun setForegroundThreshold(value: Float) {
        _foregroundThreshold.value = value
    }
    
    fun setHybridMirrorThreshold(value: Float) {
        _hybridMirrorThreshold.value = value
    }
    
    fun setHybridForegroundThreshold(value: Float) {
        _hybridForegroundThreshold.value = value
    }
    
    private fun updateConfigurationStatus() {
        _isConfigurationComplete.value = _mirrorPoints.value.size == 4
    }
    
    fun getModeDisplayName(mode: DetectionMode): String {
        return when (mode) {
            DetectionMode.MIRROR -> "Mirror"
            DetectionMode.FOREGROUND -> "Foreground"
            DetectionMode.HYBRID -> "Hybrid"
        }
    }
}
