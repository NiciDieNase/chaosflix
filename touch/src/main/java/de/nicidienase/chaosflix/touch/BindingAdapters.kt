package de.nicidienase.chaosflix.touch

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso


@BindingAdapter("bind:imageUrl")
fun loadImage(imageView: ImageView, url: String){
	Picasso.with(imageView.context)
			.load(url)
			.fit()
			.centerInside()
			.into(imageView)
}