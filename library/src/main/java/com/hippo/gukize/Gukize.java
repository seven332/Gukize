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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ValueHelper;
import com.hippo.image.Image;
import com.hippo.image.ImageData;
import com.hippo.streampipe.InputStreamPipe;

import java.io.IOException;

public class Gukize {
    private Gukize() {}

    private static Conaco<ImageData> sConaco;

    private static void init(Builder builder) {
        if (sConaco != null) {
            throw new IllegalStateException("Can't init Gukize twice");
        }
        builder.isValid();
        sConaco = builder.build();
    }

    static Conaco<ImageData> getConaco() {
        if (sConaco == null) {
            throw new IllegalStateException("Please call Gukize.init(build)");
        }
        return sConaco;
    }

    private static class ImageDataHelper implements ValueHelper<ImageData> {

        @Nullable
        @Override
        public ImageData decode(@NonNull InputStreamPipe isPipe) {
            try {
                isPipe.obtain();
                return Image.decode(isPipe.open(), false);
            } catch (IOException e) {
                return null;
            } finally {
                isPipe.close();
                isPipe.release();
            }
        }

        @Override
        public int sizeOf(@NonNull String key, @NonNull ImageData value) {
            return value.getByteCount();
        }

        @Override
        public void onAddToMemoryCache(@NonNull String key, @NonNull ImageData value) {
            value.addReference();
        }

        @Override
        public void onRemoveFromMemoryCache(@NonNull String key, @NonNull ImageData value) {
            value.removeReference();
            // If ImageData is not referenced, it must be used by any ImageBitmap.
            // Recycle it now to avoid memory leak.
            if (!value.isReferenced()) {
                value.recycle();
            }
        }

        @Override
        public boolean useMemoryCache(@NonNull String key, @Nullable ImageData value) {
            return true;
        }
    }

    public static class Builder extends Conaco.Builder<ImageData> {

        public Builder() {
            objectHelper = new ImageDataHelper();
        }

        @Override
        public void isValid() throws IllegalStateException {
            super.isValid();
            if (!hasMemoryCache) {
                throw new IllegalStateException("Gukize must support memory cache");
            }
            if (!(objectHelper instanceof ImageDataHelper)) {
                throw new IllegalStateException("Don't assign objectHelper");
            }
        }
    }
}
