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

package net.simno.dmach.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.simno.dmach.DMachApp;
import net.simno.dmach.R;
import net.simno.dmach.model.Channel;

import javax.inject.Inject;

public final class PanView extends PdView {

    public interface OnPanChangedListener {
        void onPanChanged(Channel channel, float pan);
    }

    private static final String LEFT = "L";
    private static final String RIGHT = "R";

    @Inject Typeface typeface;
    private OnPanChangedListener listener;
    private Channel channel;
    private Paint shapePaint;
    private Paint textPaint;
    private float shapeStrokeWidth;
    private float textSize;
    private float pan;
    private float rectHeight;
    private float offset;
    private float center;
    private float centerLeft;
    private float centerRight;
    private float originX;
    private float originYL;
    private float originYR;
    private final Rect bounds = new Rect();

    public PanView(Context context) {
        super(context);
        init(context);
    }

    public PanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        DMachApp.get(context).getComponent().inject(this);

        textSize = getResources().getDimension(R.dimen.text_size_channel);
        rectHeight = getResources().getDimension(R.dimen.rect_height);
        shapeStrokeWidth = getResources().getDimension(R.dimen.shape_stroke_width);
        offset = (shapeStrokeWidth / 2f) + (rectHeight / 2f);

        shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shapePaint.setStrokeWidth(shapeStrokeWidth);
        shapePaint.setStyle(Paint.Style.STROKE);
        shapePaint.setColor(getResources().getColor(R.color.gamboge));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getResources().getColor(R.color.dune));
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(typeface);
    }

    @Override
    protected float getMinX() {
        return shapeStrokeWidth / 2f;
    }

    @Override
    protected float getMinY() {
        return getMinX() + offset;
    }

    @Override
    protected float getMaxX() {
        return getWidth() - getMinX();
    }

    @Override
    protected float getMaxY() {
        return getHeight() + getMinX() - offset;
    }

    private void notifyOnPanChanged() {
        if (listener != null && channel != null) {
            if (pan == center){
                listener.onPanChanged(channel, .5f);
            } else {
                listener.onPanChanged(channel, yToPd(pan));
            }
        }
    }

    public void setOnPanChangedListener(OnPanChangedListener listener) {
        this.listener = listener;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        updatePan();
    }

    private void updatePan() {
        if (channel == null) {
            return;
        }
        float newPan = channel.getPan();

        center = getHeight() / 2f;
        centerLeft = center + (offset / 2f);
        centerRight = center - (offset / 2f);

        textPaint.getTextBounds(RIGHT, 0, RIGHT.length(), bounds);
        originX = (getWidth() / 2f) - bounds.centerX();
        originYL = getMaxY() + (textSize * 0.25f);
        originYR = getMinY() + (textSize * 0.25f);

        if (newPan == 0.5f) {
            pan = center;
        } else {
            pan = pdToY(newPan);
        }

        invalidate();
    }

    private void makeCenterPanStick() {
        if (pan > centerRight && pan < centerLeft) {
            pan = center;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(RIGHT, originX, originYR, textPaint);
        canvas.drawText(LEFT, originX, originYL, textPaint);

        float panOffset = pan - offset;
        canvas.drawRect(getMinX(), panOffset, getMaxX(), panOffset + rectHeight, shapePaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                pan = getValidY(event.getY());
                makeCenterPanStick();
                notifyOnPanChanged();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updatePan();
        }
    }
}
