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

import org.puredata.core.PdBase;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.simno.dmach.R;
import net.simno.dmach.model.Channel;
import net.simno.dmach.view.SettingView;
import net.simno.dmach.view.SettingView.OnSettingChangedListener;

public class ChannelFragment extends Fragment
implements OnClickListener, OnSettingChangedListener, OnLayoutChangeListener {

    private static final String TAG_CHANNEL = "channel";

    private Channel mChannel;
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
        if (null == savedInstanceState) {
            savedInstanceState = getArguments();
        }
        if (null != savedInstanceState) {
            mChannel = savedInstanceState.getParcelable(TAG_CHANNEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        mSettingView = (SettingView) view.findViewById(R.id.settingView);
        if (mChannel != null) {
            mSettingView.setOnSettingChangedListener(this);
            mSettingView.addOnLayoutChangeListener(this);
            LinearLayout settings = (LinearLayout) view.findViewById(R.id.settings);
            int selected = mChannel.getSelection();
            int count = mChannel.getCount();
            for (int i = 0; i < settings.getChildCount(); ++i) {
                ImageButton ib = (ImageButton) settings.getChildAt(i);
                ib.setOnClickListener(this);
                if (i < count) {
                    ib.setSelected(i == selected ? true : false);
                } else {
                    ib.setEnabled(false);
                }
            }
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        ImageButton setting = (ImageButton) view;
        LinearLayout settings = (LinearLayout) setting.getParent();
        int index = settings.indexOfChild(setting);
        if (index != mChannel.getSelection()) {
            ImageButton oldSetting = (ImageButton) settings.getChildAt(mChannel.getSelection());
            if (oldSetting != null) {
                oldSetting.setSelected(false);
            }
            mChannel.selectSetting(index);
            setting.setSelected(true);
            mSettingView.setSetting(mChannel.getSelectedSetting());
        }
    }

    @Override
    public void onSettingChanged(float x, float y) {
        String name = mChannel.getName();
        int index = mChannel.getSelection();                                                                                                                                           
        PdBase.sendList(name, new Object[]{2 * index, x});
        PdBase.sendList(name, new Object[]{2 * (index + 1), y});
        mChannel.getSelectedSetting().setX(x);
        mChannel.getSelectedSetting().setY(y);
        System.out.println("x " + x + " y " + y);        
    }
    
    @Override
    public void onLayoutChange(View v, int left, int top, int right,
            int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        mSettingView.setSetting(mChannel.getSelectedSetting());
    }
}
