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

import net.simno.dmach.DMachActivity;
import net.simno.dmach.R;

public class SequencerViewTest extends AndroidTestCase {

    private SequencerView mSequencerView;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.fragment_sequencer, null);

        mSequencerView = (SequencerView) root.findViewById(R.id.sequencer_view);
        mSequencerView.measure(1920, 1080);
        mSequencerView.layout(0, 0, 1920, 1080);
    }

    public void testSequence() throws Exception {
        int[] expected = new int[DMachActivity.GROUPS * DMachActivity.STEPS];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = i % 8;
        }
        mSequencerView.setChecked(expected);
        int[] actual = mSequencerView.getPdSequence();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], actual[i]);
        }
    }

    public void testGetMask() throws Exception {
        assertEquals(1, SequencerView.getMask(0));
        assertEquals(2, SequencerView.getMask(1));
        assertEquals(4, SequencerView.getMask(2));
        assertEquals(1, SequencerView.getMask(3));
        assertEquals(2, SequencerView.getMask(4));
        assertEquals(4, SequencerView.getMask(5));
    }

    public void testGetGroup() throws Exception {
        assertEquals(0, SequencerView.getGroup(0));
        assertEquals(0, SequencerView.getGroup(1));
        assertEquals(0, SequencerView.getGroup(2));
        assertEquals(1, SequencerView.getGroup(3));
        assertEquals(1, SequencerView.getGroup(4));
        assertEquals(1, SequencerView.getGroup(5));
    }

    public void testGetIndex() throws Exception {
        assertEquals(5, SequencerView.getIndex(0, 5));
        assertEquals(22, SequencerView.getIndex(4, 6));
    }

    public void testGetListIndex() throws Exception {
        assertEquals(70, SequencerView.getListIndex(4, 6));
    }

    public void testOnTouch() throws Exception {
        int[] expected = new int[DMachActivity.GROUPS * DMachActivity.STEPS];
        mSequencerView.setChecked(expected);
        expected[1] = 2;
        expected[18] = 4;
        expected[30] = 5;

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        int metaState = 0;

        mSequencerView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 200f, 200f, metaState));
        mSequencerView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 300f, 1000f, metaState));
        mSequencerView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 1700f, 600f, metaState));
        mSequencerView.dispatchTouchEvent(
                MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 1720f, 1000f, metaState));

        int[] actual = mSequencerView.getPdSequence();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], actual[i]);
        }
    }
}
