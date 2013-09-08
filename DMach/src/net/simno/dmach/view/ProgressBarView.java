/**
 * Copyright: 2011 Android Aalto Community
 * Copyright (C) 2013 Simon Norberg
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.simno.dmach.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import net.simno.dmach.DMachActivity.OnBeatListener;
import net.simno.dmach.DMachActivity.OnTempoChangedListener;
import net.simno.dmach.DMachActivity.OnVisibilityListener;

/**
 * ProgressBarView is an animated progress bar showing the current step in a sequence.
 */
public final class ProgressBarView extends View
implements OnBeatListener, OnTempoChangedListener, OnVisibilityListener {

    private static final int BAR_COLOR = Color.YELLOW;
    private static final int BAR_TRANSPARENCY = 127;
    private static final int MARGIN_DP = 8;

    private boolean mIsVisible;
    private int mHeight;
    private int mBarPos;
    private int mStepMargin;
    private int mStepWidth;
    private int mStepWidthAndMargin;
    private long mUpdateDelay;
    private Handler mProgressHandler;
    private ProgressBarView mThisView = this;
    private ShapeDrawable mProgressBar;
    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            moveBar();
            mThisView.invalidate();
        }
    };

    public ProgressBarView(Context context) {
        super(context);
    }

    public ProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @param context
     * @param width The total width of the view
     * @param height The total height of the view
     * @param steps The number of steps in the sequence
     * @param tempo The tempo in BPM
     */
    public ProgressBarView(Context context, int width, int height, int steps) {
        super(context);
        mHeight = height;
        mStepMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_DP,
                getResources().getDisplayMetrics());
        mStepWidth = (width - ((steps - 1) * mStepMargin)) / steps;
        mStepWidthAndMargin = mStepWidth + mStepMargin;
        mProgressBar = new ShapeDrawable(new RectShape());
        mProgressBar.getPaint().setColor(BAR_COLOR);
        mProgressBar.setAlpha(BAR_TRANSPARENCY);
        mProgressHandler = new Handler();
    }

    /**
     * Calculate the time in milliseconds per pixel in one step. It can never be less than 1.
     *
     * @param tempo The tempo in BPM
     */
    private void setUpdateDelay(int tempo) {
        mUpdateDelay = Math.max(Math.round((30000.0 / tempo) / mStepWidthAndMargin), 1);
    }

    /**
     * Sets the bounds of the bar and then increases its position by one
     */
    private void moveBar() {
        synchronized (mProgressBar) {
            mProgressBar.setBounds(mBarPos, 0, mBarPos + mStepWidth, mHeight);
        }
        ++mBarPos;
    }

    /* (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (true == mIsVisible) {
            synchronized (mProgressBar) {
                mProgressBar.draw(canvas);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.simno.android.dmach.DMachActivity.OnTempoChangedListener#onTempoChanged(int)
     */
    @Override
    public void onTempoChanged(int tempo) {
        setUpdateDelay(tempo);
    }

    /* (non-Javadoc)
     * @see net.simno.android.dmach.DMachActivity.OnBeatListener#onBeat(int)
     */
    @Override
    public void onBeat(int beat) {
        // Set the bar position to the current step
        mBarPos = beat * mStepWidthAndMargin;

        // Remove any callbacks from the queue
        mProgressHandler.removeCallbacks(mProgressRunnable);

        // Move the bar through all pixels in the current step
        for (int i = 0; i < mStepWidthAndMargin; ++i) {
            mProgressHandler.postDelayed(mProgressRunnable, i * mUpdateDelay);
        }
    }

    /* (non-Javadoc)
     * @see net.simno.android.dmach.DMachActivity.OnVisibilityListener#onShow()
     */
    @Override
    public void onShow() {
        mIsVisible = true;
    }

    /* (non-Javadoc)
     * @see net.simno.android.dmach.DMachActivity.OnVisibilityListener#onHide()
     */
    @Override
    public void onHide() {
        mIsVisible = false;
        mThisView.invalidate();
    }
}