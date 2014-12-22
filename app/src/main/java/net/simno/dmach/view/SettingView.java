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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.simno.dmach.R;
import net.simno.dmach.model.Setting;

public final class SettingView extends PdView {

    public interface OnSettingChangedListener {
        public void onSettingChanged(float x, float y);
    }

    private Paint mShapePaint;
    private Paint mTextPaint;
    private float mShapeStrokeWidth;
    private float mTextSize;
    private int mBackgroundColor;
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

    protected void init() {
        mBackgroundColor = getResources().getColor(R.color.gamboge);
        mTextSize = getResources().getDimension(R.dimen.text_size_setting);
        mCircleRadius = getResources().getDimension(R.dimen.circle_radius);

        mShapeStrokeWidth = getResources().getDimension(R.dimen.shape_stroke_width);
        mShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShapePaint.setStrokeWidth(mShapeStrokeWidth);
        mShapePaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(getResources().getColor(R.color.dune));
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTypeface(FontCache.get("fonts/saxmono.ttf", getContext().getApplicationContext()));

        mShapePaint.setColor(getResources().getColor(R.color.colonial));
    }

    public float getPdX() {
        return xToPd(mX);
    }

    public float getPdY() {
        return yToPd(mY);
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
        mX = pdToX(setting.getX());
        mY = pdToY(setting.getY());
        mHText = setting.getHText();
        mVText = setting.getVText();

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
        canvas.drawColor(mBackgroundColor);
        if (!TextUtils.isEmpty(mHText)) {
            canvas.drawText(mHText, mOriginX, mOriginY, mTextPaint);
        }
        if (!TextUtils.isEmpty(mVText)) {
            canvas.drawTextOnPath(mVText, mPath, mHOffset, mVOffset, mTextPaint);
        }
        canvas.drawCircle(mX, mY, mCircleRadius, mShapePaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
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
