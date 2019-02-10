package de.nicidienase.chaosflix.touch

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


@BindingAdapter("bind:imageUrl")
fun loadImage(imageView: ImageView, url: String){
	Glide.with(imageView.context)
			.load(url)
			.apply(RequestOptions().fitCenter())
			.into(imageView)
}