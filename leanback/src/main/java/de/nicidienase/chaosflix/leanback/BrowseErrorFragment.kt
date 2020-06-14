package de.nicidienase.chaosflix.leanback

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.ErrorSupportFragment

class BrowseErrorFragment : ErrorSupportFragment() {
    private var spinnerFragment: SpinnerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentId = arguments?.getInt(FRAGMENT) ?: throw IllegalStateException("missing fragmentID")
        spinnerFragment = SpinnerFragment().apply {
            try {
                parentFragmentManager.beginTransaction().add(fragmentId, this).commit()
            } catch (ex: IllegalStateException) {
            }
        }
    }

    fun setErrorContent(resourceId: Int) {
        setErrorContent(resources.getString(resourceId))
    }

    fun setErrorContent(message: String, parentFragmentManager: androidx.fragment.app.FragmentManager? = activity?.supportFragmentManager) {
        try {
            if (!isDetached) {
                spinnerFragment?.let {
                    parentFragmentManager?.beginTransaction()?.remove(it)?.commit()
                }
                imageDrawable = resources.getDrawable(R.drawable.lb_ic_sad_cloud, null)
                setMessage(message)
                setDefaultBackground(TRANSLUCENT)
                buttonText = resources.getString(R.string.dismiss_error)

                if (parentFragmentManager != null) {
                    setButtonClickListener { _ -> dismiss(parentFragmentManager) }
                } else {
                    setButtonClickListener { _ -> dismiss() }
                }
            }
        } catch (ex: IllegalStateException) {
            Log.e(TAG, "could not show error fragment")
        }
    }

    override fun onPause() {
        super.onPause()
        spinnerFragment?.let {
            parentFragmentManager?.beginTransaction()?.remove(it)?.commit()
        } ?: Log.e(TAG, "Could not remove spinnerFragment")
    }

    fun dismiss(parentFragmentManager: androidx.fragment.app.FragmentManager? = activity?.supportFragmentManager) {
        if (parentFragmentManager != null) {
            with(parentFragmentManager.beginTransaction()) {
                spinnerFragment?.let {
                    remove(it)
                }
                remove(this@BrowseErrorFragment)
                commit()
                parentFragmentManager.popBackStack()
            }
        } else {
            Log.e(TAG, "Cannot dismiss, parentFragmentManager is null")
        }
    }

    class SpinnerFragment : androidx.fragment.app.Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.loading_fragment, container, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        spinnerFragment = null
    }

    companion object {

        private val TRANSLUCENT = true
        val FRAGMENT = "fragmentId"
        val TAG = BrowseErrorFragment::class.java.simpleName

        fun showErrorFragment(manager: androidx.fragment.app.FragmentManager, fragmentId: Int, addToBackstack: Boolean = false): BrowseErrorFragment {
            val errorFragment = BrowseErrorFragment()
            val args = Bundle()
            args.putInt(BrowseErrorFragment.FRAGMENT, fragmentId)
            errorFragment.arguments = args
            if (addToBackstack) {
                manager.beginTransaction().add(fragmentId, errorFragment).addToBackStack(null).commit()
            } else {
                manager.beginTransaction().add(fragmentId, errorFragment).commit()
            }
            return errorFragment
        }
    }
}
