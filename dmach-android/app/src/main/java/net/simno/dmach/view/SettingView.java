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
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import net.simno.dmach.model.Setting;

public final class SettingView extends PdView {

    public interface OnSettingChangedListener {
        public void onSettingChanged(float x, float y);
    }

    private static final int CIRCLE_RADIUS = 18;
    private static final int CIRCLE_COLOR = Color.parseColor("#EBEBAF");
    private static final int BACKGROUND_COLOR = Color.parseColor("#E9950A");

    private float mCircleRadius;
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
    }

    public SettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        mCircleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CIRCLE_RADIUS, mDm);
        mShapePaint.setColor(CIRCLE_COLOR);
    }

    @Override
    protected float getMinX() {
        return mCircleRadius + (mShapeStrokeWidth / 2f);
    }

    @Override
    protected float getMinY() {
        return getMinX();
    }

    @Override
    protected float getMaxX() {
        return getWidth() - getMinX();
    }

    @Override
    protected float getMaxY() {
        return getHeight() - getMinY();
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
        mOriginX = (getWidth() / 2f) - mHBounds.centerX();
        mOriginY = getHeight() - (mTextSize * 0.4f);

        mTextPaint.getTextBounds(mVText, 0, mVText.length(), mVBounds);
        mPath.reset();
        mPath.moveTo(0, (getHeight() / 2f) + mVBounds.centerX());
        mPath.lineTo(0, 0);
        mHOffset = 0;
        mVOffset = (int) mTextSize;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);
        if (!TextUtils.isEmpty(mHText)) {
            canvas.drawText(mHText, mOriginX, mOriginY, mTextPaint);
        }
        if (!TextUtils.isEmpty(mVText)) {
            canvas.drawTextOnPath(mVText, mPath, mHOffset, mVOffset, mTextPaint);
        }
        canvas.drawCircle(mX, mY, mCircleRadius, mShapePaint);
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
