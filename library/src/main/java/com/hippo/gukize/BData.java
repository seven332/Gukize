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

class BData extends IBData {

    @Nullable
    private Bitmap mBitmap;

    public BData(@NonNull Bitmap bitmap) {
        mBitmap = bitmap;
    }

    @Override
    public void recycle() {
        // Check referenced
        if (isReferenced()) {
            throw new IllegalStateException("Can't recycle a referenced IBData.");
        }
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    public boolean isRecycled() {
        return mBitmap == null;
    }

    @Override
    public int getByteCount() {
        // Check recycled
        if (mBitmap == null) {
            throw new IllegalStateException("Can't get byte count from a recycled IData.");
        }
        return mBitmap.getRowBytes() * mBitmap.getHeight();
    }

    @NonNull
    @Override
    public IBRenderer createRenderer() {
        // Check recycled
        if (mBitmap == null) {
            throw new IllegalStateException("Can't get byte count from a recycled IData.");
        }
        return new BRenderer(this, mBitmap);
    }
}
