package net.simno.dmach.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

public class PanView extends PdView {

    public interface OnPanChangedListener {
        public void onPanChanged(float pan);
    }

    private static final String L = "L";
    private static final String R = "R";
    private static final int RECT_HEIGHT = 36;
    private static final int RECT_COLOR = Color.parseColor("#E9950A");

    private OnPanChangedListener mListener;
    private float mPan;
    private float mRectHeight;
    private float mOffset;
    private float mCenter;
    private float mCenterLeft;
    private float mCenterRight;
    private float mOriginX;
    private float mOriginYL;
    private float mOriginYR;
    private Rect mBounds = new Rect();

    public PanView(Context context) {
        super(context);
    }

    public PanView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        mShapePaint.setColor(RECT_COLOR);
        mRectHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, RECT_HEIGHT, mDm);
        mOffset = (mShapeStrokeWidth / 2f) + (mRectHeight / 2f);
    }

    @Override
    protected float getMinX() {
        return mShapeStrokeWidth / 2f;
    }

    @Override
    protected float getMinY() {
        return getMinX() + mOffset;
    }

    @Override
    protected float getMaxX() {
        return getWidth() - getMinX();
    }

    @Override
    protected float getMaxY() {
        return getHeight() + getMinX() - mOffset;
    }

    private void notifyOnPanChanged() {
        if (mListener != null) {
            if (mPan == mCenter){
                mListener.onPanChanged(0.5f);
            } else {
                mListener.onPanChanged(yToPd(mPan));
            }
        }
    }

    public void setOnPanChangedListener(OnPanChangedListener listener) {
        mListener = listener;
    }

    public void setPan(float pan) {
        mCenter = getWidth() / 2f;

        mCenter = getHeight() / 2f;
        mCenterLeft = mCenter + (mOffset / 2f);
        mCenterRight = mCenter - (mOffset / 2f);

        mTextPaint.getTextBounds(R, 0, R.length(), mBounds);
        mOriginX = (getWidth() / 2f) - mBounds.centerX();
        mOriginYL = getHeight() - (mTextSize * 0.5f);
        mOriginYR = getMinY() + (mTextSize * 0.25f);

        if (pan == 0.5f) {
            mPan = mCenter;
        } else {
            mPan = pdToY(pan);
        }

        invalidate();
    }

    private void makeCenterPanStick() {
        if (mPan > mCenterRight && mPan < mCenterLeft) {
            mPan = mCenter;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(R, mOriginX, mOriginYR, mTextPaint);
        canvas.drawText(L, mOriginX, mOriginYL, mTextPaint);

        float panOffset = mPan - mOffset;
        canvas.drawRect(getMinX(), panOffset, getMaxX(), panOffset + mRectHeight, mShapePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mPan = getValidY(event.getY());
                makeCenterPanStick();
                notifyOnPanChanged();
                invalidate();
                break;
        }
        return true;
    }
}
