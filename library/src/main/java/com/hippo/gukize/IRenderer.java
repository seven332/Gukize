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
 * Created by Hippo on 9/24/2016.
 */

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hippo.image.ImageData;
import com.hippo.image.ImageRenderer;

class IRenderer extends IBRenderer {

    private static final String LOG_TAG = IRenderer.class.getSimpleName();

    private final int mWidth;
    private final int mHeight;
    private final boolean mOpaque;

    @Nullable
    private ImageRenderer mImageRenderer;
    @Nullable
    private Bitmap mBitmap;

    public IRenderer(@NonNull IBData data, @NonNull ImageRenderer imageRenderer) {
        super(data);
        final ImageData imageData = imageRenderer.getImageData();
        mWidth = imageData.getWidth();
        mHeight = imageData.getHeight();
        mOpaque = imageData.isOpaque();
        mImageRenderer = imageRenderer;
        try {
            mBitmap = Bitmap.createBitmap(imageData.getWidth(), imageData.getHeight(),
                    imageData.isOpaque() ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            Log.d(LOG_TAG, "Can't create Bitmap, out of memory.");
            recycle();
        }
    }

    @Override
    public void recycle() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        if (mImageRenderer != null) {
            mImageRenderer.recycle();
            mImageRenderer = null;
        }
        super.recycle();
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public boolean isOpaque() {
        return mOpaque;
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public int getCurrentDelay() {
        if (mImageRenderer != null) {
            return mImageRenderer.getCurrentDelay();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void reset() {
        if (mBitmap != null && mImageRenderer != null) {
            mImageRenderer.reset();
            mImageRenderer.render(mBitmap, 0, 0, 0, 0, mWidth, mHeight, 1, false, 0);
        }
    }

    @Override
    public void advance() {
        if (mBitmap != null && mImageRenderer != null) {
            mImageRenderer.advance();
            mImageRenderer.render(mBitmap, 0, 0, 0, 0, mWidth, mHeight, 1, false, 0);
        }
    }

    @Nullable
    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }
}
