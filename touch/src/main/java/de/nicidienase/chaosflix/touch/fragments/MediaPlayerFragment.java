package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.exomedia.ui.widget.VideoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.nicidienase.chaosflix.R;

public class MediaPlayerFragment extends Fragment {
	private static final String ARG_TITLE = "title";
	private static final String ARG_URL = "url";

	private String mTitle;
	private String mVideoURL;
	private OnMediaPlayerInteractionListener mListener;

	@BindView(R.id.video_view)
	VideoView videoView;

	public MediaPlayerFragment() {
	}

	public static MediaPlayerFragment newInstance(String title, String videoURL) {
		MediaPlayerFragment fragment = new MediaPlayerFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putString(ARG_URL,videoURL);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mTitle = getArguments().getString(ARG_TITLE);
			mVideoURL = getArguments().getString(ARG_URL);
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
		videoView.setVideoURI(Uri.parse(mVideoURL));
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
