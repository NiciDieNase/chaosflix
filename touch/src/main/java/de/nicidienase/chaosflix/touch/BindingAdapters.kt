package de.nicidienase.chaosflix.touch

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("imageUrl")
fun loadImage(imageView: ImageView, url: String?) {
    url?.let {
        Glide.with(imageView.context)
            .load(it)
            .apply(RequestOptions().fitCenter())
            .into(imageView)
    }
}

@BindingAdapter("time")
fun setDuration(textView: TextView, duration: Long) {
    textView.text = String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60)
}
