package de.nicidienase.chaosflix

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView

class ChaosflixLoadingSpinner(context: Context, attributeSet: AttributeSet) : ImageView(context, attributeSet) {
    init {
        val typedArray = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ChaosflixLoadingSpinner, 0, 0)
        val duration = typedArray.getInt(R.styleable.ChaosflixLoadingSpinner_duration, 2000)
        val clockwise = typedArray.getBoolean(R.styleable.ChaosflixLoadingSpinner_clockwise, true)

        val anim = if (clockwise) {
	        RotateAnimation(0.0f, 360.0f,
			        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        } else {
	        RotateAnimation(360.0f, 0.0f,
			        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        }
        anim.interpolator = LinearInterpolator()
        anim.duration = duration.toLong()
        anim.repeatCount = Animation.INFINITE
        animation = anim
    }
}