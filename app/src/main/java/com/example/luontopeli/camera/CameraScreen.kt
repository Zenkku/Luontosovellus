
// 📁 camera/CameraScreen.kt
package com.example.luontopeli.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.viewmodel.CameraViewModel
import java.io.File

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember { ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val capturedImagePath by viewModel.capturedImagePath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val classificationResult by viewModel.classificationResult.collectAsState()
    val userComment by viewModel.userComment.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    if (!hasCameraPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text("Kameran lupa tarvitaan", modifier = Modifier.padding(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Myönnä lupa")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedImagePath == null) {
            // Kameran etsin
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Sijaintitiedon ilmaisin yläreunassa
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                color = if (currentLocation != null) Color(0xAA2E7D32) else Color(0xAAD32F2F),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (currentLocation != null) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (currentLocation != null) "GPS OK" else "Odotetaan GPS-signaalia...",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Kuvanottopainike
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(
                    onClick = { viewModel.takePhoto(context, imageCapture) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Camera, "Ota kuva", tint = Color.White)
                    }
                }
            }
        } else {
            // Kuvan esikatselu ja kirjauslomake
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black)) {
                    AsyncImage(
                        model = File(capturedImagePath!!),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Näytetään tunnistustulos (jos onnistui)
                    classificationResult?.let { result ->
                        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                            ClassificationResultCard(result)
                        }
                    }
                }

                // Kirjausosa: Kommentti ja tallennus
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large.copy(
                        bottomStart = CornerSize(0.dp), 
                        bottomEnd = CornerSize(0.dp)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = userComment,
                            onValueChange = { viewModel.onCommentChange(it) },
                            label = { Text("Kirjoita kommentti löydöstä") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("esim. Kaunis kukka metsän reunassa") },
                            maxLines = 3
                        )
                        
                        // Sijaintitieto tallennushetkellä
                        Text(
                            text = if (currentLocation != null) "✓ Sijainti tallennetaan" else "⚠ Sijaintia ei saatavilla",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentLocation != null) Color(0xFF2E7D32) else Color.Red,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.clearCapturedImage() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Uusi kuva")
                            }
                            Button(
                                onClick = { viewModel.saveCurrentSpot() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Save, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Tallenna")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassificationResultCard(result: ClassificationResult) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            when (result) {
                is ClassificationResult.Success -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tunnistettu: ${result.label}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        Text("${(result.confidence * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is ClassificationResult.NotNature -> Text("Ei tunnistettu luontokohteeksi")
                is ClassificationResult.Error -> Text("Tunnistusvirhe")
            }
        }
    }
}
