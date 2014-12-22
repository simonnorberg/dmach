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

package net.simno.dmach.view;

import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import net.simno.dmach.R;

public class PanViewTest extends AndroidTestCase {

    private PanView mPanView;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.fragment_channel, null, false);

        mPanView = (PanView) root.findViewById(R.id.pan_view);
        mPanView.measure(1920, 1080);
        mPanView.layout(0, 0, 1920, 1080);
    }

    public void testExists() throws Exception {
        assertNotNull(mPanView);
    }

    public void testPosition() throws Exception {
        assertEquals(0, mPanView.getTop());
        assertEquals(0, mPanView.getLeft());
    }

    public void testValidXY() throws Exception {
        assertEquals(mPanView.getMinY(), mPanView.getValidY(0));
        assertEquals(mPanView.getMaxY(), mPanView.getValidY(1080));
        assertEquals(mPanView.getMinX(), mPanView.getValidX(0));
        assertEquals(mPanView.getMaxX(), mPanView.getValidX(1920));
    }

    public void testPan() throws Exception {
        mPanView.setPan(.5f);
        assertEquals(.5f, mPanView.getPdPan());
    }

    public void testOnTouch() throws Exception {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        int metaState = 0;

        mPanView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 0f, 0f, metaState));
        assertEquals(1f, mPanView.getPdPan());

        mPanView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 0f, 1080f, metaState));
        assertEquals(0f, mPanView.getPdPan());

        mPanView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 1920f, 0f, metaState));
        assertEquals(1f, mPanView.getPdPan());

        mPanView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 1920f, 1080f, metaState));
        assertEquals(0f, mPanView.getPdPan());
    }
}
