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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 * 
 */

package net.simno.android.dmach;

import net.simno.android.dmach.model.Patch;
import net.simno.android.dmach.model.PointF;
import net.simno.android.dmach.view.PatchView;
import net.simno.android.dmach.view.PatchView.PatchViewListener;
import net.simno.android.dmach.R;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class PatchFragment extends Fragment {
	public interface PatchFragmentListener {
		public void onSettingIndexChanged(int index);
        public void onSettingPosChanged(PointF pos);
	}
	
	PatchFragmentListener mListener;
	private static final String TAG_PATCH = "patch";
	private Patch initPatch;
	private PatchView patchView;
	
	public PatchFragment() {
		super();
	}
	
	public static PatchFragment newInstance(Patch patch) {
		PatchFragment pf = new PatchFragment();
		Bundle args = new Bundle();
		args.putParcelable(TAG_PATCH, patch);
		pf.setArguments(args);
		return pf;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (PatchFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ "must implement PatchFragmentListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (null == savedInstanceState) {
			savedInstanceState = getArguments();
		}
		if (null != savedInstanceState) {
			initPatch = savedInstanceState.getParcelable(TAG_PATCH);
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patch, container, false);
        patchView = (PatchView) view.findViewById(R.id.patchView);
        patchView.setPatchViewListener(new PatchViewListener() {
        	@Override
			public void onPosChanged(PointF pos) {
       			mListener.onSettingPosChanged(pos);
			}
        });
        patchView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight,
					int oldBottom) {
				if (null != initPatch) {
					patchView.setPatch(initPatch);	
				}
			}
        });
        RadioGroup group = (RadioGroup) view.findViewById(R.id.patches);
        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int curIndex = group.indexOfChild(group.findViewById(checkedId));
				mListener.onSettingIndexChanged(curIndex);
				patchView.setSelectedSettingIndex(curIndex);
			}
		});
        int current = initPatch.getSelectedSettingIndex();
        for (int i = 0; i < initPatch.getCount(); ++i) {
        	RadioButton rb = new RadioButton(getActivity());
        	group.addView(rb, i);
        	rb.setText("" + (i + 1));
        	rb.setChecked(i == current ? true : false);
        }
        return view;
    }
}