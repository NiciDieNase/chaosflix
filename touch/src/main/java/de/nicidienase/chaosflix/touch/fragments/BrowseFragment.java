package de.nicidienase.chaosflix.touch.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.touch.ViewModelFactory;
import de.nicidienase.chaosflix.touch.viewmodels.BrowseViewModel;

public class BrowseFragment extends Fragment {

	private BrowseViewModel mViewModel;
	private View progressOverlay;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewModel = ViewModelProviders.of(getActivity(), ViewModelFactory.INSTANCE).get(BrowseViewModel.class);

	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		progressOverlay = view.findViewById(R.id.progress_overlay);
	}

	public void finishLoading(){
		if(progressOverlay != null)
			progressOverlay.setVisibility(View.GONE);
	}

	public BrowseViewModel getViewModel() {
		return mViewModel;
	}
}
