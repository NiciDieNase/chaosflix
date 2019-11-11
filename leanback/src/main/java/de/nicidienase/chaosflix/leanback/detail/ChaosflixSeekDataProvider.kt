package de.nicidienase.chaosflix.leanback.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.os.Build
import android.support.v17.leanback.media.PlaybackGlue
import android.support.v17.leanback.widget.PlaybackSeekDataProvider
import android.util.Log
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChaosflixSeekDataProvider(
    val context: Context,
    val length: Long,
    val url: String
) : PlaybackSeekDataProvider() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var canceled = false
    private var generateThumbsJob: Job? = null

    private val mediaMetadataRetriever = MediaMetadataRetriever()

    private val thumbnails: MutableMap<Long, Bitmap> = mutableMapOf()
    private val dummyThumbnails: MutableMap<Long, Bitmap> = mutableMapOf()

    init {
        mediaMetadataRetriever.setDataSource(url, mapOf("User-Agent" to ApiFactory.buildUserAgent()))
        generateThumbs()
    }

    private val positions: LongArray by lazy {
        val list = mutableListOf<Long>()
        for (i in 0..(length * 1000) step calculateInterval(length)) {
            list.add(i)
        }
        list.add(length * 1000)
        Log.d(TAG, "Calculated ${list.size} steps")
        return@lazy list.toLongArray()
    }

    private fun calculateInterval(length: Long): Long {
        return 1000L * when (length) {
            in 0..60 -> 1 // 0..1min
            in 60..(1800) -> 30 // 1..30min
            in (30 * 60)..(90 * 60) -> 60 // 30..90min
            else -> 90
        }
    }

    override fun getSeekPositions(): LongArray {
        if (canceled) {
            mediaMetadataRetriever.setDataSource(url, mapOf("User-Agent" to ApiFactory.buildUserAgent()))
            generateThumbs()
        }
        return positions
    }

    private fun generateThumbs() {
        generateThumbsJob = scope.launch {
            for (i in positions.indices) {
                if (!thumbnails.containsKey(positions[i])) {
                    val dummyThumbnail = createDummyThumbnail(i)
                    dummyThumbnails[positions[i]] = dummyThumbnail
                }
            }
            Log.d(TAG, "Added Dummy-Thumbs")
            for (i in positions.indices) {
                if (!thumbnails.containsKey(positions[i])) {
                    val bitmap = createBitmapForIndex(i)
                    thumbnails[positions[i]] = bitmap
                }
            }
        }
    }

    override fun getThumbnail(index: Int, callback: ResultCallback?) =
            getThumbFromVideo(index, callback)

    private fun getThumbFromVideo(
        index: Int,
        callback: ResultCallback?
    ) {
        when {
            thumbnails.contains(positions[index]) -> {
                Log.d(TAG, "Thumbnail match ($index/${positions.size})")
                callback?.onThumbnailLoaded(thumbnails[positions[index]], index)
            }
            dummyThumbnails.contains(positions[index]) -> {
                callback?.onThumbnailLoaded(dummyThumbnails[positions[index]], index)
                scope.launch {
                    val thumb = createBitmapForIndex(index)
                    thumbnails[positions[index]] = thumb
                    withContext(Dispatchers.Main) {
                        callback?.onThumbnailLoaded(thumb, index)
                    }
                }
            }
            else -> scope.launch {
                val thumb = createDummyThumbnail(index)
                dummyThumbnails[positions[index]] = thumb
                withContext(Dispatchers.Main) {
                    callback?.onThumbnailLoaded(thumb, index)
                }
                scope.launch {
                    val thumb = createBitmapForIndex(index)
                    thumbnails[positions[index]] = thumb
                    withContext(Dispatchers.Main) {
                        callback?.onThumbnailLoaded(thumb, index)
                    }
                }
            }
        }
    }

    private fun createBitmapForIndex(index: Int): Bitmap {
        val startTime = System.currentTimeMillis()
        val pos = positions[index] * 1000
        val thumb: Bitmap =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                mediaMetadataRetriever.getScaledFrameAtTime(pos, MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    THUMB_WIDTH, THUMB_HEIGHT)
            } else {
                mediaMetadataRetriever.getFrameAtTime(pos, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } ?: Bitmap.createBitmap(THUMB_WIDTH, THUMB_HEIGHT, Bitmap.Config.ARGB_8888)
        Log.d(TAG, "Thumb size: ${thumb.width}x${thumb.height}")

        val seconds = positions[index] / 1000
        val time = String.format("%d:%02d", seconds / 60, seconds % 60)
        drawStringToBitmap(thumb, time)

        Log.d(TAG, "Adding Thumbnail ($index/${positions.size}) (took ${System.currentTimeMillis() - startTime}ms)")
        return thumb
    }

    private fun drawStringToBitmap(thumb: Bitmap, time: String) {
        val canvas = Canvas(thumb)
        val paint = Paint()

        val textHeight = (canvas.height / 5).toFloat()
        paint.textSize = textHeight
        paint.color = Color.parseColor("#DD444444")
        val textWidth = paint.measureText(time)
        canvas.drawRoundRect(
            0F,
            canvas.height - (textHeight),
            textWidth * 1.1F,
            canvas.height.toFloat(),
            textHeight * 0.2F,
            textHeight * 0.2F,
            paint
        )
        paint.color = Color.WHITE
        canvas.drawText(time,
            textHeight * 0.1F,
            canvas.height.toFloat() - (textHeight * 0.1F),
            paint)
    }

    private fun createDummyThumbnail(index: Int): Bitmap {
        val seconds = positions[index] / 1000
        val time = String.format("%d:%02d", seconds / 60, seconds % 60)
        val bitmap = Bitmap.createBitmap(THUMB_WIDTH, THUMB_HEIGHT, Bitmap.Config.ARGB_8888)

        drawStringToBitmap(bitmap, time)
        return bitmap
    }

    override fun reset() {
        Log.d(TAG, "SeekData reset")
        canceled = true
        GlobalScope.launch {
            generateThumbsJob?.cancelAndJoin()
            mediaMetadataRetriever.release()
            scope.cancel()
        }
        super.reset()
    }

    companion object {
        private val TAG = ChaosflixSeekDataProvider::class.java.simpleName

        private const val THUMB_WIDTH = 480
        private const val THUMB_HEIGHT = 270

        fun setSeekProvider(
            glue: ChaosMediaPlayerGlue,
            context: Context,
            length: Long,
            url: String
        ) {
            val chaosflixSeekDataProvider = ChaosflixSeekDataProvider(context, length, url)
            if (glue.isPrepared) {
                glue.seekProvider = chaosflixSeekDataProvider
                glue.isSeekEnabled = true
            } else {
                glue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                    override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                        if (glue?.isPrepared == true) {
                            glue.removePlayerCallback(this)
                            (glue as ChaosMediaPlayerGlue).seekProvider =
                                chaosflixSeekDataProvider
                            glue.isSeekEnabled = true
                        }
                    }
                })
            }
        }
    }
}