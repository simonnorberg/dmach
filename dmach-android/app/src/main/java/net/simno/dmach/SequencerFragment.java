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

package net.simno.dmach;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLayoutChangeListener;

import net.simno.dmach.view.ProgressBarView;
import net.simno.dmach.view.SequencerView;
import net.simno.dmach.view.SequencerView.OnStepChangedListener;

import org.puredata.core.PdBase;

public class SequencerFragment extends Fragment
        implements OnStepChangedListener, OnLayoutChangeListener {

    public SequencerFragment() {
        super();
    }

    private static final String TAG_SEQUENCER = "net.simno.dmach.TAG_SEQUENCER";

    private int[] mSequence;
    private SequencerView mSequencerView;

    public static SequencerFragment newInstance(int[] sequence) {
        SequencerFragment sf = new SequencerFragment();
        Bundle args = new Bundle();
        args.putIntArray(TAG_SEQUENCER, sequence);
        sf.setArguments(args);
        return sf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            savedInstanceState = getArguments();
        }
        if (savedInstanceState != null) {
            mSequence = savedInstanceState.getIntArray(TAG_SEQUENCER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sequencer, container, false);
        mSequencerView = (SequencerView) view.findViewById(R.id.sequencer_view);
        mSequencerView.setOnStepChangedListener(this);
        mSequencerView.addOnLayoutChangeListener(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        ProgressBarView p = ((ProgressBarView) getActivity().findViewById(R.id.progress_bar_view));
        if (p != null) {
            p.cleanup();
        }
        super.onDestroyView();
    }

    @Override
    public void onStepChanged(int channel, int step) {
        int mask = channel % (DMachActivity.CHANNELS / DMachActivity.GROUPS);
        int group = channel / (DMachActivity.CHANNELS / DMachActivity.GROUPS);
        int index = (group * DMachActivity.STEPS) + step;
        mSequence[index] ^= DMachActivity.MASKS[mask];
        PdBase.sendList("step", new Object[]{group, step, mSequence[index]});
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                               int oldTop, int oldRight, int oldBottom) {
        if (mSequence != null) {
            mSequencerView.setChecked(mSequence);
        }
    }
}
