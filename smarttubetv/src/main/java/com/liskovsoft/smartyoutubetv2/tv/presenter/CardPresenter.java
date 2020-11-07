package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.leanback.widget.Presenter;
import androidx.core.content.ContextCompat;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.prefs.MainUIData;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.tv.ui.widgets.textbadgeview.TextBadgeImageCardView;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static final String TAG = CardPresenter.class.getSimpleName();
    private static final float ZOOM_RATIO = 1.35f;
    private int mDefaultBackgroundColor = -1;
    private int mDefaultTextColor = -1;
    private int mSelectedBackgroundColor = -1;
    private int mSelectedTextColor = -1;
    private Drawable mDefaultCardImage;
    private boolean mIsAnimatedPreviewsEnabled;
    private boolean mIsLargeUIEnabled;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mDefaultBackgroundColor =
            ContextCompat.getColor(parent.getContext(), R.color.card_default_background_dark);
        mDefaultTextColor =
                ContextCompat.getColor(parent.getContext(), R.color.card_default_text);
        mSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.card_selected_background_white);
        mSelectedTextColor =
                ContextCompat.getColor(parent.getContext(), R.color.card_selected_text_grey);
        mDefaultCardImage = ContextCompat.getDrawable(parent.getContext(), R.drawable.movie);

        mIsAnimatedPreviewsEnabled = MainUIData.instance(parent.getContext()).isAnimatedPreviewsEnabled();
        mIsLargeUIEnabled = MainUIData.instance(parent.getContext()).isLargeUIEnabled();

        TextBadgeImageCardView cardView = new TextBadgeImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };
        
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private void updateCardBackgroundColor(TextBadgeImageCardView view, boolean selected) {
        int backgroundColor = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;
        int textColor = selected ? mSelectedTextColor : mDefaultTextColor;

        // Both background colors should be set because the view's
        // background is temporarily visible during animations.
        view.setBackgroundColor(backgroundColor);
        View infoField = view.findViewById(R.id.info_field);
        if (infoField != null) {
            infoField.setBackgroundColor(backgroundColor);
        }

        TextView titleText = view.findViewById(R.id.title_text);
        if (titleText != null) {
            titleText.setTextColor(textColor);
        }
        TextView contentText = view.findViewById(R.id.content_text);
        if (contentText != null) {
            contentText.setTextColor(textColor);
        }
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Video video = (Video) item;

        float zoomRatio = mIsLargeUIEnabled ? ZOOM_RATIO : 1;

        TextBadgeImageCardView cardView = (TextBadgeImageCardView) viewHolder.view;
        Resources res = cardView.getResources();

        cardView.setTitleText(video.title);
        cardView.setContentText(video.description);
        cardView.setBadgeText(video.badge);
        cardView.setProgress(video.percentWatched);

        if (mIsAnimatedPreviewsEnabled) {
            cardView.setPreviewUrl(video.previewUrl);
        }

        if (mIsLargeUIEnabled) {
            float titleSize = res.getDimension(R.dimen.lb_basic_card_title_text_size);
            float contentSize = res.getDimension(R.dimen.lb_basic_card_content_text_size);

            TextView titleText = cardView.findViewById(R.id.title_text);
            if (titleText != null) {
                titleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize * zoomRatio);
            }
            TextView contentText = cardView.findViewById(R.id.content_text);
            if (contentText != null) {
                contentText.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentSize * zoomRatio);
            }
        }

        if (video.cardImageUrl != null) {
            // Set card size from dimension resources.
            int width = res.getDimensionPixelSize(R.dimen.card_width);
            int height = res.getDimensionPixelSize(R.dimen.card_height);

            if (mIsLargeUIEnabled) {
                width *= zoomRatio;
                height *= zoomRatio;
            }

            cardView.setMainImageDimensions(width, height);

            Glide.with(cardView.getContext())
                    .load(video.cardImageUrl)
                    .apply(RequestOptions.errorOf(mDefaultCardImage))
                    .listener(mErrorListener)
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        TextBadgeImageCardView cardView = (TextBadgeImageCardView) viewHolder.view;

        // Remove references to images so that the garbage collector can free up memory.
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }

    private final RequestListener<Drawable> mErrorListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            Log.e(TAG, "Glide load failed: " + e);
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    };
}
