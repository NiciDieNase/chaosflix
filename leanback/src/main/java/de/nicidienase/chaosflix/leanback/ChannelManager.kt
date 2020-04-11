package de.nicidienase.chaosflix.leanback

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.tvprovider.media.tv.Channel
import androidx.tvprovider.media.tv.ChannelLogoUtils
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import de.nicidienase.chaosflix.common.ChaosflixPreferenceManager
import de.nicidienase.chaosflix.common.viewmodel.BrowseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChannelManager {

    enum class Channels {
        PROMOTED,
        
    }

    suspend fun setupChannels(context: Context, viewmodel: BrowseViewModel, prefs: ChaosflixPreferenceManager) {
        withContext(Dispatchers.IO) {
            if (prefs.channelId == 0L) {
                val builder = Channel.Builder()
                builder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                        .setDisplayName("Chaosflix")
                        .setAppLinkIntentUri(Uri.parse("de.nicidienase.chaosflix://main"))

                val channelUri: Uri? = context.contentResolver.insert(
                        TvContractCompat.Channels.CONTENT_URI,
                        builder.build().toContentValues()
                )
                channelUri?.let {
                    val channelId = ContentUris.parseId(channelUri)
                    prefs.channelId = channelId
                    val icon = context.resources.getDrawable(R.mipmap.ic_launcher).toBitmap(80, 80)

                    ChannelLogoUtils.storeChannelLogo(context, channelId, icon)
                    TvContractCompat.requestChannelBrowsable(context, channelId)
                }
            }

            val promotedEvents = viewmodel.getPromoted()
            val bookmarkedEvents = viewmodel.getBookmarks()

            val programmIds = promotedEvents.toMutableList().apply { addAll(bookmarkedEvents) }.map {
                val toContentValues = PreviewProgram.Builder()
                        .setChannelId(prefs.channelId)
                        .setType(TvContractCompat.PreviewPrograms.TYPE_EVENT)
                        .setTitle(it.title)
                        .setPosterArtUri(Uri.parse(it.thumbUrl))
                        .setDescription(it.description)
                        .setInternalProviderId(it.guid)
                        .setIntentUri(Uri.parse("de.nicidienase.chaosflix://event/${it.guid}"))
                        .build().toContentValues()
                val programUri = context.contentResolver.insert(TvContractCompat.PreviewPrograms.CONTENT_URI, toContentValues)
                ContentUris.parseId(programUri)
            }
            Log.d(TAG,"Added $programmIds")
        }
    }

    private val TAG = ChannelManager::class.java.simpleName
}