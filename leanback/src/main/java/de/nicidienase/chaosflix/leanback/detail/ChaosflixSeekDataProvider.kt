package de.nicidienase.chaosflix.leanback.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.widget.PlaybackSeekDataProvider
import de.nicidienase.chaosflix.common.AnalyticsWrapper
import de.nicidienase.chaosflix.common.AnalyticsWrapperImpl
import de.nicidienase.chaosflix.common.mediadata.network.ApiFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class ChaosflixSeekDataProvider(
    val context: Context,
    val length: Long,
    val url: String
) : PlaybackSeekDataProvider() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var canceled = true
    private var generateThumbsJob: Job? = null

    private var mediaMetadataRetriever = MediaMetadataRetriever()

    private val thumbnails: MutableMap<Long, Bitmap> = mutableMapOf()
    private val dummyThumbnails: MutableMap<Long, Bitmap> = mutableMapOf()

    private val calcTimes: MutableList<Long> = mutableListOf()

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
            in 60..(30 * 60) -> 10 // 1..30min
            in (30 * 60)..(90 * 60) -> 15 // 30..90min
            else -> 30
        }
    }

    fun initialize() {
        if (canceled) {
            canceled = false
            Log.d(TAG, "Retriever was canceled before, reinitializing")
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(url, mapOf("User-Agent" to ApiFactory.buildUserAgent()))
            generateThumbs()
        }
    }

    override fun getSeekPositions(): LongArray {
        initialize()
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
            yield()
            for (i in positions.indices) {
                if (!thumbnails.containsKey(positions[i])) {
                    val bitmap = createBitmapForIndex(i)
                    if (bitmap != null) {
                        thumbnails[positions[i]] = bitmap
                    }
                    yield()
                }
            }
        }
    }

    override fun getThumbnail(index: Int, callback: ResultCallback?) {
        when {
            thumbnails.contains(positions[index]) -> {
                Log.d(TAG, "Thumbnail match ($index/${positions.size})")
                callback?.onThumbnailLoaded(thumbnails[positions[index]], index)
            }
            dummyThumbnails.contains(positions[index]) -> {
                callback?.onThumbnailLoaded(dummyThumbnails[positions[index]], index)
                scope.launch {
                    val thumb = createBitmapForIndex(index)
                    if (thumb != null) {
                        thumbnails[positions[index]] = thumb
                        withContext(Dispatchers.Main) {
                            callback?.onThumbnailLoaded(thumb, index)
                        }
                    }
                }
            }
            else -> scope.launch {
                val dummyThumb = createDummyThumbnail(index)
                dummyThumbnails[positions[index]] = dummyThumb
                withContext(Dispatchers.Main) {
                    callback?.onThumbnailLoaded(dummyThumb, index)
                }
                scope.launch {
                    val thumb = createBitmapForIndex(index)
                    if (thumb != null) {
                        thumbnails[positions[index]] = thumb
                        withContext(Dispatchers.Main) {
                            callback?.onThumbnailLoaded(thumb, index)
                        }
                    }
                }
            }
        }
    }

    private fun createBitmapForIndex(index: Int): Bitmap? {
        if (canceled) {
            return null
        }
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

        val duration = System.currentTimeMillis() - startTime
        calcTimes.add(duration)
        Log.d(TAG, "Adding Thumbnail ($index/${positions.size}) (took ${duration}ms)")
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
        Log.d(TAG, "SeekData reset Started")
        canceled = true
        GlobalScope.launch {
            generateThumbsJob?.cancelAndJoin()
            mediaMetadataRetriever.release()
        }
        AnalyticsWrapperImpl.addAnalyticsEvent(AnalyticsWrapper.thumbnailsStatEvent,
                mapOf(
                        "avg" to calcTimes.toLongArray().average().toLong().toString(),
                        "positions_count" to positions.size.toString(),
                        "calculated_count" to calcTimes.size.toString()
                )
        )
        calcTimes.clear()
        thumbnails.clear()
        dummyThumbnails.clear()
        Log.d(TAG, "SeekData reset Done")
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
                chaosflixSeekDataProvider.initialize()
            } else {
                glue.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                    override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                        if (glue?.isPrepared == true) {
                            glue.removePlayerCallback(this)
                            (glue as ChaosMediaPlayerGlue).seekProvider =
                                chaosflixSeekDataProvider
                            glue.isSeekEnabled = true
                            chaosflixSeekDataProvider.initialize()
                        }
                    }
                })
            }
        }
    }
}
