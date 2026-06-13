package com.intelligentrecorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intelligentrecorder.ui.theme.IntelligentRecorderTheme
import com.intelligentrecorder.userInterface.MainScreen

class MainActivity : ComponentActivity() {

    private var cameraPermissionGranted = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        cameraPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!cameraPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            IntelligentRecorderTheme {
                if (cameraPermissionGranted) {
                    val viewModel: RecorderViewModel = viewModel()
                    MainScreen(viewModel = viewModel)
                } else {
                    Text(
                        text = "Camera permission required",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}