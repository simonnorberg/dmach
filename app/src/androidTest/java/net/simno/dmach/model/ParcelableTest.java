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

package net.simno.dmach.model;

import android.os.Parcel;
import android.test.AndroidTestCase;

import net.simno.dmach.DMachActivity;

import java.util.ArrayList;
import java.util.List;

public class ParcelableTest extends AndroidTestCase {

    private Setting mSetting;
    private Channel mChannel1;
    private List<Channel> mChannels;
    private Patch mPatch;
    private int[] mSequence;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mSetting = new Setting("Pitch A", "Gain", .4f, .49f, 0, 7);

        mChannel1 = new Channel("bd", 0.5f);
        mChannel1.addSetting(new Setting("Pitch A", "Gain", .4f, .49f, 0, 7));
        mChannel1.addSetting(new Setting("Low-pass", "Square", .7f, 0, 5, 3));
        mChannel1.addSetting(new Setting("Pitch B", "Curve Time", .4f, .4f, 1, 2));
        mChannel1.addSetting(new Setting("Decay", "Noise Level", .49f, .7f, 6, 4));

        Channel mChannel2 = new Channel("sd", 0.5f);
        mChannel2.addSetting(new Setting("Pitch", "Gain", .49f, .45f, 0, 9));
        mChannel2.addSetting(new Setting("Low-pass", "Noise", .6f, .8f, 7, 1));
        mChannel2.addSetting(new Setting("X-fade", "Attack", .35f, .55f, 8, 6));
        mChannel2.addSetting(new Setting("Decay", "Body Decay", .55f, .42f, 4, 5));
        mChannel2.addSetting(new Setting("Band-pass", "Band-pass Q", .7f, .6f, 2, 3));

        mChannels = new ArrayList<>();
        mChannels.add(mChannel1);
        mChannels.add(mChannel2);

        mSequence = new int[DMachActivity.GROUPS * DMachActivity.STEPS];
        String title = "title";
        int selectedChannel = 2;
        int tempo = 124;
        int swing = 31;
        mPatch = new Patch(title, mSequence, mChannels, selectedChannel, tempo, swing);
    }

    public void testSetting() throws Exception {
        Parcel parcel = Parcel.obtain();
        mSetting.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Setting parceledSetting = Setting.CREATOR.createFromParcel(parcel);
        assertEquals(mSetting, parceledSetting);
    }

    public void testChannel() throws Exception {
        Parcel parcel = Parcel.obtain();
        mChannel1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Channel parceledChannel = Channel.CREATOR.createFromParcel(parcel);
        assertEquals(mChannel1, parceledChannel);
    }

    public void testPatch() throws Exception {
        String channelsJson = Patch.channelsToJson(mPatch.getChannels());
        String sequenceJson = Patch.sequenceToJson(mPatch.getSequence());

        Parcel parcel = Parcel.obtain();
        mPatch.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Patch parceledPatch = Patch.CREATOR.createFromParcel(parcel);
        assertEquals(mPatch, parceledPatch);

        mPatch.setChannels(channelsJson);
        assertEquals(mChannels, mPatch.getChannels());

        assertEquals(mSequence, mPatch.getSequence());
        mPatch.setSequence(sequenceJson);
    }
}
