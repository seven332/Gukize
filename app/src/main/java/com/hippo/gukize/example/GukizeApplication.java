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

package com.hippo.gukize.example;

/*
 * Created by Hippo on 8/23/2016.
 */

import android.app.Application;
import android.os.Debug;
import android.util.Log;

import com.hippo.gukize.Gukize;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.OSUtils;
import com.hippo.yorozuya.SimpleHandler;

import java.io.File;

import okhttp3.OkHttpClient;

public class GukizeApplication extends Application {

    private static final String TAG = GukizeApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Gukize.Builder builder = new Gukize.Builder();
        builder.hasMemoryCache = true;
        builder.memoryCacheMaxSize = Math.min(20 * 1024 * 1024, (int) OSUtils.getAppMaxMemory());
        builder.hasDiskCache = true;
        builder.diskCacheDir = new File(getCacheDir(), "thumb");
        builder.diskCacheMaxSize = 80 * 1024 * 1024; // 80MB
        builder.okHttpClient = new OkHttpClient.Builder().build();
        builder.debug = true;
        Gukize.init(builder);

        debugPrint();
    }

    private void debugPrint() {
        new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Java memory: " + FileUtils.readableByteCount(OSUtils.getAppAllocatedMemory(), false));
                Log.i(TAG, "Native memory: " + FileUtils.readableByteCount(Debug.getNativeHeapAllocatedSize(), false));
                SimpleHandler.getInstance().postDelayed(this, 2000);
            }
        }.run();
    }
}
