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
import android.util.AttributeSet;
import android.view.View;

public abstract class PdView extends View {

    public PdView(Context context) {
        super(context);
    }

    public PdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PdView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected abstract float getMinX();

    protected abstract float getMinY();

    protected abstract float getMaxX();

    protected abstract float getMaxY();

    final float getValidX(float x) {
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

    final float getValidY(float y) {
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

    final float pdToX(float pdX) {
        float width = getMaxX() - getMinX();
        return (pdX * width) + getMinX();
    }

    final float pdToY(float pdY) {
        float height = getMaxY() - getMinY();
        return ((1 - pdY) * height) + getMinY();
    }

    final float xToPd(float x) {
        float width = getMaxX() - getMinX();
        float pdX = x - getMinX();
        if (pdX > 0) {
            pdX /= width;
        }
        return pdX;
    }

    final float yToPd(float y) {
        float height = getMaxY() - getMinY();
        float pdY = y - getMinY();
        if (pdY > 0) {
            pdY /= height;
        }
        return 1 - pdY;
    }
}
