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

package net.simno.dmach;

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

import net.simno.dmach.R;
import net.simno.dmach.model.Patch;
import net.simno.dmach.model.PointF;
import net.simno.dmach.view.PatchView;
import net.simno.dmach.view.PatchView.OnPosChangedListener;

public class PatchFragment extends Fragment {

    public interface OnPatchChangedListener {
        public void onSettingIndexChanged(int index);
        public void onSettingPosChanged(PointF pos);
    }

    private static final String TAG_PATCH = "patch";

    private OnPatchChangedListener mListener;
    private Patch mPatch;
    private PatchView mPatchView;

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
            mListener = (OnPatchChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement OnPatchChangedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            savedInstanceState = getArguments();
        }
        if (null != savedInstanceState) {
            mPatch = savedInstanceState.getParcelable(TAG_PATCH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patch, container, false);
//        initPatchView((PatchView) view.findViewById(R.id.patchView));
//        if (mPatch != null) {
//            initSettings((RadioGroup) view.findViewById(R.id.settings));
//        }
        return view;
    }

    private void initPatchView(PatchView patchView) {
        mPatchView = patchView;
        mPatchView.setOnPosChangedListener(new OnPosChangedListener() {
            @Override
            public void onPosChanged(PointF pos) {
                mListener.onSettingPosChanged(pos);
            }
        });
        mPatchView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                    int bottom, int oldLeft, int oldTop, int oldRight,
                    int oldBottom) {
                if (null != mPatch) {
                    mPatchView.setPatch(mPatch);
                }
            }
        });
    }

    private void initSettings(RadioGroup group) {
        int current = mPatch.getSelectedSettingIndex();
        int enabledSettingsCount = mPatch.getCount();
        for (int i = 0; i < group.getChildCount(); ++i) {
            RadioButton rb = (RadioButton) group.getChildAt(i);
            if (i < enabledSettingsCount) {
                rb.setChecked(i == current ? true : false);
            } else {
                rb.setEnabled(false);
            }
        }
        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int curIndex = group.indexOfChild(group.findViewById(checkedId));
                mListener.onSettingIndexChanged(curIndex);
                mPatchView.setSelectedSettingIndex(curIndex);
            }
        });
    }
}