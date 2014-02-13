/**
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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.simno.dmach.model.Patch;
import net.simno.dmach.model.PointF;
import net.simno.dmach.model.Setting;

public final class PatchView extends View {
    public interface OnPosChangedListener {
        public void onPosChanged(PointF pos);
    }

    private static final float CIRCLE_RADIUS = 36f;
    private static final float CIRCLE_STROKE_WIDTH = 8f;
    private static final float TEXT_SIZE = 40f;
    private static final int BACKGROUND_COLOR = Color.parseColor("#E9950A");
    private static final int CIRCLE_COLOR = Color.parseColor("#EBEBAF");
    private static final int TEXT_COLOR = Color.parseColor("#B57400");

    private Paint mCirclePaint;
    private Paint mTextPaint;
    private Patch mPatch;
    private OnPosChangedListener mListener;
    private Path mVPath = new Path();
    private Rect mBounds = new Rect();
    private Rect mHBounds = new Rect();
    private Rect mVBounds = new Rect();

    public PatchView(Context context) {
        super(context);
        init();
    }

    public PatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PatchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setMinimumHeight(100);
        setMinimumWidth(100);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(CIRCLE_COLOR);
        mCirclePaint.setStrokeWidth(CIRCLE_STROKE_WIDTH);
        mCirclePaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(TEXT_COLOR);
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setTypeface(Typeface.SANS_SERIF);
        mTextPaint.setStyle(Paint.Style.FILL);

        mPatch = new Patch();
        mPatch.addSetting(new Setting());
        mPatch.addSetting(new Setting());
        mPatch.addSetting(new Setting());
        mPatch.addSetting(new Setting());
        mPatch.addSetting(new Setting());
        mPatch.addSetting(new Setting());
    }

    public void setOnPosChangedListener(OnPosChangedListener listener) {
        mListener = listener;
    }

    private void notifyOnPosChanged(PointF pos) {
        if (mListener != null) mListener.onPosChanged(pos);
    }

    /**
     * Sets the PatchView patch. All setting's parameter values are converted from internal to
     * pixel representation. Cannot be called before the layout process is finished.
     *
     * @param p The new patch with internal values
     */
    public void setPatch(Patch p) {
        mPatch = new Patch(p);
        for (int i = 0; i < mPatch.getCount(); ++i) {
            mPatch.setPos(i, pdToPx(p.getPos(i)));
        }
    }

    /**
     * Sets the selected setting in the patch and then redraws the view.
     *
     * @param index The index of the selected setting
     */
    public void setSelectedSettingIndex(int index) {
        mPatch.setSelectedSettingIndex(index);
        invalidate();
    }

    private float getMinX() {
        return CIRCLE_RADIUS + (CIRCLE_STROKE_WIDTH / 2);
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

    /**
     * Converts a PointF object's coordinates from internal Pure Data representation
     * ([0.0, 1.0], [0.0, 1.0]) to pixel representation
     * ([getMinX(), getMaxX()], [getMinY(), getMaxY()]).
     *
     * @param pos The point to convert
     * @return The new converted point
     */
    private PointF pdToPx(PointF pos) {
        float width = getMaxX() - getMinX();
        float height = getMaxY() - getMinY();
        float x = (pos.getX() * width) + getMinX();
        float y = ((1 - pos.getY()) * height) + getMinY();
        return new PointF(x, y);
    }

    /**
     * Converts a PointF object's coordinates from pixel representation
     * ([getMinX(), getMaxX()], [getMinY(), getMaxY()]) to internal Pure Data representation
     * ([0.0, 1.0], [0.0, 1.0]).
     *
     * @param pos The point to convert
     * @return The new converted point
     */
    private PointF pxToPd(PointF pos) {
        float width = getMaxX() - getMinX();
        float height = getMaxY() - getMinY();
        float x = pos.getX() - getMinX();
        float y = pos.getY() - getMinY();
        if (x > 0) {
            x /= width;
        }
        if (y > 0) {
            y /= height;
        }
        y = 1 - y;
        return new PointF(x, y);
    }

    private void moveCircle(float x, float y) {
        PointF pos = new PointF(getValidX(x), getValidY(y));
        mPatch.setSelectedPos(pos);
        notifyOnPosChanged(pxToPd(pos));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);
        canvas.getClipBounds(mBounds);

        // Draw horizontal text
        String hText = mPatch.getSelectedHText();
        mTextPaint.getTextBounds(hText, 0, hText.length(), mHBounds);
        canvas.drawText(hText, mBounds.centerX() - mHBounds.centerX(),
                mBounds.height() - 10, mTextPaint);

        // Draw vertical text
        String vText = mPatch.getSelectedVText();
        mTextPaint.getTextBounds(vText, 0, vText.length(), mVBounds);
        mVPath.reset();
        mVPath.moveTo(0, mBounds.centerY() + mVBounds.centerX());
        mVPath.lineTo(0, 0);
        canvas.drawTextOnPath(vText, mVPath, 0, mVBounds.height(), mTextPaint);

        // Draw circle
        PointF pos = mPatch.getSelectedPos();
        canvas.drawCircle(pos.getX(), pos.getY(), CIRCLE_RADIUS, mCirclePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
            moveCircle(event.getX(), event.getY());
            break;
        }
        return true;
    }
}