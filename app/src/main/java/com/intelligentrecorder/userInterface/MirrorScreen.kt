package com.intelligentrecorder.userInterface

import android.view.MotionEvent
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.intelligentrecorder.DetectionMode
import com.intelligentrecorder.MirrorViewModel

@Composable
fun MirrorModeIndicator(mode: DetectionMode) {
    val iconColor = when (mode) {
        DetectionMode.MIRROR -> Color.Cyan
        DetectionMode.FOREGROUND -> Color.Magenta
        DetectionMode.HYBRID -> Color.Yellow
    }
    
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, start = 20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Current Mode: ${mode.name}",
            modifier = Modifier.size(60.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (mode) {
                DetectionMode.MIRROR -> "Mirror"
                DetectionMode.FOREGROUND -> "Foreground"
                DetectionMode.HYBRID -> "Hybrid"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = iconColor,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MirrorCameraPreview(
    viewModel: MirrorViewModel,
    onPointAdded: (Float, Float) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val points by viewModel.mirrorPoints.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
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
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay canvas for dots and lines
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    if (event.action == MotionEvent.ACTION_DOWN && points.size < 4) {
                        onPointAdded(event.x, event.y)
                        true
                    } else {
                        false
                    }
                }
        ) {
            // Draw lines connecting points in rectangle pattern
            if (points.size >= 2) {
                // Draw lines between consecutive points
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = Color.Green,
                        start = Offset(points[i].x, points[i].y),
                        end = Offset(points[i + 1].x, points[i + 1].y),
                        strokeWidth = 4f
                    )
                }
            }
            
            // When we have exactly 4 points, complete the rectangle
            if (points.size == 4) {
                // Close the rectangle by connecting point 4 back to point 1
                drawLine(
                    color = Color.Green,
                    start = Offset(points[3].x, points[3].y),
                    end = Offset(points[0].x, points[0].y),
                    strokeWidth = 4f
                )
                
                // Fill the rectangle with semi-transparent overlay
                drawLine(
                    color = Color.Green.copy(alpha = 0.3f),
                    start = Offset(points[0].x, points[0].y),
                    end = Offset(points[2].x, points[2].y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.Green.copy(alpha = 0.3f),
                    start = Offset(points[1].x, points[1].y),
                    end = Offset(points[3].x, points[3].y),
                    strokeWidth = 2f
                )
            }
            
            // Draw dots with numbers
            points.forEachIndexed { index, point ->
                // Outer red circle
                drawCircle(
                    color = Color.Red,
                    radius = 20f,
                    center = Offset(point.x, point.y)
                )
                // Inner white circle
                drawCircle(
                    color = Color.White,
                    radius = 15f,
                    center = Offset(point.x, point.y)
                )
                // Number indicator (using smaller circle for now)
                drawCircle(
                    color = Color.Blue,
                    radius = 8f,
                    center = Offset(point.x, point.y)
                )
            }
        }
    }
}

@Composable
fun MirrorScreen(
    viewModel: MirrorViewModel,
    onConfirm: () -> Unit,
    onModeChange: (DetectionMode) -> Unit = {}
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val isComplete by viewModel.isConfigurationComplete.collectAsState()
    val points by viewModel.mirrorPoints.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview with touch handling
        MirrorCameraPreview(
            viewModel = viewModel,
            onPointAdded = { x, y ->
                viewModel.addPoint(x, y)
            }
        )
        
        // Top mode indicator (read-only, shows current mode)
        Column {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp)
            ) {
                val iconColor = when (selectedMode) {
                    DetectionMode.MIRROR -> Color.Cyan
                    DetectionMode.FOREGROUND -> Color.Magenta
                    DetectionMode.HYBRID -> Color.Yellow
                }
                
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Configuring for: ${selectedMode.name}",
                    modifier = Modifier.size(60.dp),
                    tint = iconColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configure Mirror Region",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Info text
            Text(
                text = if (points.size < 4) {
                    "Tap ${4 - points.size} more point(s) to define mirror region"
                } else {
                    "Mirror region defined. Press OK to confirm."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Clear button (only show if points exist)
            if (points.isNotEmpty()) {
                Button(
                    onClick = { viewModel.clearPoints() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Clear Points", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // OK button
            Button(
                onClick = onConfirm,
                enabled = isComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("OK", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
