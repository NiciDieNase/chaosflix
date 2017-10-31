package de.nicidienase.chaosflix.common.entities.userdata

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by felix on 04.10.17.
 */

@Dao
interface PlaybackProgressDao{
    @Query("SELECT * from playback_progress")
    fun getAll(): Flowable<List<PlaybackProgress>>

    @Query("SELECT * from playback_progress WHERE event_id = :id LIMIT 1")
    fun getProgressForEvent(id:Long): Flowable<PlaybackProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveProgress(progress: PlaybackProgress)
}