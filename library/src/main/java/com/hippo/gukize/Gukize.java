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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hippo.conaco.Conaco;
import com.hippo.conaco.ValueHelper;
import com.hippo.image.BitmapDecoder;
import com.hippo.image.Image;
import com.hippo.image.ImageData;
import com.hippo.image.ImageInfo;
import com.hippo.image.ImageRenderer;
import com.hippo.streampipe.InputStreamPipe;

import java.io.IOException;

public class Gukize {
    private Gukize() {}

    private static Conaco<IBData> sConaco;

    public static void init(Builder builder) {
        if (sConaco != null) {
            throw new IllegalStateException("Can't init Gukize twice");
        }
        builder.isValid();
        IBDrawable.init(builder.coreAnimatedThreadCount);
        sConaco = builder.build();
    }

    public static Conaco<IBData> getConaco() {
        if (sConaco == null) {
            throw new IllegalStateException("Please call Gukize.init(build)");
        }
        return sConaco;
    }

    private static class ImageDataHelper implements ValueHelper<IBData> {

        private static final String LOG_TAG = ImageDataHelper.class.getSimpleName();

        @Nullable
        @Override
        public IBData decode(@NonNull InputStreamPipe isPipe) {
            try {
                isPipe.obtain();

                // Get image info
                final ImageInfo info = new ImageInfo();
                if (!BitmapDecoder.decode(isPipe.open(), info)) {
                    Log.w(LOG_TAG, "This InputSteam is not a Image.");
                    return null;
                }
                isPipe.close();

                if (info.frameCount == 1) {
                    // It is a static image, use Bitmap
                    final Bitmap bitmap = BitmapDecoder.decode(isPipe.open());
                    if (bitmap != null) {
                        return new BData(bitmap);
                    } else {
                        return null;
                    }
                } else {
                    // It may be a animated image
                    final ImageData imageData = Image.decode(isPipe.open());
                    if (imageData != null) {
                        if (imageData.getFrameCount() == 1) {
                            // It is a static image, draw image to Bitmap
                            final Bitmap bitmap;
                            try {
                                bitmap = Bitmap.createBitmap(imageData.getWidth(), imageData.getHeight(),
                                        imageData.isOpaque() ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888);
                            } catch (OutOfMemoryError e) {
                                Log.d(LOG_TAG, "Can't create Bitmap, out of memory.");
                                imageData.recycle();
                                return null;
                            }

                            final ImageRenderer imageRenderer = imageData.createImageRenderer();
                            imageRenderer.render(bitmap, 0, 0, 0, 0, imageData.getWidth(), imageData.getHeight(), 1, false, 0);
                            imageRenderer.recycle();
                            imageData.recycle();

                            return new BData(bitmap);
                        } else {
                            // It is a animated
                            // Fix odd delay
                            imageData.setBrowserCompat(true);
                            return new IData(imageData);
                        }
                    } else {
                        return null;
                    }
                }
            } catch (IOException e) {
                return null;
            } finally {
                isPipe.close();
                isPipe.release();
            }
        }

        @Override
        public int sizeOf(@NonNull String key, @NonNull IBData value) {
            return value.getByteCount();
        }

        @Override
        public void onAddToMemoryCache(@NonNull String key, @NonNull IBData value) {
            value.addReference();
        }

        @Override
        public void onRemoveFromMemoryCache(@NonNull String key, @NonNull IBData value) {
            value.removeReference();
        }

        @Override
        public boolean useMemoryCache(@NonNull String key, @Nullable IBData value) {
            return true;
        }
    }

    public static class Builder extends Conaco.Builder<IBData> {

        /**
         * The core thread count for animated image.
         * 0 ~ 3 is fine. More animated image for large number.
         */
        public int coreAnimatedThreadCount = 0;

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
