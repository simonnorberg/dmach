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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.simno.dmach.model.Channel;
import net.simno.dmach.view.CustomFontButton;
import net.simno.dmach.view.PanView;
import net.simno.dmach.view.PanView.OnPanChangedListener;
import net.simno.dmach.view.SettingView;
import net.simno.dmach.view.SettingView.OnSettingChangedListener;

import org.puredata.core.PdBase;

public class ChannelFragment extends Fragment
        implements OnClickListener, OnPanChangedListener, OnSettingChangedListener {

    private static final String PARAM_CHANNEL = "channel";

    private Channel mChannel;
    private PanView mPanView;
    private SettingView mSettingView;

    public ChannelFragment() {
        super();
    }

    public static ChannelFragment newInstance(Channel channel) {
        ChannelFragment pf = new ChannelFragment();
        Bundle args = new Bundle();
        args.putParcelable(PARAM_CHANNEL, channel);
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
            mChannel = savedInstanceState.getParcelable(PARAM_CHANNEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        mPanView = (PanView) view.findViewById(R.id.pan_view);
        mSettingView = (SettingView) view.findViewById(R.id.setting_view);

        if (mChannel != null) {
            mPanView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
                    -> mPanView.setPan(mChannel.getPan()));
            mPanView.setOnPanChangedListener(this);

            mSettingView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
                    -> mSettingView.setSetting(mChannel.getSetting()));
            mSettingView.setOnSettingChangedListener(this);

            LinearLayout settings = (LinearLayout) view.findViewById(R.id.setting_container);
            int selected = mChannel.getSelection();
            int count = mChannel.getCount();
            for (int i = 0; i < settings.getChildCount(); ++i) {
                CustomFontButton ib = (CustomFontButton) settings.getChildAt(i);
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
        CustomFontButton button = (CustomFontButton) view;
        LinearLayout layout = (LinearLayout) button.getParent();
        int index = layout.indexOfChild(button);
        if (index != mChannel.getSelection()) {
            CustomFontButton oldButton = (CustomFontButton) layout.getChildAt(mChannel.getSelection());
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
        PdBase.sendList(name, mChannel.getSetting().getHIndex(), x);
        PdBase.sendList(name, mChannel.getSetting().getVIndex(), y);
        mChannel.getSetting().setX(x);
        mChannel.getSetting().setY(y);
    }
}
