package de.nicidienase.chaosflix.leanback

import android.os.Bundle
import android.support.v17.leanback.app.ErrorSupportFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar

class BrowseErrorFragment : ErrorSupportFragment() {
	private var spinnerFragment: SpinnerFragment? = null

	private var initialized: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val fragmentId = arguments?.getInt(FRAGMENT)
		if (fragmentId == null) {
			throw IllegalStateException("missing fragmentID")
		}
		with(SpinnerFragment()){
			spinnerFragment = this
			fragmentManager?.beginTransaction()?.add(fragmentId, this)?.commit()
		}
	}

	fun setErrorContent(resourceId: Int) {
		setErrorContent(resources.getString(resourceId))
	}

	fun setErrorContent(message: String) {
		imageDrawable = resources.getDrawable(R.drawable.lb_ic_sad_cloud, null)
		setMessage(message)
		setDefaultBackground(TRANSLUCENT)

		buttonText = resources.getString(R.string.dismiss_error)
		setButtonClickListener { _ -> dismiss() }
	}

	fun dismiss() {
		val fragmentManager = fragmentManager
		if (fragmentManager != null) {
			with(fragmentManager.beginTransaction()) {
				remove(this@BrowseErrorFragment)
				spinnerFragment?.let {
					remove(it)
				}
				commit()
	    		fragmentManager.popBackStack();
			}
		}
	}


	class SpinnerFragment : Fragment() {
		override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
			val progressBar = ProgressBar(container!!.context)
			if (container is FrameLayout) {
				val res = resources
				val width = res.getDimensionPixelSize(R.dimen.spinner_width)
				val height = res.getDimensionPixelSize(R.dimen.spinner_height)
				val layoutParams = FrameLayout.LayoutParams(width, height, Gravity.CENTER)
				progressBar.layoutParams = layoutParams
			}
			return progressBar
		}
	}

	companion object {

		private val TRANSLUCENT = true
		val FRAGMENT = "fragmentId"

		fun showErrorFragment(manager: FragmentManager, fragmentId: Int): BrowseErrorFragment {
			val errorFragment = BrowseErrorFragment()
			val args = Bundle()
			args.putInt(BrowseErrorFragment.FRAGMENT, fragmentId)
			errorFragment.arguments = args
			manager.beginTransaction().replace(fragmentId, errorFragment).addToBackStack(null).commit()
			return errorFragment
		}
	}
}
