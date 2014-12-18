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
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.simno.dmach.R;

public final class PanView extends PdView {

    public interface OnPanChangedListener {
        public void onPanChanged(float pan);
    }

    private static final String LEFT = "L";
    private static final String RIGHT = "R";

    private OnPanChangedListener mListener;
    private float mPan;
    private float mRectHeight;
    private float mOffset;
    private float mCenter;
    private float mCenterLeft;
    private float mCenterRight;
    private float mOriginX;
    private float mOriginYL;
    private float mOriginYR;
    private Rect mBounds = new Rect();

    public PanView(Context context) {
        super(context);
    }

    public PanView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        mTextSize = getResources().getDimension(R.dimen.text_size_channel);
        mRectHeight = getResources().getDimension(R.dimen.rect_height);
        super.init();
        mShapePaint.setColor(getResources().getColor(R.color.gamboge));
        mOffset = (mShapeStrokeWidth / 2f) + (mRectHeight / 2f);
    }

    public float getPdPan() {
        if (mPan == mCenter){
            return .5f;
        } else {
            return yToPd(mPan);
        }
    }

    @Override
    protected float getMinX() {
        return mShapeStrokeWidth / 2f;
    }

    @Override
    protected float getMinY() {
        return getMinX() + mOffset;
    }

    @Override
    protected float getMaxX() {
        return getWidth() - getMinX();
    }

    @Override
    protected float getMaxY() {
        return getHeight() + getMinX() - mOffset;
    }

    private void notifyOnPanChanged() {
        if (mListener != null) {
            if (mPan == mCenter){
                mListener.onPanChanged(.5f);
            } else {
                mListener.onPanChanged(yToPd(mPan));
            }
        }
    }

    public void setOnPanChangedListener(OnPanChangedListener listener) {
        mListener = listener;
    }

    public void setPan(float pan) {
        mCenter = getWidth() / 2f;

        mCenter = getHeight() / 2f;
        mCenterLeft = mCenter + (mOffset / 2f);
        mCenterRight = mCenter - (mOffset / 2f);

        mTextPaint.getTextBounds(RIGHT, 0, RIGHT.length(), mBounds);
        mOriginX = (getWidth() / 2f) - mBounds.centerX();
        mOriginYL = getMaxY() + (mTextSize * 0.25f);
        mOriginYR = getMinY() + (mTextSize * 0.25f);

        if (pan == 0.5f) {
            mPan = mCenter;
        } else {
            mPan = pdToY(pan);
        }

        invalidate();
    }

    private void makeCenterPanStick() {
        if (mPan > mCenterRight && mPan < mCenterLeft) {
            mPan = mCenter;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(RIGHT, mOriginX, mOriginYR, mTextPaint);
        canvas.drawText(LEFT, mOriginX, mOriginYL, mTextPaint);

        float panOffset = mPan - mOffset;
        canvas.drawRect(getMinX(), panOffset, getMaxX(), panOffset + mRectHeight, mShapePaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mPan = getValidY(event.getY());
                makeCenterPanStick();
                notifyOnPanChanged();
                invalidate();
                break;
        }
        return true;
    }
}
