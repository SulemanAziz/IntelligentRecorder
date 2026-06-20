package com.intelligentrecorder.userInterface

import android.media.AudioManager
import android.media.MediaActionSound
import android.media.ToneGenerator
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import com.intelligentrecorder.DetectionMode
import com.intelligentrecorder.MirrorViewModel
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.intelligentrecorder.MotionDetection.MotionDetector
import com.intelligentrecorder.RecorderViewModel
import java.util.concurrent.Executors

@Composable
fun StopRecordingButton(onClick: () -> Unit){
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = 30.dp)
            .size(65.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        )
    ){

    }
}
@Composable
fun RecordButton(onClick: () -> Unit){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = 30.dp)
            .size(75.dp),
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Red)
        )
    }
}

@Composable
fun MirrorButton(onClick: () -> Unit){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(50.dp)
    ) {
        Icon(
            imageVector = Icons.Default.VideoSettings,
            tint = Color.White,
            contentDescription = "Configure Mirror Detection",
            modifier = Modifier
                .size(40.dp)
        )
    }
}

@Composable
fun SettingsMenu(onClick: () -> Unit){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(70.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            tint = Color.DarkGray,
            contentDescription = "Adjust Motion Detection Settings",
            modifier = Modifier
                .size(70.dp)
        )
    }
}

@Composable
fun DeleteVideoBufferButton(onClick: () -> Unit){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(70.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            tint = Color.Red,
            contentDescription = "Flush video buffer",
            modifier = Modifier
                .size(70.dp)
        )
    }
}

@Composable
fun SaveButton(onClick: () -> Unit){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(70.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Save,
            tint = Color.Green,
            contentDescription = "Save recording",
            modifier = Modifier
                .size(70.dp)
        )
    }
}

