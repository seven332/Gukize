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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class can render {@link IBData} through {@link #getBitmap()}
 */
abstract class IBRenderer {

    @Nullable
    private IBData mData;

    protected IBRenderer(@NonNull IBData data) {
        mData = data;
        data.addReference();
    }

    /**
     * Recycle the IBRenderer.
     */
    @CallSuper
    protected void recycle() {
        if (mData != null) {
            mData.removeReference();
            mData = null;
        }
    }

    /**
     * Return true if the IBRenderer is recycled.
     */
    public boolean isRecycled() {
        return mData == null;
    }

    /**
     * Return the width of the IBRenderer.
     * It is safe to call it if the IBRenderer is recycled.
     */
    public abstract int getWidth();

    /**
     * Return the height of the IBRenderer.
     * It is safe to call it if the IBRenderer is recycled.
     */
    public abstract int getHeight();

    /**
     * Return true if the IBRenderer is opaque.
     * It is safe to call it if the IBRenderer is recycled.
     */
    public abstract boolean isOpaque();

    /**
     * Return true if the IBRenderer is animated.
     * It is safe to call it if the IBRenderer is recycled.
     */
    public abstract boolean isAnimated();

    /**
     * Return the delay of current frame.
     * It is safe to call it if the IBRenderer is recycled.
     */
    public abstract int getCurrentDelay();

    /**
     * Reset current frame to zero.
     * It is safe to call it if the IBRenderer is recycled
     * and nothing will happen.
     */
    public abstract void reset();

    /**
     * Set current frame to next.
     * It is safe to call it if the IBRenderer is recycled
     * and nothing will happen.
     */
    public abstract void advance();

    /**
     * Return a bitmap to render.
     * It is safe to call it if the IBRenderer is recycled
     * and return null.
     */
    @Nullable
    public abstract Bitmap getBitmap();
}
