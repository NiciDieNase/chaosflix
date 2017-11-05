package de.nicidienase.chaosflix.touch.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import de.nicidienase.chaosflix.touch.ViewModelFactory;
import de.nicidienase.chaosflix.touch.viewmodels.BrowseViewModel;

public class ChaosflixFragment extends Fragment {

	private BrowseViewModel mViewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewModel = ViewModelProviders.of(getActivity(), ViewModelFactory.INSTANCE).get(BrowseViewModel.class);
	}


	public BrowseViewModel getViewModel() {
		return mViewModel;
	}
}
