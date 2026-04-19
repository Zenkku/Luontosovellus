
package com.example.luontopeli.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.luontopeli.data.remote.firebase.AuthManager

@Composable
fun ProfileScreen() {
    val authManager = remember { AuthManager() }
    val userId = authManager.currentUserId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Oma Profiili",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Profiilikuva-ikoni
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Käyttäjätiedot
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Käyttäjä-ID", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(userId, style = MaterialTheme.typography.bodyLarge)
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                Text("Tila", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text("Anonyymi käyttäjä", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Asetukset-painike (esimerkki)
        OutlinedButton(
            onClick = { /* Asetukset */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Settings, null)
            Spacer(Modifier.width(8.dp))
            Text("Asetukset")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Kirjaudu ulos -painike
        Button(
            onClick = { authManager.signOut() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("Kirjaudu ulos")
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            "Versio 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
