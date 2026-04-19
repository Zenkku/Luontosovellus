
// 📁 viewmodel/MapViewModel.kt
package com.example.luontopeli.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.location.LocationManager
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel karttanäkymälle (MapScreen).
 * Hallinnoi sijaintiseurantaa, reittipisteitä ja luontolöytöjen näyttämistä kartalla.
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager(application)
    private val db = AppDatabase.getDatabase(application)

    val routePoints: StateFlow<List<GeoPoint>> = locationManager.routePoints
    val currentLocation: StateFlow<Location?> = locationManager.currentLocation

    private val _natureSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
    val natureSpots: StateFlow<List<NatureSpot>> = _natureSpots.asStateFlow()

    init {
        // Seurataan kaikkia löytöjä (myös niitä joilla ei ole sijaintia, mutta suodatetaan ne myöhemmin)
        viewModelScope.launch {
            db.natureSpotDao().getAllSpots().collect { spots ->
                // Näytetään kartalla vain ne, joilla on oikea sijainti (ei 0.0, 0.0)
                _natureSpots.value = spots.filter { it.latitude != 0.0 && it.longitude != 0.0 }
            }
        }
    }

    fun startTracking() = locationManager.startTracking()
    fun stopTracking() = locationManager.stopTracking()
    fun resetRoute() = locationManager.resetRoute()

    override fun onCleared() {
        super.onCleared()
        locationManager.stopTracking()
    }
}

fun Long.toFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}
