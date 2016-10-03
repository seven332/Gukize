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
 * Created by Hippo on 10/3/2016.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ConacoTask;
import com.hippo.conaco.DataContainer;
import com.hippo.conaco.Unikery;
import com.hippo.largeimageview.LargeImageView;
import com.hippo.largeimageview.image.AutoSource2;
import com.hippo.streampipe.InputStreamPipe;

public class LargeGukizeView extends LargeImageView implements Unikery<IBData>,
        LargeImageView.ImageInitListener {

    private Conaco<IBData> mConaco;
    private int mId = Unikery.INVALID_ID;

    private String mKey;
    private String mUrl;
    private DataContainer mContainer;
    private boolean mUseNetwork;
    private boolean mHasData;
    private boolean mFailOrCancel;

    private Listener mListener;

    public LargeGukizeView(Context context) {
        super(context);
        init();
    }

    public LargeGukizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LargeGukizeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mConaco = Gukize.getConaco();
        setImageInitListener(this);
    }

    private void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void setTaskId(int id) {
        mId = id;
    }

    @Override
    public int getTaskId() {
        return mId;
    }

    /**
     * Return true if the load task is running now.
     */
    public boolean isLoading() {
        return mId != Unikery.INVALID_ID;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mHasData && !isLoading() && !mFailOrCancel && getImage() == null) {
            load(mKey, mUrl, mContainer, mUseNetwork);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Avoid a lot of tasks stuck
        if (isLoading()) {
            mConaco.cancel(this);
        }
        // Free image source
        setImage(null);
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
        mFailOrCancel = false;

        if (ViewCompat.isAttachedToWindow(this)) {
            final ConacoTask.Builder<IBData> builder = new ConacoTask.Builder<>();
            builder.unikery = this;
            builder.key = key;
            builder.url = url;
            builder.dataContainer = container;
            builder.useNetwork = useNetwork;
            builder.skipDecode = true;
            mConaco.load(builder);
        }
    }

    @Override
    public void onMiss(int source) {}

    @Override
    public void onProgress(long singleReceivedSize, long receivedSize, long totalSize) {
        if (mListener != null) {
            mListener.onProgress(singleReceivedSize, receivedSize, totalSize);
        }
    }

    @Override
    public void onWait() {}

    @Override
    public void onGetValue(@NonNull IBData value, int source) {
        throw new IllegalStateException("Not support onGetValue.");
    }

    @Override
    public void onGetPipe(@NonNull InputStreamPipe pipe) {
        setImage(new AutoSource2(pipe));
    }

    @Override
    public void onFailure() {
        mFailOrCancel = true;

        if (mListener != null) {
            mListener.onCancel();
        }
    }

    @Override
    public void onCancel() {
        mFailOrCancel = true;

        if (mListener != null) {
            mListener.onCancel();
        }
    }

    @Override
    public void onImageInitSuccessful() {
        if (mListener != null) {
            mListener.onSuccess();
        }
    }

    @Override
    public void onImageInitFailed() {
        if (mListener != null) {
            mListener.onFailure();
        }
    }

    public interface Listener {
        void onProgress(long singleReceivedSize, long receivedSize, long totalSize);
        void onSuccess();
        void onFailure();
        void onCancel();
    }
}
