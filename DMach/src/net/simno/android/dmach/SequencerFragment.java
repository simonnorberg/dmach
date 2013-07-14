/**
 * Copyright (C) 2013 Simon Norberg
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

package net.simno.android.dmach;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import net.simno.android.dmach.R;
import net.simno.android.dmach.model.Channel;

import java.util.ArrayList;

/**
 * SequencerFragment holds all step buttons in the step sequencer.
 */
public class SequencerFragment extends Fragment {

    public SequencerFragment() {
        super();
    }

    private static final String TAG_SEQUENCER = "sequencer";
    private ArrayList<Channel> mChannels;

    /**
     * Creates a new SequencerFragment instance
     *
     * @param channels The channels list to send to the new instance
     * @return The new SequencerFramgment instance
     */
    public static SequencerFragment newInstance(ArrayList<Channel> channels) {
        SequencerFragment sf = new SequencerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(TAG_SEQUENCER, channels);
        sf.setArguments(args);
        return sf;
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            savedInstanceState = getArguments();
        }
        if (null != savedInstanceState) {
            mChannels = savedInstanceState.getParcelableArrayList(TAG_SEQUENCER);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
     * android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sequencer, container, false);

        // Initialize step buttons
        if (null != mChannels) {
            ViewGroup bdSteps = (ViewGroup) view.findViewById(R.id.bdSteps);
            ViewGroup sdSteps = (ViewGroup) view.findViewById(R.id.sdSteps);
            ViewGroup ttSteps = (ViewGroup) view.findViewById(R.id.ttSteps);
            ViewGroup hhSteps = (ViewGroup) view.findViewById(R.id.hhSteps);
            boolean[] bdSequence = mChannels.get(0).getSequence();
            boolean[] sdSequence = mChannels.get(1).getSequence();
            boolean[] ttSequence = mChannels.get(2).getSequence();
            boolean[] hhSequence = mChannels.get(3).getSequence();
            for (int i = 0; i < DMachActivity.STEP_COUNT; ++i) {
                ToggleButton bd = (ToggleButton) bdSteps.getChildAt(i);
                ToggleButton sd = (ToggleButton) sdSteps.getChildAt(i);
                ToggleButton tt = (ToggleButton) ttSteps.getChildAt(i);
                ToggleButton hh = (ToggleButton) hhSteps.getChildAt(i);
                bd.setChecked(bdSequence[i]);
                sd.setChecked(sdSequence[i]);
                tt.setChecked(ttSequence[i]);
                hh.setChecked(hhSequence[i]);
            }
        }
        return view;
    }
}