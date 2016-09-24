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

class BRenderer extends IBRenderer {

    private final int mWidth;
    private final int mHeight;
    private final boolean mOpaque;

    @Nullable
    private Bitmap mBitmap;

    public BRenderer(@NonNull IBData data, @NonNull Bitmap bitmap) {
        super(data);
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mOpaque = bitmap.getConfig() == Bitmap.Config.RGB_565;
        mBitmap = bitmap;
    }

    @Override
    public void recycle() {
        // Don't recycle the bitmap, let BData recycle bitmap
        mBitmap = null;
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
        return false;
    }

    @Override
    public int getCurrentDelay() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void reset() {}

    @Override
    public void advance() {}

    @Nullable
    @Override
    public Bitmap getBitmap() {
        return mBitmap;
    }
}
