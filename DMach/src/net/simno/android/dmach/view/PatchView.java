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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 * 
 */

package net.simno.android.dmach.view;

import net.simno.android.dmach.model.Patch;
import net.simno.android.dmach.model.PointF;
import net.simno.android.dmach.model.Setting;
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

public final class PatchView extends View {

	public interface PatchViewListener {
        public void onPosChanged(PointF pos);
    }

	private PatchViewListener mListener;
	private static final int backgroundColor = Color.parseColor("#E9950A");
	private static final int circleColor = Color.parseColor("#EBEBAF");
	private static final int textColor = Color.parseColor("#B57400");
	private static final float radius = 30f;
	private static final float strokeWidth = 6f;
	private static final float textSize = 40f;
	private Rect bounds = new Rect();
	private Rect vBounds = new Rect();
	private Rect hBounds = new Rect();
	private Path vPath = new Path();
	private Patch patch;
	private Paint circlePaint;
	private Paint textPaint;
	
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
		setMinimumWidth(100);
		setMinimumHeight(100);
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setStrokeWidth(strokeWidth);
		circlePaint.setColor(circleColor);
		circlePaint.setStyle(Paint.Style.STROKE);
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(textColor);
		textPaint.setTextSize(textSize);
		textPaint.setTypeface(Typeface.SANS_SERIF);
		textPaint.setStyle(Paint.Style.FILL);
		patch = new Patch();
		patch.addSetting(new Setting());
		patch.addSetting(new Setting());
		patch.addSetting(new Setting());
		patch.addSetting(new Setting());
		patch.addSetting(new Setting());
		patch.addSetting(new Setting());
	}

	public void setPatchViewListener(PatchViewListener listener) {
		mListener = listener;
	}
	
	private void notifyOnPosChanged(PointF pos) {
		if (mListener != null) {
			mListener.onPosChanged(pxToPd(pos));
		}
	}

	public void setPatch(Patch p) {
		patch = new Patch(p);
		for (int i = 0; i < patch.getCount(); ++i) {
			patch.setPos(i, pdToPx(p.getPos(i)));
		}
	}

	public void setSelectedSettingIndex(int index) {
		patch.setSelectedSettingIndex(index);
		invalidate();
	}
	
	private float getMinX() {
		return radius + (strokeWidth / 2);
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

	private PointF pdToPx(PointF pos) {
		float width = getMaxX() - getMinX();
		float height = getMaxY() - getMinY();
		float x = (pos.getX() * width) + getMinX();
		float y = ((1 - pos.getY()) * height) + getMinY();
		return new PointF(x, y);
	}

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

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(backgroundColor);
		canvas.getClipBounds(bounds);
		
		String hText = patch.getSelectedHText();
		String vText = patch.getSelectedVText();
		PointF pos = patch.getSelectedPos();
		
		textPaint.getTextBounds(hText, 0, hText.length(), hBounds);
		canvas.drawText(hText, bounds.centerX() - hBounds.centerX(),
				bounds.height(), textPaint);
		
		textPaint.getTextBounds(vText, 0, vText.length(), vBounds);
		vPath.reset();
		vPath.moveTo(0, bounds.centerY() + vBounds.centerX());
		vPath.lineTo(0, 0);
		canvas.drawTextOnPath(vText, vPath, 0, vBounds.height(), textPaint);
		
		canvas.drawCircle(pos.getX(), pos.getY(), radius, circlePaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			PointF pos = new PointF(getValidX(event.getX()), getValidY(event.getY()));
			patch.setSelectedPos(pos);
			notifyOnPosChanged(pos);
			invalidate();
			break;
		}
		return true;
	}
}