package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.exomedia.ui.widget.VideoControls;
import com.devbrackets.android.exomedia.ui.widget.VideoView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;

public class MediaPlayerFragment extends Fragment {
	private static final String ARG_TITEL = "title";
	private static final String ARG_SUBTITLE = "subtitle";
	private static final String ARG_URL = "url";

	private OnMediaPlayerInteractionListener mListener;

	@BindView(R.id.video_view)
	VideoView videoView;
	private String mTitle;
	private String mSubtitle;
	private String mUrl;

	public MediaPlayerFragment() {
	}

	public static MediaPlayerFragment newInstance(String title, String subtitle, String url) {
		MediaPlayerFragment fragment = new MediaPlayerFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TITEL, title);
		args.putString(ARG_SUBTITLE, subtitle);
		args.putString(ARG_URL, url);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		if (getArguments() != null) {
			mTitle = getArguments().getString(ARG_TITEL);
			mSubtitle = getArguments().getString(ARG_SUBTITLE);
			mUrl = getArguments().getString(ARG_URL);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_media_player, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ButterKnife.bind(this,view);

		videoView.setOnPreparedListener(() -> videoView.start());
		VideoControls controls = videoView.getVideoControls();
		controls.setTitle(mTitle);
		controls.setSubTitle(mSubtitle);

		videoView.setVideoURI(Uri.parse(mUrl));
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnMediaPlayerInteractionListener) {
			mListener = (OnMediaPlayerInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnMediaPlayerInteractionListener {
	}
}
