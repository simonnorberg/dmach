/**
 * Copyright (C) 2014 Simon Norberg
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

import java.util.ArrayList;
import java.util.Iterator;

import net.simno.dmach.DMach;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public final class SequencerView extends View {

    public interface OnStepChangedListener {
        public void onStepChanged(int channel, int step);
    }
    
    private static final int BACKGROUND_COLOR = Color.parseColor("#EBEBAF");
    private static final int UNCHECKED_COLOR = Color.parseColor("#C1BF87");
    private static final int CHECKED_COLOR = Color.parseColor("#B02B2F");
    private static final int MARGIN = 3;

    private final ArrayList<Step> mSequence = new ArrayList<Step>();
    private OnStepChangedListener mListener;
    private Paint mUncheckedPaint;
    private Paint mCheckedPaint;
    private boolean mChecked;
    private int mWidth;
    private int mHeight;
    private int mMargin;
    private float mStepWidth;
    private float mStepHeight;
    private float mStepWidthMargin;
    private float mStepHeightMargin;
    
    private class Step {
        private RectF rect;
        private boolean checked;
        
        void toggle() {
            checked = !checked;
        }
    }

    public SequencerView(Context context) {
        super(context);
        init();
    }

    public SequencerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SequencerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setOnStepChangedListener(OnStepChangedListener listener) {
        mListener = listener;
    }

    private void notifyOnStepChanged(int channel, int step) {
        if (mListener != null) {
            mListener.onStepChanged(channel, step);
        }
    }

    public void setChecked(int[] sequence) {
        Iterator<Step> i = mSequence.iterator();
        for (int c = 0; c < (DMach.CHANNELS); ++c) {
            for (int s = 0; s < DMach.STEPS; ++s) {
                if (i.hasNext()) {
                    int mask = DMach.MASKS[c % (DMach.CHANNELS / DMach.GROUPS)];
                    int index = s + ((c / (DMach.CHANNELS / DMach.GROUPS)) * DMach.STEPS);
                    int value = sequence[index] & mask;
                    i.next().checked = value != 0 ? true : false;
                }
            }
        }
        invalidate();
    }
    
    private void init() {
        setMinimumHeight(100);
        setMinimumWidth(100);
        
        mUncheckedPaint = new Paint();
        mUncheckedPaint.setColor(UNCHECKED_COLOR);
        mUncheckedPaint.setStyle(Paint.Style.FILL);
        
        mCheckedPaint = new Paint();
        mCheckedPaint.setColor(CHECKED_COLOR);
        mCheckedPaint.setStyle(Paint.Style.FILL);
        
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        mMargin = Math.round(MARGIN * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        
        for (int channel = 0; channel < DMach.CHANNELS; ++channel) {
            for (int step = 0; step < DMach.STEPS; ++step) {
                mSequence.add(new Step());
            }
        }
    }
    
    private void initSteps() {
        for (int channel = 0; channel < DMach.CHANNELS; ++channel) {
            for (int step = 0; step < DMach.STEPS; ++step) {
                float left = step * mStepWidthMargin;
                float right = left + mStepWidth;
                float top = channel * mStepHeightMargin;
                float bottom = top + mStepHeight;
                int index = (channel * DMach.STEPS) + step;
                mSequence.get(index).rect = new RectF(left, top, right, bottom);
            }
        }
    }
    
    private void onActionDown(float x, float y) {
        if (!isValidXY(x, y)) {
            return;
        }
        
        int channel = (int) (y / mStepHeightMargin);
        int step = (int) (x / mStepWidthMargin);
        int index = channel * DMach.STEPS + step;
        
        mChecked = mSequence.get(index).checked;
        mSequence.get(index).toggle();
        notifyOnStepChanged(channel, step);
        invalidate();

    }
    
    private void onActionMove(float x, float y) {
        if (!isValidXY(x, y)) {
            return;
        }

        int channel = (int) (y / mStepHeightMargin);
        int step = (int) (x / mStepWidthMargin);
        int index = channel * DMach.STEPS + step;
        
        if (mSequence.get(index).checked == mChecked) {
            mSequence.get(index).toggle();
            notifyOnStepChanged(channel, step);
            invalidate();
        }
    }
    
    private boolean isValidXY(float x, float y) {
        if (x < 0 || y < 0 || x > mWidth || y > mHeight) {
            return false;
        }
        return true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);
        for (Step s : mSequence) {
            canvas.drawRect(s.rect, s.checked ? mCheckedPaint : mUncheckedPaint);
        }
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != 0 && h != 0) {
            mWidth = w;
            mHeight = h;
            mStepWidth = (float) ((w - ((DMach.STEPS - 1.0) * mMargin)) / DMach.STEPS);
            mStepHeight = (float) ((h - ((DMach.CHANNELS - 1.0) * mMargin)) / DMach.CHANNELS);
            mStepWidthMargin = mStepWidth + mMargin;
            mStepHeightMargin = mStepHeight + mMargin;
            initSteps();
            invalidate();
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            onActionDown(event.getX(), event.getY());
            break;
        case MotionEvent.ACTION_MOVE:
            onActionMove(event.getX(), event.getY());
            break;
        }
        return true;
    }
}
