// 📁 data/repository/NatureSpotRepository.kt
package com.example.luontopeli.data.repository

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.remote.firebase.StorageManager
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository-luokka luontolöytöjen hallintaan (Repository-suunnittelumalli).
 *
 * Toimii välittäjänä tietolähteiden (Room-tietokanta) ja ViewModelien välillä.
 * Offline-tilassa kaikki data tallennetaan ja haetaan paikallisesta Room-tietokannasta.
 * Firebase-managerit ovat no-op -toteutuksia, jotka eivät tee verkko-operaatioita.
 */
class NatureSpotRepository(
    private val dao: NatureSpotDao,
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val authManager: AuthManager
) {
    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()

    // Tallenna löytö: ensin Room, sitten Firebase
    suspend fun insertSpot(spot: NatureSpot) {
        val spotWithUser = spot.copy(userId = authManager.currentUserId)

        // 1. Tallenna paikallisesti HETI (toimii offline-tilassakin)
        dao.insert(spotWithUser.copy(synced = false))

        // 2. Yritä synkronoida Firebaseen
        syncSpotToFirebase(spotWithUser)
    }

    // Synkronoi yksittäinen kohde Firebaseen
    private suspend fun syncSpotToFirebase(spot: NatureSpot) {
        try {
            // 2a. Lataa kuva Storageen (jos paikallinen kuva olemassa)
            val firebaseImageUrl = spot.imageLocalPath?.let { localPath ->
                storageManager.uploadImage(localPath, spot.id).getOrNull()
            }

            // 2b. Tallenna metadata Firestoreen
            val spotWithUrl = spot.copy(imageFirebaseUrl = firebaseImageUrl)
            firestoreManager.saveSpot(spotWithUrl).getOrThrow()

            // 2c. Merkitse Room:ssa synkronoiduksi
            dao.markSynced(spot.id, firebaseImageUrl ?: "")
        } catch (e: Exception) {
            // Synkronointi epäonnistui – yritetään uudelleen myöhemmin
            // synced = false pysyy Room:ssa
        }
    }

    // Synkronoi kaikki odottavat kohteet (kutsutaan yhteyden palautuessa)
    suspend fun syncPendingSpots() {
        val unsyncedSpots = dao.getUnsyncedSpots()
        unsyncedSpots.forEach { spot ->
            syncSpotToFirebase(spot)
        }
    }
}

@Composable
fun NatureSpotItem(spot: NatureSpot) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // TÄHÄN TULEE PAIKALLINEN KUVA
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(spot.imageLocalPath!!))  // Polku tiedostoon
                    .crossfade(true)                    // Pehmeä häivytysanimaatio
                    .build(),
                contentDescription = spot.plantLabel ?: "Luontokuva",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

// Firebase Storage URL:sta (viikko 6)
            AsyncImage(
                model = spot.imageFirebaseUrl,
                contentDescription = "Luontokuva pilvestä",
                modifier = Modifier.size(80.dp).clip(CircleShape)
            )

            // Tähän alle voit laittaa löydön tekstitykset, kuten:
            // Text(text = spot.plantLabel ?: "Tuntematon laji")
        }
    }
}