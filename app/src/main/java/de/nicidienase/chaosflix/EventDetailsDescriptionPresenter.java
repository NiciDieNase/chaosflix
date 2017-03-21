package de.nicidienase.chaosflix;

import android.graphics.Paint;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import de.nicidienase.chaosflix.entities.Event;

/**
 * Created by felix on 18.03.17.
 */

public class EventDetailsDescriptionPresenter extends Presenter {

	protected void onBindDescription(ViewHolder vh, Object item) {
		Event event = (Event) item;

		vh.getTitle().setMaxLines(3);
		vh.getTitle().setText(event.getTitle());
		vh.getSubtitle().setMaxLines(3);
		vh.getSubtitle().setText(event.getSubtitle());
		StringBuilder sb = new StringBuilder();
		String speaker = TextUtils.join(", ", event.getPersons());
		sb.append(event.getDescription())
				.append("\n\n")
				.append("by: ")
				.append(speaker)
				.append("\nreleased at: ")
				.append(event.getReleaseDate())
				.append("\nlast updated at: ")
				.append(event.getUpdatedAt())
				.append("\nTags: ")
				.append(event.getTags());
		vh.getBody().setEllipsize(null);
		vh.getBody().setMaxLines(30);
		vh.getBody().setText(sb.toString());
	}

	public static class ViewHolder extends Presenter.ViewHolder {
		public TextView mTitle;
		public TextView mSubtitle;
		public TextView mBody;
		public int mTitleMargin;
		public int mUnderTitleBaselineMargin;
		public int mUnderSubtitleBaselineMargin;
		public int mTitleLineSpacing;
		public int mBodyLineSpacing;
		public int mBodyMaxLines;
		public int mBodyMinLines;
		public Paint.FontMetricsInt mTitleFontMetricsInt;
		public Paint.FontMetricsInt mSubtitleFontMetricsInt;
		public Paint.FontMetricsInt mBodyFontMetricsInt;
		public int mTitleMaxLines;
		private ViewTreeObserver.OnPreDrawListener mPreDrawListener;

