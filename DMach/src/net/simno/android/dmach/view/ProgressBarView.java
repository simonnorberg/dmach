/**
 * 
 * Copyright: 2011 Android Aalto Community
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

import net.simno.android.dmach.DMachActivity.OnBeatListener;
import net.simno.android.dmach.DMachActivity.OnTempoChangedListener;
import net.simno.android.dmach.DMachActivity.OnVisibilityListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public final class ProgressBarView extends View
implements OnBeatListener, OnTempoChangedListener, OnVisibilityListener {

	private static final int BAR_COLOR = Color.YELLOW;
	private static final int BAR_TRANSPARENCY = 127;
	private static final int MARGIN_DP = 8;
	
	private boolean isVisible;
	private int height;
	private int barPos;
	private int stepLength;
	private int stepMargin;
	private int stepWidth;
	private long updateDelay;
	private ShapeDrawable progressBar;
	private ProgressBarView thisView = this;
	private Handler progressHandler;
	private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
        	moveBar();
            thisView.invalidate();
        }
    };
	
	public ProgressBarView(Context context) {
		super(context);
	}

	public ProgressBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ProgressBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
    
    public ProgressBarView(Context context, int width, int height, int steps, int tempo) {
        super(context);
        this.height = height;
        stepMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_DP,
        		getResources().getDisplayMetrics());
        stepWidth = (width - ((steps - 1) * stepMargin)) / steps;
        stepLength = stepWidth + stepMargin;
        setUpdateDelay(tempo);
        progressHandler = new Handler();
        progressBar = new ShapeDrawable(new RectShape());
        progressBar.getPaint().setColor(BAR_COLOR);
        progressBar.setAlpha(BAR_TRANSPARENCY);
     }

    private void setUpdateDelay(int tempo) {
        updateDelay = Math.max(Math.round((30000.0 / tempo) / stepLength), 1);
    }
    
    private void moveBar() {
        synchronized (progressBar) {
            progressBar.setBounds(barPos, 0, barPos + stepWidth, height);
        }
        ++barPos;
    }
		
	@Override
	protected void onDraw(Canvas canvas) {
		if (true == isVisible) {
			synchronized (progressBar) {
	            progressBar.draw(canvas);
	        }	
		}
	}

	@Override
	public void onTempoChanged(int tempo) {
		setUpdateDelay(tempo);
	}

	@Override
	public void onBeat(int beat) {
        barPos = beat * stepLength;
        progressHandler.removeCallbacks(progressRunnable);
        for (int i = 0; i < stepLength * updateDelay; i += updateDelay) {
            progressHandler.postDelayed(progressRunnable, i);
        }
    }

	@Override
	public void onShow() {
		isVisible = true;
	}

	@Override
	public void onHide() {
		isVisible = false;
		thisView.invalidate();
	}
}