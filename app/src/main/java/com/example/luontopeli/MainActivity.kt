package com.example.luontopeli

import android.Manifest // KORJAUS: Lisätty oikea android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.luontopeli.ui.navigation.LuontopeliBottomBar
import com.example.luontopeli.ui.navigation.LuontopeliNavHost
import com.example.luontopeli.ui.theme.LuontopeliTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
// POISTETTU: java.util.jar.Manifest (tämä aiheutti "Unresolved reference" -virheet)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LuontopeliTheme {
                LuontopeliApp()
            }
        }
    }
}

@Composable
fun RequestActivityRecognitionPermission(
    onGranted: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onGranted()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            onGranted()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class) // TARVITAAN: Accompanist vaatii tämän
@Composable
fun LuontopeliApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // 1. Sijaintiluvat (Accompanist)
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 2. Askelmittarin lupa (ACTIVITY_RECOGNITION)
    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Lupa käsitelty */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    // 3. LUPATARKISTUS
    if (!permissionState.allPermissionsGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sijaintilupa tarvitaan karttaa varten",
                modifier = Modifier.padding(16.dp) // KORJAUS: Lisätty Modifier. alkuun
            )
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Myönnä luvat")
            }
        }
        return
    }

    // 4. VARSINAINEN SOVELLUS
    Scaffold(
        bottomBar = {
            LuontopeliBottomBar(navController = navController)
        }
    ) { innerPadding ->
        LuontopeliNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}