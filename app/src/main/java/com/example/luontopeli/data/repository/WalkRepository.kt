// 📁 data/repository/WalkRepository.kt
package com.example.luontopeli.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository-luokka kävelykertojen hallintaan (Repository-suunnittelumalli).
 *
 * Toimii välittäjänä Room-tietokannan (WalkSessionDao) ja ViewModelien välillä.
 * Tarjoaa yksinkertaisen rajapinnan kävelylenkkien CRUD-operaatioihin.
 *
 * @param dao WalkSessionDao tietokantaoperaatioihin
 */
class WalkRepository(private val dao: WalkSessionDao) {

    /** Flow-virta kaikista kävelykerroista aikajärjestyksessä (uusin ensin) */
    val allSessions: Flow<List<WalkSession>> = dao.getAllSessions()

    /**
     * Data Access Object (DAO) kävelysessioiden tietokantaoperaatioille.
     */
    @Dao
    interface WalkSessionDao {

        /**
         * Hakee kaikki tallennetut kävelykerrat.
         * Palauttaa Flow-virran, joka päivittyy automaattisesti, kun tietokanta muuttuu.
         */
        @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
        fun getAllSessions(): Flow<List<WalkSession>>

        /**
         * Tallentaa uuden kävelykerran. Jos ID on jo olemassa, se korvataan.
         */
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(session: WalkSession)

        /**
         * Päivittää olemassa olevan kävelykerran tiedot (esim. askeleet ja matkan).
         */
        @Update
        suspend fun update(session: WalkSession)

        /**
         * Hakee aktiivisen kävelykerran (sessio, jota ei ole vielä päätetty).
         * Oletetaan, että WalkSession-entiteetissä on sarake 'isActive'.
         */
        @Query("SELECT * FROM walk_sessions WHERE isActive = 1 LIMIT 1")
        suspend fun getActiveSession(): WalkSession?

        /**
         * Vaihtoehtoinen haku aktiiviselle sessiolle, jos käytät endTimea (null = käynnissä).
         * Jos yllä oleva 'isActive' ei toimi, kokeile tätä:
         * @Query("SELECT * FROM walk_sessions WHERE endTime IS NULL LIMIT 1")
         * suspend fun getActiveSessionByTime(): WalkSession?
         */
    }

    /**
     * Tallentaa uuden kävelykerran tietokantaan.
     * Kutsutaan kun kävelylenkki lopetetaan.
     * @param session Tallennettava kävelykerta
     */
    suspend fun insertSession(session: WalkSession) {
        dao.insert(session)
    }

    /**
     * Päivittää olemassa olevan kävelykerran tiedot.
     * Käytetään esim. askelten tai matkan päivittämiseen kävelyn aikana.
     * @param session Päivitettävä kävelykerta
     */
    suspend fun updateSession(session: WalkSession) {
        dao.update(session)
    }

    /**
     * Hakee parhaillaan aktiivisen kävelykerran.
     * @return Aktiivinen kävelykerta tai null
     */
    suspend fun getActiveSession(): WalkSession? {
        return dao.getActiveSession()
    }
}