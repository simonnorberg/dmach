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

	private static final int BAR_WIDTH = 10;
//	private static final int PROGRESS_SIZE = 15;
	private static final int BAR_COLOR = Color.YELLOW;
	private static final int BAR_TRANSPARENCY = 200;
	
//	private int width;
	private int height;
	private int barPos;
	private int stepLength;
	private int stepMargin;
	private int barWidth;
//	private long updateDelay;
//	private long beatTime;
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
        
//        this.beatTime = 60000 / tempo;
        
        stepMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        
        
        stepLength = ((width - ((steps - 1) * stepMargin)) / steps) + stepMargin;
        barWidth = (width - ((steps - 1) * stepMargin)) / steps;
//        stepLength = width / steps;
        
        
        
//        updateDelay = beatTime / (stepLength / PROGRESS_SIZE);
        progressHandler = new Handler();
        progressBar = new ShapeDrawable(new RectShape());
        progressBar.getPaint().setColor(BAR_COLOR);
        progressBar.setAlpha(BAR_TRANSPARENCY);
        
        
        System.out.println("stepMargin: " + stepMargin);
        System.out.println("stepLength: " + stepLength);
        System.out.println("width: " + width);
        
//        int margin = (steps - 1) * stepMargin;
//        System.out.println("margin: " + margin);
//        int stepNoM = (width - margin) / steps;
//        System.out.println("stepNoM: " + stepNoM);
//        stepLength = stepNoM + stepMargin;
//        System.out.println("stepLength: " + stepLength);
        
        
//        moveBar();
    }

    private void moveBar() {
        synchronized (progressBar) {
//            barPos = (barPos + PROGRESS_SIZE) % width;
            progressBar.setBounds(barPos, 0, barPos + stepMargin, height);
        }
    }
		
	@Override
	protected void onDraw(Canvas canvas) {
        synchronized (progressBar) {
            progressBar.draw(canvas);
        }
	}

	@Override
	public void onTempoChange(int tempo) {
		
	}

	@Override
	public void onBeat(int beat) {
        barPos = beat * (stepLength);// + stepMargin);

        progressHandler.removeCallbacks(progressRunnable);
//        for (int i = 0; i < beatTime; i += updateDelay) {
//            progressHandler.postDelayed(progressRunnable, i);
//        }
        progressHandler.post(progressRunnable);
    }
}