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

import net.simno.dmach.R;
import android.content.Context;
import android.content.res.TypedArray;
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
        public void onStepChanged(int group, int channel, int step);
    }
    
    private static final int BACKGROUND_COLOR = Color.parseColor("#EBEBAF");
    private static final int UNCHECKED_COLOR = Color.parseColor("#C1BF87");
    private static final int CHECKED_COLOR = Color.parseColor("#B02B2F");
    private static final int STEPS = 16;
    private static final int CHANNELS = 3;

    private float mStepWidth;
    private float mHeight;
    private int mMargin;
    private int mGroup;
    
    private Paint mUncheckedPaint;
    private Paint mCheckedPaint;
    private ArrayList<Step> mSteps = new ArrayList<Step>();
    private OnStepChangedListener mListener;
    
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
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,R.styleable.SequencerView, 0, 0);
        try {
            mGroup = a.getInt(R.styleable.SequencerView_group, 0);
            } finally {
                a.recycle();
            }
        init();
    }

    public SequencerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SequencerView, 0, 0);
        try {
            mGroup = a.getInt(R.styleable.SequencerView_group, 0);
            } finally {
                a.recycle();
            }
        init();
    }

    public void setOnStepChangedListener(OnStepChangedListener listener) {
        mListener = listener;
    }

    private void notifyOnStepChanged(int channel, int step) {
        if (mListener != null) {
            mListener.onStepChanged(mGroup, channel, step);
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
                float left = st * (mStepWidth + mMargin);
                float right = left + mStepWidth;
                float top = ch * (mHeight + mMargin);
                float bottom = top + mHeight;
                mSteps.add(new Step(new RectF(left, top, right, bottom)));
            }
        }
    }
    
    private void changeStep(float x, float y) {
        if (x >= 0 && y >= 0) {
            int channel = (int) (y / (mHeight + mMargin));
            int step = (int) (x / (mStepWidth + mMargin));
            int index = channel * STEPS + step;
            if (index < mSteps.size()) {
                notifyOnStepChanged(channel, step);
                mSteps.get(index).toggle();
                invalidate();    
            }
        }
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
            mStepWidth = (float) ((w - ((STEPS - 1.0) * mMargin)) / STEPS);
            mHeight = (float) ((h - ((CHANNELS - 1.0) * mMargin)) / CHANNELS);
            initSteps();            
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            changeStep(event.getX(), event.getY());
            break;
        }
        return true;
    }
}