package de.nicidienase.chaosflix.touch.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.exomedia.ui.widget.VideoView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;
import de.nicidienase.chaosflix.R;
import de.nicidienase.chaosflix.common.entities.PlayableItem;

public class MediaPlayerFragment extends Fragment {
	private static final String ARG_ITEM = "title";
	private static final String ARG_INDEX = "index";

	private OnMediaPlayerInteractionListener mListener;

	@BindView(R.id.video_view)
	VideoView videoView;
	private PlayableItem mItem;
	private int mIndex = -1;

	public MediaPlayerFragment() {
	}

	public static MediaPlayerFragment newInstance(PlayableItem item, int index) {
		MediaPlayerFragment fragment = new MediaPlayerFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_ITEM, item);
		args.putInt(ARG_INDEX,index);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		if (getArguments() != null) {
			mItem = getArguments().getParcelable(ARG_ITEM);
			mIndex = getArguments().getInt(ARG_INDEX);
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
		if(mIndex >= 0){
			setVideoByIndex(mIndex);
		} else {
			List<String> strings = mItem.getPlaybackOptions();
			CharSequence[] options = strings.toArray(new CharSequence[strings.size()]);
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.select_option)
					.setItems(options,(dialog, which) -> setVideoByIndex(which))
					.create().show();
			}
		}

	private void setVideoByIndex(int index) {
		videoView.setVideoURI(Uri.parse(mItem.getUrlForOption(index)));
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
