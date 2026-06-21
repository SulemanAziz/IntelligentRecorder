package com.intelligentrecorder.userInterface

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intelligentrecorder.DetectionMode
import com.intelligentrecorder.MirrorViewModel
import com.intelligentrecorder.RecorderViewModel
import kotlin.math.roundToInt

@Composable
fun MirrorSettingsModal(
    currentThreshold: Float,
    onThresholdChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(currentThreshold) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Mirror Detection Settings")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mirror Region Sensitivity: ${sliderValue.roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pixels that must change in mirror region to trigger motion detection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..100f,
                    steps = 99,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onThresholdChange(sliderValue)
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
fun MirrorRecordingScreen(
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
