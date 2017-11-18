package de.nicidienase.chaosflix.touch.browse;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.touch.ViewModelFactory;

public class BrowseFragment extends Fragment {

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
