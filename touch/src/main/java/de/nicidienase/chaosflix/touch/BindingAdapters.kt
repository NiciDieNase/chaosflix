package de.nicidienase.chaosflix.touch

import androidx.databinding.BindingAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("bind:imageUrl")
fun loadImage(imageView: ImageView, url: String) {
    Glide.with(imageView.context)
            .load(url)
            .apply(RequestOptions().fitCenter())
            .into(imageView)
}

@BindingAdapter("bind:time")
fun setDuration(textView: TextView, duration: Long) {
    textView.text = String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60)
}