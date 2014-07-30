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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.simno.dmach.model.Channel;
import net.simno.dmach.view.PanView;
import net.simno.dmach.view.PanView.OnPanChangedListener;
import net.simno.dmach.view.SettingView;
import net.simno.dmach.view.SettingView.OnSettingChangedListener;

import org.puredata.core.PdBase;

public class ChannelFragment extends Fragment
        implements OnClickListener, OnPanChangedListener, OnSettingChangedListener {

    private static final String TAG_CHANNEL = "net.simno.dmach.TAG_CHANNEL";

    private Channel mChannel;
    private PanView mPanView;
    private SettingView mSettingView;

    public ChannelFragment() {
        super();
    }

    public static ChannelFragment newInstance(Channel channel) {
        ChannelFragment pf = new ChannelFragment();
        Bundle args = new Bundle();
        args.putParcelable(TAG_CHANNEL, channel);
        pf.setArguments(args);
        return pf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            savedInstanceState = getArguments();
        }
        if (savedInstanceState != null) {
            mChannel = savedInstanceState.getParcelable(TAG_CHANNEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        mPanView = (PanView) view.findViewById(R.id.pan_view);
        mSettingView = (SettingView) view.findViewById(R.id.setting_view);

        if (mChannel != null) {
            mPanView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mPanView.setPan(mChannel.getPan());
                }
            });
            mPanView.setOnPanChangedListener(this);

            mSettingView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mSettingView.setSetting(mChannel.getSetting());
                }
            });
            mSettingView.setOnSettingChangedListener(this);

            LinearLayout settings = (LinearLayout) view.findViewById(R.id.setting_container);
            int selected = mChannel.getSelection();
            int count = mChannel.getCount();
            for (int i = 0; i < settings.getChildCount(); ++i) {
                ImageButton ib = (ImageButton) settings.getChildAt(i);
                ib.setOnClickListener(this);
                if (i < count) {
                    ib.setSelected(i == selected);
                } else {
                    ib.setEnabled(false);
                }
            }
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        ImageButton button = (ImageButton) view;
        LinearLayout layout = (LinearLayout) button.getParent();
        int index = layout.indexOfChild(button);
        if (index != mChannel.getSelection()) {
            ImageButton oldButton = (ImageButton) layout.getChildAt(mChannel.getSelection());
            if (oldButton != null) {
                oldButton.setSelected(false);
            }
            mChannel.selectSetting(index);
            button.setSelected(true);
            mSettingView.setSetting(mChannel.getSetting());
        }
    }

    @Override
    public void onPanChanged(float pan) {
        String name = mChannel.getName();
        PdBase.sendFloat(name + "p", pan);
        mChannel.setPan(pan);
    }

    @Override
    public void onSettingChanged(float x, float y) {
        String name = mChannel.getName();
        PdBase.sendList(name, new Object[]{mChannel.getSetting().hIndex, x});
        PdBase.sendList(name, new Object[]{mChannel.getSetting().vIndex, y});
        mChannel.getSetting().setXY(x, y);
    }
}
