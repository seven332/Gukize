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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.image.ImageData;

class IData extends IBData {

    @Nullable
    private ImageData mImageData;

    public IData(@NonNull ImageData imageData) {
        mImageData = imageData;
        imageData.addReference();
    }

    @Override
    public void recycle() {
        // Check referenced
        if (isReferenced()) {
            throw new IllegalStateException("Can't recycle a referenced IBData.");
        }
        if (mImageData != null) {
            mImageData.removeReference();
            // Only IData use the ImageData,
            // so if IData is not referenced,
            // ImageData is not referenced.
            // It is safe to call ImageData.recycle().
            mImageData.recycle();
            mImageData = null;
        }
    }

    @Override
    public boolean isRecycled() {
        return mImageData == null;
    }

    @Override
    public int getByteCount() {
        // Check recycled
        if (mImageData == null) {
            throw new IllegalStateException("Can't get byte count from a recycled IData.");
        }
        return mImageData.getByteCount();
    }

    @NonNull
    @Override
    public IBRenderer createRenderer() {
        // Check recycled
        if (mImageData == null) {
            throw new IllegalStateException("Can't get byte count from a recycled IData.");
        }
        return new IRenderer(this, mImageData.createImageRenderer());
    }
}
