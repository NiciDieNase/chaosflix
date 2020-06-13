package de.nicidienase.chaosflix.common.mediadata

import android.util.Log
import androidx.lifecycle.LiveData
import de.nicidienase.chaosflix.common.AnalyticsWrapper
import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.EventInfo
import de.nicidienase.chaosflix.common.mediadata.entities.eventinfo.EventInfoDao
import de.nicidienase.chaosflix.common.mediadata.network.EventInfoApi
import java.io.IOException
import java.util.Date
import javax.net.ssl.SSLHandshakeException

class EventInfoRepository(
    private val api: EventInfoApi,
    private val dao: EventInfoDao,
    private val analyticsWrapper: AnalyticsWrapper
) {

    suspend fun updateMediaInfo() {
        val eventInfoWrapper = withNetworkErrorHandling {
            return@withNetworkErrorHandling api.getVocEvents()
        }
        if (eventInfoWrapper != null) {
            val eventInfos = eventInfoWrapper.events.values.mapNotNull { EventInfo.fromVocEventDto(it) }
            dao.updateOrInsert(eventInfos)
        }
    }

    private suspend fun <T> withNetworkErrorHandling(block: suspend () -> T): T? {
        return try {
            block.invoke()
        } catch (e: SSLHandshakeException) {
            Log.e(TAG, e.message, e)
            analyticsWrapper.trackException(e)
            null
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            analyticsWrapper.trackException(e)
            null
        }
    }

    fun getEventInfo(): LiveData<List<EventInfo>> {
        return dao.findEventWithStartDateAfter(Date().time)
    }

    companion object {
        private val TAG = EventInfoRepository::class.java.simpleName
    }
}
