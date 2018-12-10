package de.nicidienase.chaosflix.leanback.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.ErrorSupportFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import de.nicidienase.chaosflix.leanback.R;

public class BrowseErrorFragment extends ErrorSupportFragment {

	private static final boolean TRANSLUCENT = true;
	public static final String FRAGMENT = "fragmentId";
	private SpinnerFragment spinnerFragment;

	public static BrowseErrorFragment showErrorFragment(FragmentManager manager, int fragmentId) {
		BrowseErrorFragment errorFragment = new BrowseErrorFragment();
		Bundle args = new Bundle();
		args.putInt(BrowseErrorFragment.FRAGMENT, fragmentId);
		errorFragment.setArguments(args);
		manager.beginTransaction().replace(fragmentId, errorFragment)
				.addToBackStack(null).commit();
		return errorFragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int fragmentId = getArguments().getInt(FRAGMENT);
		spinnerFragment = new SpinnerFragment();
		getFragmentManager().beginTransaction().add(fragmentId, spinnerFragment).commit();
	}

	public void setErrorContent(int resourceId) {
		setErrorContent(getResources().getString(resourceId));
	}

	public void setErrorContent(String message) {
		setImageDrawable(getResources().getDrawable(R.drawable.lb_ic_sad_cloud, null));
		setMessage(message);
		setDefaultBackground(TRANSLUCENT);

		setButtonText(getResources().getString(R.string.dismiss_error));
		setButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();
			}
		});
	}

	public void dismiss() {
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager != null) {
			fragmentManager
					.beginTransaction()
					.remove(BrowseErrorFragment.this)
					.remove(spinnerFragment)
					.commit();
			fragmentManager.popBackStack();
		}
	}


	public static class SpinnerFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			ProgressBar progressBar = new ProgressBar(container.getContext());
			if (container instanceof FrameLayout) {
				Resources res = getResources();
				int width = res.getDimensionPixelSize(R.dimen.spinner_width);
				int height = res.getDimensionPixelSize(R.dimen.spinner_height);
				FrameLayout.LayoutParams layoutParams =
						new FrameLayout.LayoutParams(width, height, Gravity.CENTER);
				progressBar.setLayoutParams(layoutParams);
			}
			return progressBar;
		}
	}
}
