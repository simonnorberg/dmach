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
import net.simno.android.dmach.DMachActivity.OnTempoChangeListener;
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
implements OnBeatListener, OnTempoChangeListener {

	private static final int BAR_COLOR = Color.YELLOW;
	private static final int BAR_TRANSPARENCY = 127;
	
//	private int width;
	private int height;
	private int barPos;
	private int stepLength;
	private int stepMargin;
	private int barWidth;
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
//        this.width = width;

        stepMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        barWidth = (width - ((steps - 1) * stepMargin)) / steps;
        stepLength = barWidth + stepMargin;

        setUpdateDelay(tempo);
        
        progressHandler = new Handler();
        progressBar = new ShapeDrawable(new RectShape());
        progressBar.getPaint().setColor(BAR_COLOR);
        progressBar.setAlpha(BAR_TRANSPARENCY);
     }

    private void setUpdateDelay(int tempo) {
        long beatTime = 15000 / tempo;
        System.out.println("beatTime: " + beatTime);
        updateDelay = Math.max(Math.round((double) beatTime / stepLength), 1);
        System.out.println("updateDelay: " + updateDelay);
    }
    
    private void moveBar() {
        synchronized (progressBar) {
            progressBar.setBounds(barPos, 0, barPos + barWidth, height);
        }
        ++barPos;
    }
		
	@Override
	protected void onDraw(Canvas canvas) {
        synchronized (progressBar) {
            progressBar.draw(canvas);
        }
	}

	@Override
	public void onTempoChange(int tempo) {
		setUpdateDelay(tempo);
	}

	@Override
	public void onBeat(int beat) {
        barPos = beat * (stepLength);

        progressHandler.removeCallbacks(progressRunnable);
        for (int i = 0; i < stepLength * updateDelay; i += updateDelay) {
            progressHandler.postDelayed(progressRunnable, i);
        }
//        for (int i = 0; i < stepLength; ++i) {
//            progressHandler.postDelayed(progressRunnable, i * updateDelay);
//        }
    }
}