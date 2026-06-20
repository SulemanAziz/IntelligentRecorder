package com.intelligentrecorder.userInterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intelligentrecorder.DetectionMode
import com.intelligentrecorder.MirrorViewModel
import com.intelligentrecorder.RecorderViewModel
import kotlin.math.roundToInt

@Composable
fun HybridSettingsModal(
    currentMirrorThreshold: Float,
    currentForegroundThreshold: Float,
    onMirrorThresholdChange: (Float) -> Unit,
    onForegroundThresholdChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var mirrorSliderValue by remember { mutableFloatStateOf(currentMirrorThreshold) }
    var foregroundSliderValue by remember { mutableFloatStateOf(currentForegroundThreshold) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Hybrid Detection Settings")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mirror Region Sensitivity: ${mirrorSliderValue.roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = mirrorSliderValue,
                    onValueChange = { mirrorSliderValue = it },
                    valueRange = 0f..100f,
                    steps = 99,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Foreground Sensitivity: ${foregroundSliderValue.roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = foregroundSliderValue,
                    onValueChange = { foregroundSliderValue = it },
                    valueRange = 0f..100f,
                    steps = 99,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Adjust both mirror region and foreground motion detection thresholds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onMirrorThresholdChange(mirrorSliderValue)
                    onForegroundThresholdChange(foregroundSliderValue)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HybridRecordingScreen(
    viewModel: RecorderViewModel,
    mirrorViewModel: MirrorViewModel,
    onNavigateToMirror: () -> Unit = {},
    onModeChange: (DetectionMode) -> Unit
) {
    MainScreen(
        viewModel = viewModel,
        mirrorViewModel = mirrorViewModel,
        onNavigateToMirror = onNavigateToMirror,
        onModeChange = onModeChange
    )
}
