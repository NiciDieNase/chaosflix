package de.nicidienase.chaosflix.leanback.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v17.leanback.media.PlaybackGlue
import android.support.v17.leanback.widget.PlaybackSeekDataProvider

class ChaosflixSeekDataProvider(val context: Context, val length: Long, val thumb: String) :
    PlaybackSeekDataProvider() {

    private val positions: LongArray by lazy {
        listOf(0,length/4,length/2,3*length/4,length).toLongArray()
    }

    override fun getSeekPositions(): LongArray {
        return positions
    }

    override fun getThumbnail(index: Int, callback: ResultCallback?) {
        val bitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.textSize = 16F
        paint.color = Color.BLUE
        canvas.drawColor(Color.YELLOW)
        canvas.drawText(positions[index].toString()
            , 10F, 150F, paint)
        callback?.onThumbnailLoaded(bitmap, index)
    }

    companion object {
        fun setSeekProvider(
            glue: ChaosMediaPlayerGlue,
            context: Context,
            length: Long,
            thumb: String
        ) {
            if (glue.isPrepared) {
                glue.seekProvider = ChaosflixSeekDataProvider(context, length, thumb)
                glue.isSeekEnabled = true
            } else {
                glue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                    override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                        if (glue?.isPrepared == true) {
                            glue.removePlayerCallback(this)
                            (glue as ChaosMediaPlayerGlue).seekProvider =
                                ChaosflixSeekDataProvider(context, length, thumb)
                            glue.isSeekEnabled = true
                        }
                    }
                })
            }
        }
    }
}