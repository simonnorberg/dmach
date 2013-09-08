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

/**
 * PatchView is a graphical representation ofn the settings in a DMach patch. The setting's two
 * parameters are assigned to the X axis and the Y axis respectively. The parameters can be changed
 * by moving the circle on the canvas.
 */
public final class PatchView extends View {
    /**
     * Interface definition for a callback to be invoked when the circle that represents the
     * setting's parameter values has moved its position.
     */
    public interface OnPosChangedListener {
        /**
         * Called when the circle position has changed.
         *
         * @param pos The new circle position
         */
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

    /**
     * Sets minimum height and width of this view and initializes paint and patch objects.
     */
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

    /**
     * Register a callback to be invoked when the circle position has changed.
     *
     * @param listener The callback that will run
     */
    public void setOnPosChangedListener(OnPosChangedListener listener) {
        mListener = listener;
    }

    /**
     * Notifies the listener that the circle position has changed.
     *
     * @param pos The circle position
     */
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

    /**
     * Gets the minimum horizontal position of the circle center. It depends on the circle radius
     * and stroke width.
     *
     * @return The minimum horizontal position of the circle center
     */
    private float getMinX() {
        return CIRCLE_RADIUS + (CIRCLE_STROKE_WIDTH / 2);
    }

    /**
     * Gets the minimum vertical position of the circle center. It is always the same as the minimum
     * horizontal position.
     *
     * @return The minimum vertical position of the circle center
     */
    private float getMinY() {
        return getMinX();
    }

    /**
     * @return The maximum horizontal position of the circle center
     */
    private float getMaxX() {
        return getWidth() - getMinX();
    }

    /**
     * @return The maximum vertical position of the circle center
     */
    private float getMaxY() {
        return getHeight() - getMinY();
    }

    /**
     * Checks if the provided value is in the allowed horizontal range. If it is not in the allowed
     * horizontal range it will be adjusted before it is returned.
     *
     * @param x The value to check
     * @return The valid value
     */
    private float getValidX(float x) {
        float min = getMinX();
        if (x < min) return min;
        float max = getMaxX();
        if (x > max) return max;
        return x;
    }

    /**
     * Checks if the provided value is in the allowed vertical range. If it is not in the allowed
     * vertical range it will be adjusted before it is returned.
     *
     * @param x The value to check
     * @return The valid value
     */
    private float getValidY(float y) {
        float min = getMinY();
        if (y < min) return min;
        float max = getMaxY();
        if (y > max) return max;
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
        if (x > 0) x /= width;
        if (y > 0) y /= height;
        y = 1 - y;
        return new PointF(x, y);
    }

    /**
     * Checks if the provided x and y values are valid. This will make sure that the circle is
     * always drawn inside this view's bounds. The PatchView patch is updated and then the values
     * are converted to internal representation before notifying the OnPosChangedListener.
     * 
     * @param x The horizontal circle center position
     * @param y The vertical circle center position
     */
    private void moveCircle(float x, float y) {
        PointF pos = new PointF(getValidX(x), getValidY(y));
        mPatch.setSelectedPos(pos);
        notifyOnPosChanged(pxToPd(pos));
        invalidate();
    }

    /* (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
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

    /* (non-Javadoc)
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
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