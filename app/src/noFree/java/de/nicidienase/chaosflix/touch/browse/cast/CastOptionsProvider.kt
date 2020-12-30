package de.nicidienase.chaosflix.touch.browse.cast

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions
import de.nicidienase.chaosflix.ExpandedControllsActivity

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(p0: Context?): CastOptions {
        val notificationOptions = NotificationOptions.Builder()
                .setTargetActivityClassName(ExpandedControllsActivity::class.java.name)
                .build()
        val mediaOptions = CastMediaOptions.Builder()
                .setNotificationOptions(notificationOptions)
                .setExpandedControllerActivityClassName(ExpandedControllsActivity::class.java.name)
                .build()

        return CastOptions.Builder()
                .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .setCastMediaOptions(mediaOptions)
                .build()
    }

    override fun getAdditionalSessionProviders(p0: Context?): MutableList<SessionProvider> {
        return emptyList<SessionProvider>().toMutableList()
    }
}
