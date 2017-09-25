package de.nicidienase.chaosflix.touch.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.touch.ChaosflixViewModel;

/**
 * Created by felix on 25.09.17.
 */

public class ChaosflixFragment extends Fragment {

	private ChaosflixViewModel mViewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources res = getResources();
		ChaosflixViewModel.Factory factory =
				new ChaosflixViewModel.Factory(
						res.getString(R.string.api_media_ccc_url),
						res.getString(R.string.streaming_media_ccc_url));
		mViewModel = ViewModelProviders.of(getActivity(),factory).get(ChaosflixViewModel.class);
	}


	public ChaosflixViewModel getViewModel() {
		return mViewModel;
	}
}
