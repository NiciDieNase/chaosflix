package de.nicidienase.chaosflix.touch

import android.content.Context
import android.util.AttributeSet
import android.view.animation.*
import android.widget.ImageView

class ChaosflixLoadingSpinner(context: Context, attributeSet: AttributeSet): ImageView(context,attributeSet){
	init {
		val anim = RotateAnimation(360.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
		anim.interpolator = AnticipateInterpolator()
		anim.duration = 1000
		anim.repeatCount = Animation.INFINITE
		animation = anim
	}
}