		public ViewHolder(final View view) {
			super(view);
			mTitle = (TextView) view.findViewById(android.support.v17.leanback.R.id.lb_details_description_title);
			mSubtitle = (TextView) view.findViewById(android.support.v17.leanback.R.id.lb_details_description_subtitle);
			mBody = (TextView) view.findViewById(android.support.v17.leanback.R.id.lb_details_description_body);

			Paint.FontMetricsInt titleFontMetricsInt = getFontMetricsInt(mTitle);
			final int titleAscent = view.getResources().getDimensionPixelSize(
					android.support.v17.leanback.R.dimen.lb_details_description_title_baseline);
			// Ascent is negative
			mTitleMargin = titleAscent + titleFontMetricsInt.ascent;

			mUnderTitleBaselineMargin = view.getResources().getDimensionPixelSize(
					android.support.v17.leanback.R.dimen.lb_details_description_under_title_baseline_margin);
			mUnderSubtitleBaselineMargin = view.getResources().getDimensionPixelSize(
					android.support.v17.leanback.R.dimen.lb_details_description_under_subtitle_baseline_margin);

			mTitleLineSpacing = view.getResources().getDimensionPixelSize(
					android.support.v17.leanback.R.dimen.lb_details_description_title_line_spacing);
			mBodyLineSpacing = view.getResources().getDimensionPixelSize(
					android.support.v17.leanback.R.dimen.lb_details_description_body_line_spacing);

			mBodyMaxLines = 15;
			mBodyMinLines = view.getResources().getInteger(
					android.support.v17.leanback.R.integer.lb_details_description_body_min_lines);
			mTitleMaxLines = mTitle.getMaxLines();

			mTitleFontMetricsInt = getFontMetricsInt(mTitle);
			mSubtitleFontMetricsInt = getFontMetricsInt(mSubtitle);
			mBodyFontMetricsInt = getFontMetricsInt(mBody);

			mTitle.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom,
										   int oldLeft, int oldTop, int oldRight, int oldBottom) {
					addPreDrawListener();
				}
			});
		}

		void addPreDrawListener() {
			if (mPreDrawListener != null) {
				return;
			}
			mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					if (mSubtitle.getVisibility() == View.VISIBLE
							&& mSubtitle.getTop() > view.getHeight()
							&& mTitle.getLineCount() > 1) {
						mTitle.setMaxLines(mTitle.getLineCount() - 1);
						return false;
					}
					final int titleLines = mTitle.getLineCount();
					final int maxLines = titleLines > 1 ? mBodyMinLines : mBodyMaxLines;
					if (mBody.getMaxLines() != maxLines) {
						mBody.setMaxLines(maxLines);
						return false;
					} else {
						removePreDrawListener();
						return true;
					}
				}
			};
			view.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
		}

		void removePreDrawListener() {
			if (mPreDrawListener != null) {
				view.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
				mPreDrawListener = null;
			}
		}

		public TextView getTitle() {
			return mTitle;
		}

		public TextView getSubtitle() {
			return mSubtitle;
		}

		public TextView getBody() {
			return mBody;
		}

		private Paint.FontMetricsInt getFontMetricsInt(TextView textView) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setTextSize(textView.getTextSize());
			paint.setTypeface(textView.getTypeface());
			return paint.getFontMetricsInt();
		}
	}

	@Override
	public final AbstractDetailsDescriptionPresenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(android.support.v17.leanback.R.layout.lb_details_description, parent, false);
		return new AbstractDetailsDescriptionPresenter.ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
		ViewHolder vh = (ViewHolder) viewHolder;
		onBindDescription(vh, item);

		boolean hasTitle = true;
		if (TextUtils.isEmpty(vh.mTitle.getText())) {
			vh.mTitle.setVisibility(View.GONE);
			hasTitle = false;
		} else {
			vh.mTitle.setVisibility(View.VISIBLE);
			vh.mTitle.setLineSpacing(vh.mTitleLineSpacing - vh.mTitle.getLineHeight()
					+ vh.mTitle.getLineSpacingExtra(), vh.mTitle.getLineSpacingMultiplier());
			vh.mTitle.setMaxLines(vh.mTitleMaxLines);
		}
		setTopMargin(vh.mTitle, vh.mTitleMargin);

		boolean hasSubtitle = true;
		if (TextUtils.isEmpty(vh.mSubtitle.getText())) {
			vh.mSubtitle.setVisibility(View.GONE);
			hasSubtitle = false;
		} else {
			vh.mSubtitle.setVisibility(View.VISIBLE);
			if (hasTitle) {
				setTopMargin(vh.mSubtitle, vh.mUnderTitleBaselineMargin
						+ vh.mSubtitleFontMetricsInt.ascent - vh.mTitleFontMetricsInt.descent);
			} else {
				setTopMargin(vh.mSubtitle, 0);
			}
		}

		if (TextUtils.isEmpty(vh.mBody.getText())) {
			vh.mBody.setVisibility(View.GONE);
		} else {
			vh.mBody.setVisibility(View.VISIBLE);
			vh.mBody.setLineSpacing(vh.mBodyLineSpacing - vh.mBody.getLineHeight()
					+ vh.mBody.getLineSpacingExtra(), vh.mBody.getLineSpacingMultiplier());

			if (hasSubtitle) {
				setTopMargin(vh.mBody, vh.mUnderSubtitleBaselineMargin
						+ vh.mBodyFontMetricsInt.ascent - vh.mSubtitleFontMetricsInt.descent);
			} else if (hasTitle) {
				setTopMargin(vh.mBody, vh.mUnderTitleBaselineMargin
						+ vh.mBodyFontMetricsInt.ascent - vh.mTitleFontMetricsInt.descent);
			} else {
				setTopMargin(vh.mBody, 0);
			}
		}
	}

	@Override
	public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {}

	public void onViewAttachedToWindow(EventDetailsDescriptionPresenter.ViewHolder holder) {
		// In case predraw listener was removed in detach, make sure
		// we have the proper layout.
		ViewHolder vh = (EventDetailsDescriptionPresenter.ViewHolder) holder;
		vh.addPreDrawListener();
		super.onViewAttachedToWindow(holder);
	}

	@Override
	public void onViewDetachedFromWindow(Presenter.ViewHolder holder) {
		ViewHolder vh = (EventDetailsDescriptionPresenter.ViewHolder) holder;
		vh.removePreDrawListener();
		super.onViewDetachedFromWindow(holder);
	}

	private void setTopMargin(TextView textView, int topMargin) {
		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
		lp.topMargin = topMargin;
		textView.setLayoutParams(lp);
	}
}
