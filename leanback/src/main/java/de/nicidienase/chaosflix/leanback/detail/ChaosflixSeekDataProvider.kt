package de.nicidienase.chaosflix.leanback.detail

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.widget.PlaybackSeekDataProvider
import com.bumptech.glide.Glide
import de.nicidienase.chaosflix.common.mediadata.ThumbnailParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChaosflixSeekDataProvider(
    val context: Context,
    val length: Long,
    val thumbInfos: List<ThumbnailParser.ThumbnailInfo>
) : PlaybackSeekDataProvider() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val drawableMap: MutableMap<String, Bitmap> = mutableMapOf()
    private val thumbnails: MutableMap<Long, Bitmap> = mutableMapOf()

    private val positions: LongArray by lazy {
        (0..(length * 1000) step calculateInterval(length)).takeWhile { it < length * 1000 }.toLongArray()
    }

    private fun calculateInterval(length: Long): Long {
        return 1000L * when (length) {
            in 0..60 -> 1 // 0..1min
            in 60..(30 * 60) -> 10 // 1..30min
            in (30 * 60)..(90 * 60) -> 15 // 30..90min
            else -> 30
        }
    }

    override fun getSeekPositions(): LongArray {
        return positions
    }

    override fun getThumbnail(index: Int, callback: ResultCallback?) {
        val timestamp = positions[index]
        when {
            thumbnails.contains(timestamp) -> {
                Log.d(TAG, "Thumbnail match ($index/${positions.size})")
                callback?.onThumbnailLoaded(thumbnails[timestamp], index)
            }
            else -> scope.launch {
                try {
                    val thumbInfo = thumbInfos.first { it.startTime <= timestamp && timestamp <= it.endTime }
                    loadImageForUri(thumbInfo.thumb)?.let {
                        thumbnails[timestamp] = it
                        withContext(Dispatchers.Main) {
                            callback?.onThumbnailLoaded(it, index)
                        }
                    }
                } catch (e: NoSuchElementException) {
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    override fun reset() {
        thumbnails.clear()
        drawableMap.clear()
        Log.d(TAG, "SeekData reset Done")
        super.reset()
    }

    private suspend fun loadImageForUri(uri: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val imgUri = Uri.parse(uri)
                val sourceBitmap = drawableMap[imgUri.path] ?: Glide.with(context)
                        .asBitmap()
                        .load(imgUri)
//                    .submit()
                        .submit(960, 990)
                        .get()
                val params = imgUri.getQueryParameter("xywh")?.split(",")
                return@withContext if (params?.size == 4) {
                    val (x, y, w, h) = params.map { it.toInt() }
                    Bitmap.createBitmap(sourceBitmap, x, y, w, h)
                } else {
                    null
                }
            } catch (ex: RuntimeException) {
                Log.e(TAG, ex.message, ex)
                null
            }
        }
    }

    companion object {
        private val TAG = ChaosflixSeekDataProvider::class.java.simpleName

        fun setSeekProvider(
            glue: ChaosMediaPlayerGlue,
            context: Context,
            length: Long,
            thumbs: List<ThumbnailParser.ThumbnailInfo>
        ) {
            val chaosflixSeekDataProvider = ChaosflixSeekDataProvider(context, length, thumbs)
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
