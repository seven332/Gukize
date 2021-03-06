/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.gukize;

/*
 * Created by Hippo on 8/22/2016.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.Unikery;
import com.hippo.konwidget.AdvImageView;
import com.hippo.streampipe.InputStreamPipe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GukizeView extends AdvImageView implements Unikery<IBData>,
        View.OnClickListener, View.OnLongClickListener, Animatable {

    private static final String LOG_TAG = GukizeView.class.getSimpleName();

    @IntDef({DRAWABLE_NONE, DRAWABLE_PLACEHOLDER, DRAWABLE_LOAD,
            DRAWABLE_FAILURE, DRAWABLE_CUSTOM})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DrawableState {}

    @IntDef({RETRY_TYPE_NONE, RETRY_TYPE_CLICK, RETRY_TYPE_LONG_CLICK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RetryType {}

    private static final int DRAWABLE_NONE = 0;
    private static final int DRAWABLE_PLACEHOLDER = 1;
    private static final int DRAWABLE_LOAD = 2;
    private static final int DRAWABLE_FAILURE = 4;
    private static final int DRAWABLE_CUSTOM = 5;

    public static final int RETRY_TYPE_NONE = 0;
    public static final int RETRY_TYPE_CLICK = 1;
    public static final int RETRY_TYPE_LONG_CLICK = 2;

    private static final int TIME_TRANSITION = 300;

    private int mId = Unikery.INVALID_ID;

    private Conaco<IBData> mConaco;

    private String mKey;
    private String mUrl;
    private DataContainer mContainer;
    private boolean mUseNetwork;
    private boolean mHasData;

    @DrawableState
    private int mDrawableState = DRAWABLE_NONE;

    @RetryType
    private int mRetryType = RETRY_TYPE_NONE;
    private boolean mAutoStart = true;
    private boolean mShowFadeAnimation = true;
    @ScaleType
    private int mPlaceholderScaleType = SCALE_TYPE_FIT_CENTER;
    @ScaleType
    private int mActualScaleType = SCALE_TYPE_FIT_CENTER;
    @ScaleType
    private int mFailureScaleType = SCALE_TYPE_FIT_CENTER;
    @ScaleType
    private int mCustomScaleType = SCALE_TYPE_FIT_CENTER;
    private Drawable mPlaceholderDrawable;
    private Drawable mFailureDrawable;

    private Listener mListener;

    public GukizeView(Context context) {
        super(context);
        mConaco = Gukize.getConaco();
    }

    public GukizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public GukizeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @SuppressWarnings("WrongConstant")
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mConaco = Gukize.getConaco();

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.GukizeView, defStyleAttr, defStyleRes);
        setRetryType(a.getInt(R.styleable.GukizeView_gkz_retryType, RETRY_TYPE_NONE));
        setAutoStart(a.getBoolean(R.styleable.GukizeView_gkz_autoStart, true));
        setShowFadeAnimation(a.getBoolean(R.styleable.GukizeView_gkz_showFadeAnimation, true));
        setPlaceholderScaleType(a.getInt(R.styleable.GukizeView_gkz_placeholderScaleType, SCALE_TYPE_FIT_CENTER));
        setActualScaleType(a.getInt(R.styleable.GukizeView_gkz_actualScaleType, SCALE_TYPE_FIT_CENTER));
        setFailureScaleType(a.getInt(R.styleable.GukizeView_gkz_failureScaleType, SCALE_TYPE_FIT_CENTER));
        setPlaceholderDrawable(a.getDrawable(R.styleable.GukizeView_gkz_placeholderDrawable));
        setFailureDrawable(a.getDrawable(R.styleable.GukizeView_gkz_failureDrawable));
        a.recycle();
    }

    @Override
    public void setTaskId(int id) {
        mId = id;
    }

    @Override
    public int getTaskId() {
        return mId;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Return true if the load task is running now.
     */
    public boolean isLoading() {
        return mId != Unikery.INVALID_ID;
    }

    /**
     * Set whether auto start animated image.
     */
    public void setAutoStart(boolean autoStart) {
        if (mAutoStart != autoStart) {
            mAutoStart = autoStart;

            final IBDrawable drawable;
            if (autoStart && (drawable = getLoadedImageDrawable()) != null && !drawable.isRunning()) {
                drawable.start();
            }
        }
    }

    /**
     * Set whether show fade animation.
     */
    public void setShowFadeAnimation(boolean showFadeAnimation) {
        mShowFadeAnimation = showFadeAnimation;
    }

    /**
     * Set scale type for placeholder drawable.
     * {@link #SCALE_TYPE_MATRIX} not supported.
     */
    public void setPlaceholderScaleType(@ScaleType int scaleType) {
        mPlaceholderScaleType = scaleType;
        if (mDrawableState == DRAWABLE_PLACEHOLDER) {
            setScaleType(scaleType);
        }
    }

    /**
     * Set scale type for loaded drawable.
     * {@link #SCALE_TYPE_MATRIX} not supported.
     */
    public void setActualScaleType(@ScaleType int scaleType) {
        mActualScaleType = scaleType;
        if (mDrawableState == DRAWABLE_LOAD) {
            setScaleType(scaleType);
        }
    }

    /**
     * Set scale type for failure drawable.
     * {@link #SCALE_TYPE_MATRIX} not supported.
     */
    public void setFailureScaleType(@ScaleType int scaleType) {
        mFailureScaleType = scaleType;
        if (mDrawableState == DRAWABLE_FAILURE) {
            setScaleType(scaleType);
        }
    }

    /**
     * Set scale type for custom drawable.
     * {@link #SCALE_TYPE_MATRIX} not supported.
     */
    public void setCustomScaleType(@ScaleType int scaleType) {
        mCustomScaleType = scaleType;
        if (mDrawableState == DRAWABLE_FAILURE) {
            setScaleType(scaleType);
        }
    }

    /**
     * Set placeholder drawable. It is shown before image loaded.
     */
    public void setPlaceholderDrawable(Drawable drawable) {
        mPlaceholderDrawable = drawable;
        if (mDrawableState == DRAWABLE_PLACEHOLDER) {
            setDrawable(drawable, DRAWABLE_PLACEHOLDER, true);
        }
    }

    /**
     * Set retry drawable. It is shown when load failed.
     */
    public void setFailureDrawable(Drawable drawable) {
        mFailureDrawable = drawable;
        if (mDrawableState == DRAWABLE_FAILURE) {
            setDrawable(drawable, DRAWABLE_FAILURE, true);
        }
    }

    /**
     * Set retry type for load failure.
     */
    public void setRetryType(@RetryType int retryType) {
        if (mRetryType == retryType) {
            return;
        }

        if (mDrawableState == DRAWABLE_FAILURE) {
            clearRetry();
            mRetryType = retryType;
            applyRetry();
        } else {
            mRetryType = retryType;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mHasData && !isLoading() && mDrawableState != DRAWABLE_LOAD) {
            load(mKey, mUrl, mContainer, mUseNetwork);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Avoid a lot of tasks stuck.
        if (isLoading()) {
            mConaco.cancel(this);
        }
        // Free loaded drawable.
        if (mDrawableState == DRAWABLE_LOAD) {
            setDrawable(null, DRAWABLE_NONE, false);
        }
    }

    public void load(String key, String url) {
        load(key, url, null);
    }

    public void load(String key, String url, DataContainer container) {
        load(key, url, container, true);
    }

    public void load(String key, String url, DataContainer container, boolean useNetwork) {
        mKey = key;
        mUrl = url;
        mContainer = container;
        mUseNetwork = useNetwork;

        mHasData = true;

        if (ViewCompat.isAttachedToWindow(this)) {
            if (mListener != null) {
                mListener.onLoad();
            }

            final ConacoTask.Builder<IBData> builder = new ConacoTask.Builder<>();
            builder.unikery = this;
            builder.key = key;
            builder.url = url;
            builder.dataContainer = container;
            builder.useNetwork = useNetwork;
            mConaco.load(builder);
        }
    }

    /**
     * Set custom drawable to show. It will cancel current load task.
     */
    public void setCustomDrawable(Drawable drawable) {
        // Clear load data
        mKey = null;
        mUrl = null;
        mContainer = null;

        mHasData = false;

        mConaco.cancel(this);
        setDrawable(drawable, DRAWABLE_CUSTOM, true);
    }

    private void applyRetry() {
        if (mRetryType == RETRY_TYPE_CLICK) {
            setOnClickListener(this);
        } else if (mRetryType == RETRY_TYPE_LONG_CLICK) {
            setOnLongClickListener(this);
        }
    }

    private void clearRetry() {
        if (mRetryType == RETRY_TYPE_CLICK) {
            setOnClickListener(null);
            setClickable(false);
        } else if (mRetryType == RETRY_TYPE_LONG_CLICK) {
            setOnLongClickListener(null);
            setLongClickable(false);
        }
    }

    /**
     * Wrap loaded drawable to add effect.
     */
    @NonNull
    protected Drawable wrapDrawable(@NonNull Drawable drawable, @Conaco.Source int source) {
        final boolean animated = drawable instanceof IBDrawable
                && ((IBDrawable) drawable).isAnimated();
        if (mShowFadeAnimation && source != Conaco.SOURCE_MEMORY && !animated) {
            final Drawable[] layers = new Drawable[2];
            final Drawable firstDrawable;
            if (mPlaceholderDrawable != null && mPlaceholderDrawable.getIntrinsicWidth() == -1 &&
                    mPlaceholderDrawable.getIntrinsicHeight() == -1) {
                firstDrawable = mPlaceholderDrawable;
            } else {
                firstDrawable = new ColorDrawable(Color.TRANSPARENT);
            }
            layers[0] = firstDrawable;
            layers[1] = drawable;
            final TransitionDrawable newDrawable = new TransitionDrawable(layers);
            newDrawable.startTransition(TIME_TRANSITION);
            return newDrawable;
        } else {
            return drawable;
        }
    }

    /**
     * Must return what {@link #wrapDrawable(Drawable, int)} provide.
     */
    @NonNull
    protected Drawable unwrapDrawable(@NonNull Drawable drawable) {
        if (drawable instanceof TransitionDrawable) {
            final TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
            if (transitionDrawable.getNumberOfLayers() == 2) {
                return transitionDrawable.getDrawable(1);
            }
        }
        return drawable;
    }

    private IBDrawable getLoadedImageDrawable() {
        Drawable drawable;
        if (mDrawableState == DRAWABLE_LOAD && (drawable = getDrawable()) != null) {
            drawable = unwrapDrawable(drawable);
            if (drawable instanceof IBDrawable) {
                return (IBDrawable) drawable;
            } else {
                throw new IllegalStateException("unwrapDrawable() must return ImageDrawable, " +
                        "but it is " + drawable.getClass().getName());
            }
        } else {
            return null;
        }
    }

    private void setDrawable(Drawable drawable, @DrawableState int drawableState, boolean overrider) {
        if (mDrawableState == drawableState && !overrider) {
            return;
        }

        // Release old loaded image drawable
        final IBDrawable oldImageDrawable = getLoadedImageDrawable();
        if (oldImageDrawable != null) {
            oldImageDrawable.recycle();
        }

        if (mDrawableState != drawableState) {
            // Apply or cancel retry
            if (drawableState == DRAWABLE_FAILURE) {
                applyRetry();
            } else if (mDrawableState == DRAWABLE_FAILURE) {
                clearRetry();
            }

            // Scale type
            switch (drawableState) {
                case DRAWABLE_NONE:
                    break;
                case DRAWABLE_PLACEHOLDER:
                    setScaleType(mPlaceholderScaleType);
                    break;
                case DRAWABLE_LOAD:
                    setScaleType(mActualScaleType);
                    break;
                case DRAWABLE_FAILURE:
                    setScaleType(mFailureScaleType);
                    break;
                case DRAWABLE_CUSTOM:
                    setScaleType(mCustomScaleType);
                    break;
            }
        }

        mDrawableState = drawableState;

        setImageDrawable(drawable);
    }

    @Override
    public void onWait() {
        setDrawable(mPlaceholderDrawable, DRAWABLE_PLACEHOLDER, false);
    }

    @Override
    public void onMiss(@Conaco.Source int source) {
        if (source == Conaco.SOURCE_MEMORY) {
            setDrawable(mPlaceholderDrawable, DRAWABLE_PLACEHOLDER, false);
        }
    }

    @Override
    public void onGetValue(@NonNull IBData value, @Conaco.Source int source) {
        // The IBData might be recycled, but it's small probability event.
        if (value.isRecycled()) {
            Log.w(LOG_TAG, "IBData is recycled in GukizeView.onGetValue(), need larger memory cache.");
            onFailure();
            return;
        }

        // We use onDetachedFromWindow to handle IBData recycle.
        // So can't use IBData when detached from window.
        if (!ViewCompat.isAttachedToWindow(this)) {
            // If IBData is not referenced, it must be not in memory cache.
            // Recycle it now to avoid memory leak.
            if (!value.isReferenced()) {
                value.recycle();
            }
            return;
        }

        final IBDrawable imageDrawable = new IBDrawable(value.createRenderer());
        if (mAutoStart) {
            imageDrawable.start();
        }
        final Drawable drawable = wrapDrawable(imageDrawable, source);
        setDrawable(drawable, DRAWABLE_LOAD, true);

        if (mListener != null) {
            mListener.onSuccess();
        }
    }

    @Override
    public void onGetPipe(@NonNull InputStreamPipe pipe) {
        throw new IllegalStateException("Not support onGetPipe.");
    }

    @Override
    public void onFailure() {
        setDrawable(mFailureDrawable, DRAWABLE_FAILURE, false);

        if (mListener != null) {
            mListener.onFailure();
        }
    }

    @Override
    public void onCancel() {
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    @Override
    public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {
        if (mListener != null) {
            mListener.onProgress(singleReceivedSize, receivedSize, totalSize);
        }
    }

    @Override
    public void start() {
        final IBDrawable drawable = getLoadedImageDrawable();
        if (drawable != null) {
            drawable.start();
        }
    }

    @Override
    public void stop() {
        final IBDrawable drawable = getLoadedImageDrawable();
        if (drawable != null) {
            drawable.stop();
        }
    }

    @Override
    public boolean isRunning() {
        final IBDrawable drawable = getLoadedImageDrawable();
        return drawable != null && drawable.isRunning();
    }

    @Override
    public void onClick(@NonNull View v) {
        if (mHasData) {
            if (mListener != null) {
                mListener.onRetry();
            }
            load(mKey, mUrl, mContainer, mUseNetwork);
        }
    }

    @Override
    public boolean onLongClick(@NonNull View v) {
        if (mHasData) {
            if (mListener != null) {
                mListener.onRetry();
            }
            load(mKey, mUrl, mContainer, mUseNetwork);
        }
        return true;
    }

    public interface Listener {
        void onLoad();
        void onProgress(long singleReceivedSize, long receivedSize, long totalSize);
        void onSuccess();
        void onFailure();
        void onCancel();
        void onRetry();
    }
}
