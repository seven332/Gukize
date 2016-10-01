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
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.hippo.yorozuya.thread.InfiniteThreadExecutor;
import com.hippo.yorozuya.thread.PriorityThreadFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A drawable to draw {@link IBRenderer}.
 */
class IBDrawable extends Drawable implements Animatable, Runnable {

    private static final String LOG_TAG = IBDrawable.class.getSimpleName();

    private static final Executor sExecutor = new InfiniteThreadExecutor(
            3, // Keep 3 core thread
            3000, // 3000ms
            new LinkedList<Runnable>(),
            new PriorityThreadFactory(LOG_TAG, Process.THREAD_PRIORITY_BACKGROUND)
    );

    private final IBRenderer mIBRenderer;
    private final Paint mPaint;
    private final Task mTask;

    /** Whether the drawable has an animation callback posted. */
    private boolean mRunning;

    /** Whether the drawable should animate when visible. */
    private boolean mAnimating;

    public IBDrawable(@NonNull IBRenderer ibRenderer) {
        mIBRenderer = ibRenderer;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        mTask = new Task();
        mTask.executeOnExecutor(sExecutor);
    }

    public void recycle() {
        mTask.addTask(Task.RECYCLE);
    }

    public boolean isAnimated() {
        return mIBRenderer.isAnimated();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);
        if (mIBRenderer.isAnimated()) {
            if (visible) {
                if (restart || changed) {
                    final boolean next = !restart && mRunning;
                    setFrame(next, mAnimating);
                }
            } else {
                unscheduleSelf(this);
            }
        }
        return changed;
    }

    @Override
    public void start() {
        mAnimating = true;

        if (mIBRenderer.isAnimated() && !isRunning()) {
            // Start from 0th frame.
            setFrame(false, true);
        }
    }

    @Override
    public void stop() {
        mAnimating = false;

        if (mIBRenderer.isAnimated() && isRunning()) {
            unscheduleSelf(this);
        }
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public void run() {
        setFrame(true, true);
    }

    // resetOrNext, false for reset, true for next
    private void setFrame(boolean resetOrNext, boolean animate) {
        // Check recycled
        if (mTask.isRecycled() || mIBRenderer.isRecycled()) {
            return;
        }

        mAnimating = animate;
        unscheduleSelf(this);
        if (animate) {
            mRunning = true;
        }

        // Add task
        final int task = resetOrNext ? (animate ? Task.ADVANCE_ANIMATE : Task.ADVANCE) :
                (animate ? Task.RESET_ANIMATE : Task.RESET);
        mTask.addTask(task);
    }

    @Override
    public void unscheduleSelf(@NonNull Runnable what) {
        mRunning = false;
        super.unscheduleSelf(what);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Bitmap bitmap = mIBRenderer.getBitmap();
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, getBounds(), mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return mIBRenderer.isOpaque() ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIBRenderer.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mIBRenderer.getHeight();
    }

    // Task keeps till recycled
    private class Task extends AsyncTask<Void, Long, Void> {

        private static final int RESET = 0;
        private static final int RESET_ANIMATE = 1;
        private static final int ADVANCE = 2;
        private static final int ADVANCE_ANIMATE = 3;
        private static final int RECYCLE = 4;

        private boolean mRecycled;
        private final Deque<Integer> mTaskStack = new LinkedList<>();
        private final Deque<Long> mTimeStack = new LinkedList<>();
        private final Lock mThreadLock = new ReentrantLock();
        private final Object mWaitLock = new Object();

        public void addTask(int task) {
            mThreadLock.lock();

            if (mRecycled) {
                return;
            }
            if (task == RECYCLE) {
                mTaskStack.clear();
                mRecycled = true;
            }
            mTaskStack.addLast(task);
            mTimeStack.addLast(SystemClock.uptimeMillis());

            synchronized (mWaitLock) {
                mWaitLock.notify();
            }

            mThreadLock.unlock();
        }

        public boolean isRecycled() {
            return mRecycled;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (;;) {
                mThreadLock.lock();
                final Integer task = mTaskStack.pollFirst();
                final Long time = mTimeStack.pollFirst();
                if (task == null) {
                    mThreadLock.unlock();
                    synchronized (mWaitLock) {
                        try {
                            mWaitLock.wait();
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    }
                    continue;
                } else {
                    mThreadLock.unlock();
                }

                switch (task) {
                    case RESET:
                        mIBRenderer.reset();
                        publishProgress((Long) null);
                        break;
                    case RESET_ANIMATE:
                        mIBRenderer.reset();
                        publishProgress(time);
                        break;
                    case ADVANCE:
                        mIBRenderer.advance();
                        publishProgress((Long) null);
                        break;
                    case ADVANCE_ANIMATE:
                        mIBRenderer.advance();
                        publishProgress(time);
                        break;
                    case RECYCLE:
                        // mIBRenderer will be recycled in onPostExecute(),
                        // just return now.
                        return null;
                    default:
                        throw new IllegalStateException("Invalid task: " + task);
                }
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            invalidateSelf();
            final Long time = values[0];
            if (time != null) {
                scheduleSelf(IBDrawable.this, time + mIBRenderer.getCurrentDelay());
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mIBRenderer.recycle();
        }
    }
}
