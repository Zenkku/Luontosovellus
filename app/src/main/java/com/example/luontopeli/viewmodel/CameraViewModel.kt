
package com.example.luontopeli.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.ml.PlantClassifier
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.remote.firebase.StorageManager
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel kameranäkymälle (CameraScreen).
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val classifier = PlantClassifier()
    private val locationManager = LocationManager(application)

    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult.asStateFlow()

    private val db = AppDatabase.getDatabase(application)

    private val repository = NatureSpotRepository(
        dao = db.natureSpotDao(),
        firestoreManager = FirestoreManager(),
        storageManager = StorageManager(),
        authManager = AuthManager()
    )

    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userComment = MutableStateFlow("")
    val userComment: StateFlow<String> = _userComment.asStateFlow()

    /** Seurataan sijaintia suoraan LocationManagerista */
    val currentLocation = locationManager.currentLocation

    init {
        // Aloitetaan sijaintiseuranta kameran ajaksi
        locationManager.startTracking()
    }

    fun onCommentChange(newComment: String) {
        _userComment.value = newComment
    }

    fun takePhoto(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputDir = File(context.filesDir, "nature_photos").also { it.mkdirs() }
        val outputFile = File(outputDir, "IMG_${timestamp}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _capturedImagePath.value = outputFile.absolutePath

                    viewModelScope.launch {
                        try {
                            val uri = Uri.fromFile(outputFile)
                            val result = classifier.classify(uri, context)
                            _classificationResult.value = result
                        } catch (e: Exception) {
                            _classificationResult.value =
                                ClassificationResult.Error(e.message ?: "Tuntematon virhe")
                        }
                        _isLoading.value = false
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _isLoading.value = false
                }
            }
        )
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        val location = currentLocation.value
        
        viewModelScope.launch {
            val result = _classificationResult.value

            val spot = NatureSpot(
                name = when (result) {
                    is ClassificationResult.Success -> result.label
                    else -> "Luontolöytö"
                },
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                imageLocalPath = imagePath,
                plantLabel = (result as? ClassificationResult.Success)?.label,
                confidence = (result as? ClassificationResult.Success)?.confidence,
                comment = _userComment.value.ifBlank { null }
            )
            repository.insertSpot(spot)
            clearCapturedImage()
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
        locationManager.stopTracking()
    }

    fun clearCapturedImage() {
        _capturedImagePath.value = null
        _classificationResult.value = null
        _userComment.value = ""
    }
}
