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
import net.simno.dmach.model.Setting;

public class SettingViewTest extends AndroidTestCase {

    private SettingView mSettingView;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.fragment_channel, null, false);

        mSettingView = (SettingView) root.findViewById(R.id.setting_view);
        mSettingView.measure(1920, 1080);
        mSettingView.layout(0, 0, 1920, 1080);
    }

    public void testExists() throws Exception {
        assertNotNull(mSettingView);
    }

    public void testPosition() throws Exception {
        assertEquals(0, mSettingView.getTop());
        assertEquals(0, mSettingView.getLeft());
    }

    public void testValidXY() throws Exception {
        assertEquals(mSettingView.getMinY(), mSettingView.getValidY(0));
        assertEquals(mSettingView.getMaxY(), mSettingView.getValidY(1080));
        assertEquals(mSettingView.getMinX(), mSettingView.getValidX(0));
        assertEquals(mSettingView.getMaxX(), mSettingView.getValidX(1920));
    }

    public void testConversion() throws Exception {
        assertEquals(0f, mSettingView.xToPd(mSettingView.pdToX(0f)));
        assertEquals(0f, mSettingView.yToPd(mSettingView.pdToY(0f)));

        assertEquals(1f, mSettingView.xToPd(mSettingView.pdToX(1f)));
        assertEquals(1f, mSettingView.yToPd(mSettingView.pdToY(1f)));

        assertEquals(0.5f, mSettingView.xToPd(mSettingView.pdToX(0.5f)));
        assertEquals(0.5f, mSettingView.yToPd(mSettingView.pdToY(0.5f)));
    }

    public void testSettings() throws Exception {
        mSettingView.setSetting(new Setting("Pitch A", "Gain", .4f, .6f, 0, 7));
        assertEquals(.4f, mSettingView.getPdX());
        assertEquals(.6f, mSettingView.getPdY());
    }

    public void testOnTouch() throws Exception {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        int metaState = 0;

        mSettingView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 0f, 0f, metaState));
        assertEquals(0f, mSettingView.getPdX());
        assertEquals(1f, mSettingView.getPdY());

        mSettingView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 0f, 1080f, metaState));
        assertEquals(0f, mSettingView.getPdX());
        assertEquals(0f, mSettingView.getPdY());

        mSettingView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 1920f, 0f, metaState));
        assertEquals(1f, mSettingView.getPdX());
        assertEquals(1f, mSettingView.getPdY());

        mSettingView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 1920f, 1080f, metaState));
        assertEquals(1f, mSettingView.getPdX());
        assertEquals(0f, mSettingView.getPdY());
    }
}
