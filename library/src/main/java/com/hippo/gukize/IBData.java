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

/**
 * This class store image data, {@link android.graphics.Bitmap}
 * or {@link com.hippo.image.ImageData}.
 */
abstract class IBData {

    private int mReference;

    /**
     * Return true if the IBData is referenced.
     */
    public boolean isReferenced() {
        return mReference != 0;
    }

    /**
     * Add reference keep IBData away from recycling.
     */
    public void addReference() {
        ++mReference;
    }

    /**
     * Remove reference. If no reference attached to
     * the IBData, {@link #recycle} will be called automatically.
     */
    public void removeReference() {
        --mReference;
        // Check reference valid
        if (mReference < 0) {
            throw new IllegalStateException();
        }

        // Auto recycle
        if (mReference == 0) {
            recycle();
        }
    }

    /**
     * Recycle the IBData.
     */
    public abstract void recycle();

    /**
     * Return true if the IBData is recycled.
     */
    public abstract boolean isRecycled();

    /**
     * Return the byte count of the IBData.
     * Throw {@code IllegalStateException}
     * if the IBData is recycled.
     */
    public abstract int getByteCount();

    /**
     * Create a IBRenderer of the IBData
     * Throw {@code IllegalStateException}
     * if the IBData is recycled.
     */
    @NonNull
    public abstract IBRenderer createRenderer();
}