@Composable
fun CameraPreview(viewModel: RecorderViewModel) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also { previewView ->
                previewView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = CameraPreview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    val executor = Executors.newSingleThreadExecutor()
                    imageAnalysis.setAnalyzer(executor, MotionDetector(viewModel))
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeDropdownMenu(
    selectedMode: DetectionMode,
    onModeSelected: (DetectionMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        DetectionMode.FOREGROUND,
        DetectionMode.MIRROR,
        DetectionMode.HYBRID
    )
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = when(selectedMode) {
                DetectionMode.FOREGROUND -> "Foreground"
                DetectionMode.MIRROR -> "Mirror"
                DetectionMode.HYBRID -> "Hybrid"
            },
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            ),
            modifier = Modifier.menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            modes.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(
                        when(mode) {
                            DetectionMode.FOREGROUND -> "Foreground"
                            DetectionMode.MIRROR -> "Mirror"
                            DetectionMode.HYBRID -> "Hybrid"
                        }
                    )},
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: RecorderViewModel,
    mirrorViewModel: MirrorViewModel? = null,
    onNavigateToMirror: () -> Unit = {},
    onModeChange: ((DetectionMode) -> Unit)? = null
){
    val context = LocalContext.current
    val sound = remember { MediaActionSound() }
    val isRecording by viewModel.isRecording.collectAsState()
    val isMoving by viewModel.isMoving.collectAsState()
    val savedVideoUri by viewModel.savedVideoUri.collectAsState()
    val selectedMode by (mirrorViewModel?.selectedMode?.collectAsState() ?: remember { mutableStateOf(DetectionMode.FOREGROUND) })
    val mirrorPoints by (mirrorViewModel?.mirrorPoints?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    var showSettings by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Get the appropriate threshold based on mode
    val currentThreshold = when (selectedMode) {
        DetectionMode.FOREGROUND -> mirrorViewModel?.foregroundThreshold?.collectAsState()?.value ?: viewModel.threshold.collectAsState().value
        DetectionMode.MIRROR -> mirrorViewModel?.mirrorThreshold?.collectAsState()?.value ?: 65f
        DetectionMode.HYBRID -> mirrorViewModel?.hybridForegroundThreshold?.collectAsState()?.value ?: 65f
    }
    
    // Sync RecorderViewModel with mode-specific settings
    LaunchedEffect(selectedMode, currentThreshold, mirrorPoints) {
        viewModel.setMode(selectedMode)
        viewModel.setThreshold(currentThreshold)
        viewModel.setMirrorPoints(mirrorPoints)
    }

    LaunchedEffect(savedVideoUri) {
        savedVideoUri?.let {
            snackbarHostState.showSnackbar("Video saved to $it")
            viewModel.clearSavedUri()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            CameraPreview(viewModel = viewModel)

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 10.dp, end = 10.dp)
            ) {
                // Motion indicator on the left
                if(isMoving){
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Motion Detected",
                        modifier = Modifier.size(50.dp),
                        tint = Color.Yellow
                    )
                }
                else{
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "No Motion",
                        modifier = Modifier.size(50.dp),
                        tint = Color.DarkGray
                    )
                }
                
                // Mode dropdown in the middle
                if (onModeChange != null && mirrorViewModel != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        ModeDropdownMenu(
                            selectedMode = selectedMode,
                            onModeSelected = onModeChange
                        )
                    }
                }
                
                // Mirror button on the right (only in Mirror and Hybrid modes)
                if (selectedMode == DetectionMode.MIRROR || selectedMode == DetectionMode.HYBRID) {
                    MirrorButton(onClick = onNavigateToMirror)
                } else {
                    // Empty spacer to maintain layout when button is hidden
                    Spacer(modifier = Modifier.size(50.dp))
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(isRecording){
                StopRecordingButton(onClick = {
                    sound.play(MediaActionSound.STOP_VIDEO_RECORDING)
                    viewModel.stopRecording(context) })
            }
            else{
                RecordButton(onClick = {
                    sound.play(MediaActionSound.START_VIDEO_RECORDING);
                    viewModel.startRecording(context)}
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
             if(viewModel.bufferfilled.collectAsState().value == true && viewModel.isRecording.collectAsState().value == false) {
                 SaveButton(onClick = {
                     viewModel.saveVideo(context)
                 })
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            SettingsMenu(onClick = { showSettings = true })
            if(viewModel.isRecording.collectAsState().value == false && viewModel.bufferfilled.collectAsState().value == true) {
                Spacer(Modifier.padding(vertical = 15.dp))
                DeleteVideoBufferButton(onClick = {
                    viewModel.clearBuffer()
                    Toast.makeText(context, "Video Buffer Deleted!", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }

    if (showSettings) {
        when (selectedMode) {
            DetectionMode.FOREGROUND -> {
                SettingsModal(
                    currentThreshold = mirrorViewModel?.foregroundThreshold?.collectAsState()?.value ?: currentThreshold,
                    onThresholdChange = { 
                        mirrorViewModel?.setForegroundThreshold(it)
                        viewModel.setThreshold(it)
                    },
                    onDismiss = { showSettings = false }
                )
            }
            DetectionMode.MIRROR -> {
                MirrorSettingsModal(
                    currentThreshold = mirrorViewModel?.mirrorThreshold?.collectAsState()?.value ?: currentThreshold,
                    onThresholdChange = { 
                        mirrorViewModel?.setMirrorThreshold(it)
                        viewModel.setThreshold(it)
                    },
                    onDismiss = { showSettings = false }
                )
            }
            DetectionMode.HYBRID -> {
                HybridSettingsModal(
                    currentMirrorThreshold = mirrorViewModel?.hybridMirrorThreshold?.collectAsState()?.value ?: 65f,
                    currentForegroundThreshold = mirrorViewModel?.hybridForegroundThreshold?.collectAsState()?.value ?: 65f,
                    onMirrorThresholdChange = { mirrorViewModel?.setHybridMirrorThreshold(it) },
                    onForegroundThresholdChange = { 
                        mirrorViewModel?.setHybridForegroundThreshold(it)
                        viewModel.setThreshold(it)
                    },
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}
}