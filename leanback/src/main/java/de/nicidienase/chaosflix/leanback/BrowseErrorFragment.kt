package de.nicidienase.chaosflix.leanback

import android.os.Bundle
import android.support.v17.leanback.app.ErrorSupportFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar

class BrowseErrorFragment : ErrorSupportFragment() {
	private var spinnerFragment: SpinnerFragment? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val fragmentId = arguments?.getInt(FRAGMENT)
		if (fragmentId == null) {
			throw IllegalStateException("missing fragmentID")
		}
		spinnerFragment = SpinnerFragment()
		spinnerFragment?.let {
			fragmentManager?.beginTransaction()?.add(fragmentId, it)?.commit()
		}

	}

	fun setErrorContent(resourceId: Int) {
		setErrorContent(resources.getString(resourceId))
	}

	fun setErrorContent(message: String, fragmentManager: FragmentManager? = activity?.supportFragmentManager) {
		if(!isDetached){
			spinnerFragment?.let {
				fragmentManager?.beginTransaction()?.remove(it)?.commit()
			}
			imageDrawable = resources.getDrawable(R.drawable.lb_ic_sad_cloud, null)
			setMessage(message)
			setDefaultBackground(TRANSLUCENT)
			buttonText = resources.getString(R.string.dismiss_error)

			if(fragmentManager != null){
				setButtonClickListener { v -> dismiss(fragmentManager) }
			} else {
				setButtonClickListener { v -> dismiss() }
			}
		}
	}

	override fun onPause() {
		super.onPause()
		spinnerFragment?.let {
			fragmentManager?.beginTransaction()?.remove(it)?.commit()
		} ?: Log.e(TAG, "Could not remove spinnerFragment")
	}

	fun dismiss(fragmentManager: FragmentManager? = activity?.supportFragmentManager) {
		if (fragmentManager != null) {
			with(fragmentManager.beginTransaction()) {
				spinnerFragment?.let {
					remove(it)
				}
				remove(this@BrowseErrorFragment)
				commit()
	    		fragmentManager.popBackStack();
			}
		} else {
			Log.e(TAG,"Cannot dismiss, fragmentManager is null")
		}
	}


	class SpinnerFragment : Fragment() {
		override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
			return inflater.inflate(R.layout.loading_fragment, container, false)
		}
	}

	companion object {

		private val TRANSLUCENT = true
		val FRAGMENT = "fragmentId"
		val TAG = BrowseErrorFragment::class.java.simpleName

		fun showErrorFragment(manager: FragmentManager, fragmentId: Int, addToBackstack: Boolean = false): BrowseErrorFragment {
			val errorFragment = BrowseErrorFragment()
			val args = Bundle()
			args.putInt(BrowseErrorFragment.FRAGMENT, fragmentId)
			errorFragment.arguments = args
			if(addToBackstack){
				manager.beginTransaction().add(fragmentId, errorFragment).addToBackStack(null).commit()
			} else {
				manager.beginTransaction().add(fragmentId, errorFragment).commit()
			}
			return errorFragment
		}
	}
}
