package de.nicidienase.chaosflix.common.entities.userdata

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable

/**
 * Created by felix on 04.10.17.
 */

@Dao
interface PlaybackProgressDao{
    @Query("SELECT * from playback_progress")
    fun getAll(): Flowable<List<PlaybackProgress>>

    @Query("SELECT * from playback_progress WHERE event_id = :arg0 LIMIT 1")
    fun getProgressForEvent(id:Int): Flowable<PlaybackProgress>

    @Insert
    fun saveProgress(progress: PlaybackProgress)
}