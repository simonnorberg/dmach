package net.simno.android.dmach.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public final class ProgressBarView extends View {

	private Rect bounds = new Rect();
	private Paint paint;
	
	public ProgressBarView(Context context) {
		super(context);
		init();
	}

	public ProgressBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public ProgressBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		paint = new Paint(Color.parseColor("#E9950A"));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.getClipBounds(bounds);
		canvas.drawLine(30f, 0, 30f, bounds.height(), paint);
	}
}