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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hippo.image.ImageData;
import com.hippo.image.ImageRenderer;

/**
 * This class uses {@link Bitmap} to represent {@link ImageRenderer}.
 * Call {@link #getBitmap()} to get the bitmap.
 */
class ImageBitmap {

    private static final String LOG_TAG = ImageBitmap.class.getSimpleName();

    private final int mSX;
    private final int mSY;
    private final int mSWidth;
    private final int mSHeight;
    private final int mRatio;

    private final int mWidth;
    private final int mHeight;
    private final int mFormat;
    private final boolean mOpaque;
    private final int mFrameCount;
    private final int[] mDelayArray;
    private final int mByteCount;

    @Nullable
    private Bitmap mBitmap;
    @Nullable
    private ImageRenderer mImageRenderer;

    /**
     * The ImageData must be completed and
     * ratio <= width && ratio <= height, or throw
     * IllegalStateException.
     */
    public ImageBitmap(ImageData imageData, Rect rect, int ratio) {
        // Only completed image supported
        if (!imageData.isCompleted()) {
            throw new IllegalStateException("ImageBitmap can only handle completed ImageData");
        }

        if (rect == null || rect.isEmpty()) {
            mSX = 0;
            mSY = 0;
            mSWidth = imageData.getWidth();
            mSHeight = imageData.getHeight();
        } else {
            mSX = rect.left;
            mSY = rect.top;
            mSWidth = rect.width();
            mSHeight = rect.height();
        }

        // Check ration invalid
        if (ratio > mSWidth || ratio > mSHeight) {
            throw new IllegalStateException("Ratio is too big");
        }

        mRatio = ratio;
        mWidth = mSWidth / ratio;
        mHeight = mSHeight / ratio;
        mFormat = imageData.getFormat();
        mOpaque = imageData.isOpaque();
        mFrameCount = imageData.getFrameCount();
        mByteCount = imageData.getByteCount();
        mDelayArray = new int[mFrameCount];
        for (int i = 0; i < mFrameCount; i++) {
            mDelayArray[i] = imageData.getDelay(i);
        }

        try {
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            // Out of memory. It looks like recycled.
            Log.e(LOG_TAG, "Create Bitmap for ImageBitmap failed.", e);
            // If ImageData is not referenced, it must be not in memory cache.
            // Recycle it now to avoid memory leak.
            if (!imageData.isReferenced()) {
                imageData.recycle();
            }
            return;
        }

        // Render first frame
        final ImageRenderer imageRenderer = imageData.createImageRenderer();
        imageRenderer.reset();
        imageRenderer.render(mBitmap, 0, 0, mSX, mSY, mSWidth, mSHeight, ratio, true, Color.TRANSPARENT);

        if (mFrameCount == 1) {
            // Recycle image renderer if it is not animated
            imageRenderer.recycle();
            // If ImageData is not referenced, it must be not in memory cache.
            // Recycle it now to avoid memory leak.
            if (!imageData.isReferenced()) {
                imageData.recycle();
            }
        } else {
            // Store image renderer if it is animated
            mImageRenderer = imageRenderer;
        }
    }

    /**
     * Recycle the Bitmap and ImageRenderer.
     */
    public void recycle() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        if (mImageRenderer != null) {
            mImageRenderer.recycle();
            // If ImageData is not referenced, it must be not in memory cache.
            // Recycle it now to avoid memory leak.
            final ImageData imageData = mImageRenderer.getImageData();
            if (!imageData.isReferenced()) {
                imageData.recycle();
            }
            mImageRenderer = null;
        }
    }

    /**
     * Return true if the ImageBitmap is recycled.
     */
    public boolean isRecycled() {
        return mBitmap == null;
    }

    /**
     * Draw first frame to bitmap.
     */
    public void reset() {
        if (mBitmap != null && mImageRenderer != null) {
            mImageRenderer.reset();
            mImageRenderer.render(mBitmap, 0, 0, mSX, mSY, mSWidth, mSHeight, mRatio, true, Color.TRANSPARENT);
        }
    }

    /**
     * Draw next frame to bitmap.
     */
    public void advance() {
        if (mBitmap != null && mImageRenderer != null) {
            mImageRenderer.advance();
            mImageRenderer.render(mBitmap, 0, 0, mSX, mSY, mSWidth, mSHeight, mRatio, true, Color.TRANSPARENT);
        }
    }

    /**
     * Get the Bitmap, null if recycled.
     */
    @Nullable
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * Return the ratio.
     */
    public int getRatio() {
        return mRatio;
    }

    /**
     * Return the width of the Bitmap.
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Return the height of the Bitmap.
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Return the format of the ImageData.
     */
    public int getFormat() {
        return mFormat;
    }

    /**
     * Return true if the ImageData is opaque.
     */
    public boolean isOpaque() {
        return mOpaque;
    }

    /**
     * Return the frame count of the ImageData.
     */
    public int getFrameCount() {
        return mFrameCount;
    }

    /**
     * Return true if it is animated image
     */
    public boolean isAnimated() {
        return mFrameCount > 1;
    }

    /**
     * Return the delay of the frame.
     */
    public int getDelay(int frame) {
        return mDelayArray[frame];
    }

    /**
     * Return delay of current delay.
     * Return 0 if current frame is invalid.
     */
    public int getCurrentDelay() {
        if (mImageRenderer != null) {
            return mImageRenderer.getCurrentDelay();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Return the byte count of the ImageData.
     */
    public int getByteCount() {
        return mByteCount;
    }
}
