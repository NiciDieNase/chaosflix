package de.nicidienase.chaosflix.leanback.detail

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import de.nicidienase.chaosflix.common.mediadata.entities.streaming.Room
import de.nicidienase.chaosflix.leanback.databinding.DetailViewBinding

class EventDetailsDescriptionPresenter(private val context: Context) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val binding = DetailViewBinding.inflate(LayoutInflater.from(context))
        return DescriptionViewHolder(binding.root, binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        if (viewHolder !is DescriptionViewHolder) { context
            throw IllegalStateException("Wrong ViewHolder")
        }
        viewHolder.binding.item = when (item) {
            is Event -> {
                val sb = StringBuilder()
                val speaker = TextUtils.join(", ", item.persons ?: emptyArray())
                sb.append(item.description)
                        .append("\n")
                        .append("\nreleased at: ").append(item.releaseDate)
                        .append("\nTags: ").append(TextUtils.join(", ", item.tags!!))
                DetailDataHolder(item.title,
                        item.subtitle,
                        speaker,
                        sb.toString())
            }
            is Room -> {

                val currentTalk = item.talks?.get("current")
                val nextTalk = item.talks?.get("next")
                DetailDataHolder(title = item.display,
                        subtitle = item.schedulename,
                        speakers = currentTalk?.description ?: "",
                        description = "Next Talk: ${nextTalk?.description ?: "no talk scheduled"}")
            }
            else -> {
                Log.e(TAG, "Item is neither PersistentEvent nor Room, this should not be happening")
                DetailDataHolder("", "", "", "")
            }
        }
    }

    inner class DetailDataHolder internal constructor(val title: String, val subtitle: String?, val speakers: String, val description: String) {

        internal constructor(event: Event) : this(
                event.title,
                event.subtitle,
                TextUtils.join(", ", event.persons!!),
                StringBuilder().append(event.description)
                        .append("\n")
                        .append("\nreleased at: ").append(event.releaseDate)
                        .append("\nTags: ")
                        .append(android.text.TextUtils.join(", ", event.tags!!))
                        .toString())

        internal constructor(room: Room) : this(
                room.display,
                room.schedulename,
                "",
                "")
    }

    inner class DescriptionViewHolder(view: View, val binding: DetailViewBinding) : ViewHolder(view)

    override fun onUnbindViewHolder(vh: ViewHolder) {}

    companion object {
        private val TAG = EventDetailsDescriptionPresenter::class.java.simpleName
    }
}
