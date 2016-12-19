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
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.simno.dmach.R;

import java.util.ArrayList;
import java.util.Iterator;

import static android.support.v4.content.ContextCompat.getColor;
import static net.simno.dmach.ui.activity.DMachActivity.CHANNELS;
import static net.simno.dmach.ui.activity.DMachActivity.GROUPS;
import static net.simno.dmach.ui.activity.DMachActivity.MASKS;
import static net.simno.dmach.ui.activity.DMachActivity.STEPS;

public final class SequencerView extends View {

    public interface OnStepChangedListener {
        void onStepChanged(int group, int step, int mask, int index);
    }

    private static final int CHANNELS_PER_GROUP = CHANNELS / GROUPS;

    private final ArrayList<Step> sequence = new ArrayList<>();
    private OnStepChangedListener listener;
    private Paint uncheckedLight;
    private Paint uncheckedDark;
    private Paint checked;
    private int background;
    private boolean isChecked;
    private int width;
    private int height;
    private float margin;
    private float stepWidth;
    private float stepHeight;
    private float stepWidthMargin;
    private float stepHeightMargin;

    private static class Step {
        private RectF rect;
        private boolean checked;

        void toggle() {
            checked = !checked;
        }
    }

    public SequencerView(Context context) {
        super(context);
        init(context);
    }

    public SequencerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SequencerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        background = getColor(context, R.color.colonial);

        uncheckedLight = new Paint();
        uncheckedLight.setColor(getColor(context, R.color.khaki));
        uncheckedLight.setStyle(Paint.Style.FILL);

        uncheckedDark = new Paint();
        uncheckedDark.setColor(getColor(context, R.color.gurkha));
        uncheckedDark.setStyle(Paint.Style.FILL);

        checked = new Paint();
        checked.setColor(getColor(context, R.color.poppy));
        checked.setStyle(Paint.Style.FILL);

        margin = getResources().getDimension(R.dimen.margin_small);

        for (int channel = 0; channel < CHANNELS; ++channel) {
            for (int step = 0; step < STEPS; ++step) {
                sequence.add(new Step());
            }
        }
    }

    private void initSteps() {
        for (int channel = 0; channel < CHANNELS; ++channel) {
            for (int step = 0; step < STEPS; ++step) {
                float left = step * stepWidthMargin;
                float right = left + stepWidth;
                float top = channel * stepHeightMargin;
                float bottom = top + stepHeight;
                int index = (channel * STEPS) + step;
                sequence.get(index).rect = new RectF(left, top, right, bottom);
            }
        }
    }

    public void setOnStepChangedListener(OnStepChangedListener listener) {
        this.listener = listener;
    }

    private void notifyOnStepChanged(int channel, int step) {
        if (listener != null) {
            listener.onStepChanged(getGroup(channel), step, getMask(channel),
                    getIndex(channel, step));
        }
    }

    public void setChecked(int[] seq) {
        Iterator<Step> it = sequence.iterator();
        for (int channel = 0; channel < (CHANNELS); ++channel) {
            for (int step = 0; step < STEPS; ++step) {
                if (it.hasNext()) {
                    int mask = getMask(channel);
                    int index = getIndex(channel, step);
                    int value = seq[index] & mask;
                    it.next().checked = value != 0;
                }
            }
        }
        invalidate();
    }

    private static int getMask(int channel) {
        return MASKS[channel % CHANNELS_PER_GROUP];
    }

    private static int getGroup(int channel) {
        return channel / CHANNELS_PER_GROUP;
    }

    private static int getIndex(int channel, int step) {
        int offset = getGroup(channel) * STEPS;
        return step + offset;
    }

    private static int getListIndex(int channel, int step) {
        return channel * STEPS + step;
    }

    private int pxToChannel(float px) {
        return (int) (px / stepHeightMargin);
    }

    private int pxToStep(float px) {
        return (int) (px / stepWidthMargin);
    }

    private boolean isOutsideView(float x, float y) {
        return x < 0 || y < 0 || x > width || y > height;
    }

    private void onActionDown(float x, float y) {
        if (isOutsideView(x, y)) {
            return;
        }

        int channel = pxToChannel(y);
        int step = pxToStep(x);
        int index = getListIndex(channel, step);

        isChecked = sequence.get(index).checked;
        sequence.get(index).toggle();
        notifyOnStepChanged(channel, step);
        invalidate();
    }

    private void onActionMove(float x, float y) {
        if (isOutsideView(x, y)) {
            return;
        }

        int channel = pxToChannel(y);
        int step = pxToStep(x);
        int index = getListIndex(channel, step);

        if (sequence.get(index).checked == isChecked) {
            sequence.get(index).toggle();
            notifyOnStepChanged(channel, step);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                onActionMove(event.getX(), event.getY());
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(background);
        for (int i = 0; i < sequence.size(); ++i) {
            Step step = sequence.get(i);
            if (step.checked) {
                canvas.drawRect(step.rect, checked);
            } else {
                if ((i % 8) < 4) {
                    canvas.drawRect(step.rect, uncheckedLight);
                } else {
                    canvas.drawRect(step.rect, uncheckedDark);
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != 0 && h != 0) {
            width = w;
            height = h;
            stepWidth = (w - ((STEPS - 1f) * margin)) / STEPS;
            stepHeight = (h - ((CHANNELS - 1f) * margin)) / CHANNELS;
            stepWidthMargin = stepWidth + margin;
            stepHeightMargin = stepHeight + margin;
            initSteps();
            invalidate();
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }
}
