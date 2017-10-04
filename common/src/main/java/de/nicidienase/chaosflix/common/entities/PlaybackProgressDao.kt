package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

/**
 * Created by felix on 04.10.17.
 */

@Dao
interface PlaybackProgressDao{
    @Query("SELECT * from playback_progress")
    fun getAll(): List<PlaybackProgress>

    @Query("SELECT * from playback_progress WHERE id = (:id)")
    fun getProgressForEvent(id:Int):PlaybackProgress

    @Query("DELETE from playback_progress WHERE id = (:id)")
    fun deleteProgress(id:Int)
}