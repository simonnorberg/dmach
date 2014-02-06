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
        public void onStepChanged(int index);
    }
    
    private static final int BACKGROUND_COLOR = Color.parseColor("#EBEBAF");
    private static final int UNCHECKED_COLOR = Color.parseColor("#C1BF87");
    private static final int CHECKED_COLOR = Color.parseColor("#B02B2F");
    private static final int STEPS = 16;
    private static final int CHANNELS = 6;

    private ArrayList<Step> mSteps = new ArrayList<Step>();
    private OnStepChangedListener mListener;
    private Paint mUncheckedPaint;
    private Paint mCheckedPaint;
    private boolean mChecked;
    private int mWidth;
    private int mHeight;
    private int mMargin;
    private float mStepWidth;
    private float mStepHeight;
    private float mStepMarginWidth;
    private float mStepMarginHeight;
    
    private class Step {
        private final RectF rect;
        private boolean checked;
        
        Step(RectF rect) {
            this.rect = rect;
        }

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

    private void notifyOnStepChanged(int index) {
        if (mListener != null) {
            mListener.onStepChanged(index);
        }
    }

    private void init() {
        setMinimumHeight(100);
        setMinimumWidth(100);
        
        mUncheckedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUncheckedPaint.setColor(UNCHECKED_COLOR);
        mUncheckedPaint.setStyle(Paint.Style.FILL);
        
        mCheckedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCheckedPaint.setColor(CHECKED_COLOR);
        mCheckedPaint.setStyle(Paint.Style.FILL);
        
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        mMargin = Math.round(3 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void initSteps() {
        for (int ch = 0; ch < CHANNELS; ++ch) {
            for (int st = 0; st < STEPS; ++st) {
                float left = st * mStepMarginWidth;
                float right = left + mStepWidth;
                float top = ch * mStepMarginHeight;
                float bottom = top + mStepHeight;
                mSteps.add(new Step(new RectF(left, top, right, bottom)));
            }
        }
    }    
    
    private void onActionDown(float x, float y) {
        int index = calculateIndex(x, y);
        if (index == -1) {
            return;
        }
        mChecked = mSteps.get(index).checked;
        mSteps.get(index).toggle();
        notifyOnStepChanged(index);
        invalidate();
    }
    
    private void onActionMove(float x, float y) {
        int index = calculateIndex(x, y);
        if (index == -1) {
            return;
        }
        if (mSteps.get(index).checked == mChecked) {
            mSteps.get(index).toggle();
            notifyOnStepChanged(index);
            invalidate();
        }
    }

    private void onActionUp(float x, float y) {
        
    }
    
    private int calculateIndex(float x, float y) {
        if (x < 0 || y < 0 || x > mWidth || y > mHeight) {
            return -1;
        }
        int channel = (int) (y / mStepMarginHeight);
        int step = (int) (x / mStepMarginWidth);
        return channel * STEPS + step;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);
        for (Step s : mSteps) {
            canvas.drawRect(s.rect, s.checked ? mCheckedPaint : mUncheckedPaint);
        }

    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != 0 && h != 0) {
            mWidth = w;
            mHeight = h;
            mStepWidth = (float) ((w - ((STEPS - 1.0) * mMargin)) / STEPS);
            mStepHeight = (float) ((h - ((CHANNELS - 1.0) * mMargin)) / CHANNELS);
            mStepMarginWidth = mStepWidth + mMargin;
            mStepMarginHeight = mStepHeight + mMargin;
            initSteps();            
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
        case MotionEvent.ACTION_UP:
//            onActionUp(event.getX(), event.getY());
//            changeStep(event.getX(), event.getY());
//            break;
        }
        return true;
    }
}
