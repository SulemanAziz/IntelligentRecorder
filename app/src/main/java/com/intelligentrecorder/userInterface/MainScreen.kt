package com.intelligentrecorder.userInterface

import android.view.ViewGroup
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
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

@Composable
fun MainScreen(viewModel: RecorderViewModel){
    val context = LocalContext.current
    val isRecording by viewModel.isRecording.collectAsState()
    val isMoving by viewModel.isMoving.collectAsState()
    val threshold by viewModel.threshold.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(viewModel = viewModel)

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) {
            if(isMoving){
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Motion Detected",
                    modifier = Modifier.size(80.dp),
                    tint = Color.Yellow
                )
            }
            else{
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "No Motion",
                    modifier = Modifier.size(80.dp),
                    tint = Color.DarkGray
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(isRecording){
                StopRecordingButton(onClick = { viewModel.stopRecording() })
            }
            else{
                RecordButton(onClick = { viewModel.startRecording() })
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            SaveButton(onClick = { viewModel.saveToDownloads(context) })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            SettingsMenu(onClick = { showSettings = true })
            Spacer(Modifier.padding(vertical = 15.dp))
            DeleteVideoBufferButton(onClick = { viewModel.clearBuffer() })
        }
    }

    if (showSettings) {
        SettingsModal(
            currentThreshold = threshold,
            onThresholdChange = { viewModel.setThreshold(it) },
            onDismiss = { showSettings = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview(){
    // Preview disabled - requires ViewModel and camera
}