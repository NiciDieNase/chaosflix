package de.nicidienase.chaosflix.common.userdata.entities.progress

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PlaybackProgressDao {

//    @Query("""SELECT event.*, p.progress, p.watch_date, conference.acronym as conference
//        FROM playback_progress as p
//        JOIN event ON event_guid = event.guid
//        JOIN conference ON event.conferenceId = conference.id
//        ORDER BY p.watch_date DESC""")
//    fun getInProgessEvents(): LiveData<List<ProgressEventView>>

    @Query("SELECT * FROM playback_progress")
    fun getAll(): LiveData<List<PlaybackProgress>>

    @Transaction
    @Query("SELECT * FROM playback_progress ORDER BY watch_date DESC")
    fun getAllWithEvent(): LiveData<List<ProgressEventView>>

    @Transaction
    @Query("SELECT * FROM playback_progress ORDER BY watch_date DESC")
    suspend fun getAllWithEventSync(): List<ProgressEventView>

    @Query("SELECT * FROM playback_progress")
    fun getAllSync(): List<PlaybackProgress>

    @Query("SELECT * FROM playback_progress WHERE event_guid = :guid LIMIT 1")
    fun getProgressForEvent(guid: String): LiveData<PlaybackProgress?>

    @Query("SELECT * FROM playback_progress WHERE event_guid = :guid LIMIT 1")
    suspend fun getProgressForEventSync(guid: String): PlaybackProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProgress(progress: PlaybackProgress): Long

    @Query("DELETE from playback_progress WHERE event_guid = :guid")
    fun deleteItem(guid: String)
}
