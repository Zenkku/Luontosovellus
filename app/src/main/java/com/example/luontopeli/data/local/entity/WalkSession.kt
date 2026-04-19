package com.example.luontopeli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// WalkSession tallentaa yhden kävelylenkkin tiedot
@Entity(tableName = "walk_sessions")
data class WalkSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val stepCount: Int = 0,
    val distanceMeters: Float = 0f,  // Laskettu: stepCount * STEP_LENGTH_METERS
    val spotsFound: Int = 0,         // Tällä kävelylenkillä löydetyt luontokohteet
    val isActive: Boolean = true
)