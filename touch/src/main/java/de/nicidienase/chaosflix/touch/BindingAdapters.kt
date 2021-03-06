package de.nicidienase.chaosflix.touch

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event

@BindingAdapter("imageUrl")
fun loadImage(imageView: ImageView, url: String?) {
    if (url == null) return
    Glide.with(imageView.context)
            .load(url)
            .apply(RequestOptions().fitCenter())
            .into(imageView)
}

@BindingAdapter("time")
fun setDuration(textView: TextView, duration: Long) {
    textView.text = String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60)
}

@BindingAdapter("progress")
fun ProgressBar.eventProgress(event: Event) {
    val progress = event.progress
    if (progress > 0) {
        this.visibility = View.VISIBLE
        this.max = event.length.toInt()
        this.progress = (progress / 1000).toInt()
    } else {
        this.visibility = View.GONE
    }
}
