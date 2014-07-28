/*
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import net.simno.dmach.DMachActivity;

import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;

public final class ProgressBarView extends View {

    private static final int BAR_COLOR = Color.YELLOW;
    private static final int BAR_TRANSPARENCY = 127;
    private static final int MARGIN = 3;

    private int mHeight;
    private int mMargin;
    private int mDirtyLeft;
    private float mStepWidth;
    private float mStepWidthMargin;
    private PdUiDispatcher mDispatcher;
    private ShapeDrawable mProgressBar;

    public ProgressBarView(Context context) {
        super(context);
        init();
    }

    public ProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MARGIN, getResources().getDisplayMetrics());

        mDispatcher = new PdUiDispatcher();
        PdBase.setReceiver(mDispatcher);
        mDispatcher.addListener("beat", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, float x) {
                int left  = (int) (x * mStepWidthMargin);
                int right = (int) Math.ceil(left + mStepWidth);
                mProgressBar.setBounds(left, 0, right, mHeight);
                if (x > 0) {
                    invalidate(mDirtyLeft, 0, right, mHeight);
                } else {
                    invalidate();
                }
                mDirtyLeft = left;
            }
        });

        mProgressBar = new ShapeDrawable(new RectShape());
        mProgressBar.getPaint().setColor(BAR_COLOR);
        mProgressBar.setAlpha(BAR_TRANSPARENCY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mProgressBar.draw(canvas);
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != 0 && h != 0) {
            mHeight = h;
            mStepWidth = (float) ((w - ((DMachActivity.STEPS - 1.0) * mMargin)) / DMachActivity.STEPS);
            mStepWidthMargin = mStepWidth + mMargin;
            invalidate();
        }
    }

    public void cleanup() {
        mDispatcher.release();
    }
}
