package com.intelligentrecorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.intelligentrecorder.DetectionMode
import com.intelligentrecorder.MirrorViewModel
import com.intelligentrecorder.userInterface.MirrorScreen
import com.intelligentrecorder.userInterface.MirrorRecordingScreen
import com.intelligentrecorder.userInterface.HybridRecordingScreen
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.intelligentrecorder.ui.theme.IntelligentRecorderTheme
import com.intelligentrecorder.userInterface.MainScreen

class MainActivity : ComponentActivity() {
    private var cameraPermissionGranted by mutableStateOf(false)
    private var currentScreen by mutableStateOf("main")
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        cameraPermissionGranted = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            cameraPermissionGranted = true
        }

        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            IntelligentRecorderTheme {
                if (cameraPermissionGranted) {
                    val recorderViewModel = RecorderViewModel()
                    val mirrorViewModel = MirrorViewModel()
                    
                    when (currentScreen) {
                        "main" -> MainScreen(
                            viewModel = recorderViewModel,
                            mirrorViewModel = mirrorViewModel,
                            onNavigateToMirror = {
                                currentScreen = "mirror_config"
                            },
                            onModeChange = { mode ->
                                mirrorViewModel.setMode(mode)
                                currentScreen = when (mode) {
                                    DetectionMode.MIRROR -> "mirror_recording"
                                    DetectionMode.FOREGROUND -> "foreground_recording"
                                    DetectionMode.HYBRID -> "hybrid_recording"
                                }
                            }
                        )
                        "mirror_config" -> MirrorScreen(
                            viewModel = mirrorViewModel,
                            onConfirm = {
                                // Return to the same mode screen they came from
                                currentScreen = when (mirrorViewModel.selectedMode.value) {
                                    DetectionMode.MIRROR -> "mirror_recording"
                                    DetectionMode.FOREGROUND -> "foreground_recording"
                                    DetectionMode.HYBRID -> "hybrid_recording"
                                }
                            }
                        )
                        "mirror_recording" -> MirrorRecordingScreen(
                            viewModel = recorderViewModel,
                            mirrorViewModel = mirrorViewModel,
                            onNavigateToMirror = {
                                currentScreen = "mirror_config"
                            },
                            onModeChange = { mode ->
                                mirrorViewModel.setMode(mode)
                                currentScreen = when (mode) {
                                    DetectionMode.MIRROR -> "mirror_recording"
                                    DetectionMode.FOREGROUND -> "foreground_recording"
                                    DetectionMode.HYBRID -> "hybrid_recording"
                                }
                            }
                        )
                        "foreground_recording" -> MainScreen(
                            viewModel = recorderViewModel,
                            mirrorViewModel = mirrorViewModel,
                            onNavigateToMirror = {
                                currentScreen = "mirror_config"
                            },
                            onModeChange = { mode ->
                                mirrorViewModel.setMode(mode)
                                currentScreen = when (mode) {
                                    DetectionMode.MIRROR -> "mirror_recording"
                                    DetectionMode.FOREGROUND -> "foreground_recording"
                                    DetectionMode.HYBRID -> "hybrid_recording"
                                }
                            }
                        )
                        "hybrid_recording" -> HybridRecordingScreen(
                            viewModel = recorderViewModel,
                            mirrorViewModel = mirrorViewModel,
                            onNavigateToMirror = {
                                currentScreen = "mirror_config"
                            },
                            onModeChange = { mode ->
                                mirrorViewModel.setMode(mode)
                                currentScreen = when (mode) {
                                    DetectionMode.MIRROR -> "mirror_recording"
                                    DetectionMode.FOREGROUND -> "foreground_recording"
                                    DetectionMode.HYBRID -> "hybrid_recording"
                                }
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera permission required",
                            modifier = Modifier.size(100.dp)
                        )
                    }

                }
            }
        }
    }
}