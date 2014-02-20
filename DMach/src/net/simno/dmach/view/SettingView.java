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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import net.simno.dmach.model.Setting;

public final class SettingView extends View {

    public interface OnSettingChangedListener {
        public void onSettingChanged(float x, float y);
    }

    private static final int BACKGROUND_COLOR = Color.parseColor("#E9950A");
    private static final int CIRCLE_COLOR = Color.parseColor("#EBEBAF");
    private static final int TEXT_COLOR = Color.parseColor("#302E2C");
    private static final int CIRCLE_RADIUS = 18;
    private static final int CIRCLE_STROKE_WIDTH = 4;
    private static final int TEXT_SIZE = 22;

    private float mCircleRadius;
    private float mCircleStrokeWidth;
    private float mTextSize;
    private Paint mCirclePaint;
    private Paint mTextPaint;
    private OnSettingChangedListener mListener;
    private Rect mHBounds = new Rect();
    private Rect mVBounds = new Rect();
    private Path mPath = new Path();
    private String mHText;
    private String mVText;
    private float mX;
    private float mY;
    private float mOriginX;
    private float mOriginY;
    private int mHOffset;
    private int mVOffset;

    public SettingView(Context context) {
        super(context);
        init();
    }

    public SettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        mCircleRadius = Math.round(CIRCLE_RADIUS * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        mCircleStrokeWidth = Math.round(CIRCLE_STROKE_WIDTH * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        mTextSize = Math.round(TEXT_SIZE * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(CIRCLE_COLOR);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
        mCirclePaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStyle(Paint.Style.FILL);
        Typeface saxmono = Typeface.createFromAsset(getContext().getAssets(), "fonts/saxmono.ttf");
        mTextPaint.setTypeface(saxmono);
    }

    private float getMinX() {
        return mCircleRadius + (mCircleStrokeWidth / 2);
    }

    private float getMinY() {
        return getMinX();
    }

    private float getMaxX() {
        return getWidth() - getMinX();
    }

    private float getMaxY() {
        return getHeight() - getMinY();
    }

    private float getValidX(float x) {
        float min = getMinX();
        if (x < min) {
            return min;
        }
        float max = getMaxX();
        if (x > max) {
            return max;
        }
        return x;
    }

    private float getValidY(float y) {
        float min = getMinY();
        if (y < min) {
            return min;
        }
        float max = getMaxY();
        if (y > max) {
            return max;
        }
        return y;
    }

    private float pdToX(float pdX) {
        float width = getMaxX() - getMinX();
        return (pdX * width) + getMinX();
    }

    private float pdToY(float pdY) {
        float height = getMaxY() - getMinY();
        return ((1 - pdY) * height) + getMinY();
    }

    private float xToPd(float x) {
        float width = getMaxX() - getMinX();
        float pdX = x - getMinX();
        if (pdX > 0) {
            pdX /= width;
        }
        return pdX;
    }

    private float yToPd(float y) {
        float height = getMaxY() - getMinY();
        float pdY = y - getMinY();
        if (pdY > 0) {
            pdY /= height;
        }
        return 1 - pdY;
    }

    private void notifyOnSettingChanged() {
        if (mListener != null) {
            mListener.onSettingChanged(xToPd(mX), yToPd(mY));
        }
    }

    public void setOnSettingChangedListener(OnSettingChangedListener listener) {
        mListener = listener;
    }

    public void setSetting(Setting setting) {
        mX = pdToX(setting.x);
        mY = pdToY(setting.y);
        mHText = setting.hText;
        mVText = setting.vText;

        mTextPaint.getTextBounds(mHText, 0, mHText.length(), mHBounds);
        mOriginX = (float) ((getWidth() / 2.0) - mHBounds.centerX());
        mOriginY = getHeight() - (mTextSize * 0.4f);

        mTextPaint.getTextBounds(mVText, 0, mVText.length(), mVBounds);
        mPath.reset();
        mPath.moveTo(0, (float) ((getHeight() / 2.0) + mVBounds.centerX()));
        mPath.lineTo(0, 0);
        mHOffset = 0;
        mVOffset = (int) mTextSize;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);
        if (!mHText.isEmpty()) {
            canvas.drawText(mHText, mOriginX, mOriginY, mTextPaint);
        }
        if (!mVText.isEmpty()) {
            canvas.drawTextOnPath(mVText, mPath, mHOffset, mVOffset, mTextPaint);
        }
        canvas.drawCircle(mX, mY, mCircleRadius, mCirclePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
            mX = getValidX(event.getX());
            mY = getValidY(event.getY());
            notifyOnSettingChanged();
            invalidate();
            break;
        }
        return true;
    }
}
