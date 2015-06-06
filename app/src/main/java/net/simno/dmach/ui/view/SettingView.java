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
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import net.simno.dmach.DMachApp;
import net.simno.dmach.R;
import net.simno.dmach.model.Channel;
import net.simno.dmach.model.Setting;

import javax.inject.Inject;

public final class SettingView extends PdView {

    public interface OnSettingChangedListener {
        void onSettingChanged(Channel channel, float x, float y);
    }

    @Inject Typeface typeface;
    private Channel channel;
    private Paint shapePaint;
    private Paint textPaint;
    private float shapeStrokeWidth;
    private float textSize;
    private int backgroundColor;
    private float circleRadius;
    private OnSettingChangedListener listener;
    private final Rect hBounds = new Rect();
    private final Rect vBounds = new Rect();
    private final Path path = new Path();
    private String hText;
    private String vText;
    private float x;
    private float y;
    private float originX;
    private float originY;
    private int hOffset;
    private int vOffset;

    public SettingView(Context context) {
        super(context);
        init(context);
    }

    public SettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SettingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        DMachApp.get(context).getComponent().inject(this);

        backgroundColor = getResources().getColor(R.color.gamboge);
        textSize = getResources().getDimension(R.dimen.text_size_setting);
        circleRadius = getResources().getDimension(R.dimen.circle_radius);
        shapeStrokeWidth = getResources().getDimension(R.dimen.shape_stroke_width);

        shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shapePaint.setStrokeWidth(shapeStrokeWidth);
        shapePaint.setStyle(Paint.Style.STROKE);
        shapePaint.setColor(getResources().getColor(R.color.colonial));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getResources().getColor(R.color.dune));
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTypeface(typeface);
    }

    @Override
    protected float getMinX() {
        return circleRadius + (shapeStrokeWidth / 2f);
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
        if (listener != null && channel != null) {
            listener.onSettingChanged(channel, xToPd(x), yToPd(y));
        }
    }

    public void setOnSettingChangedListener(OnSettingChangedListener listener) {
        this.listener = listener;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        updateSetting();
    }

    private void updateSetting() {
        if (channel == null) {
            return;
        }
        Setting setting = channel.getSetting();

        x = pdToX(setting.getX());
        y = pdToY(setting.getY());
        hText = setting.getHText();
        vText = setting.getVText();

        textPaint.getTextBounds(hText, 0, hText.length(), hBounds);
        originX = (getWidth() / 2f) - hBounds.centerX();
        originY = getHeight() - (textSize * 0.4f);

        textPaint.getTextBounds(vText, 0, vText.length(), vBounds);
        path.reset();
        path.moveTo(0, (getHeight() / 2f) + vBounds.centerX());
        path.lineTo(0, 0);
        hOffset = 0;
        vOffset = (int) textSize;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(backgroundColor);
        if (!TextUtils.isEmpty(hText)) {
            canvas.drawText(hText, originX, originY, textPaint);
        }
        if (!TextUtils.isEmpty(vText)) {
            canvas.drawTextOnPath(vText, path, hOffset, vOffset, textPaint);
        }
        canvas.drawCircle(x, y, circleRadius, shapePaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                x = getValidX(event.getX());
                y = getValidY(event.getY());
                notifyOnSettingChanged();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateSetting();
        }
    }
}
