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

import android.os.Parcelable;
import android.test.AndroidTestCase;

import net.simno.dmach.DMachActivity;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ParcelableTest extends AndroidTestCase {

    private Setting mSetting;
    private Channel mChannel1;
    private Patch mPatch;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mSetting = new Setting("Pitch A", "Gain", .4f, .49f, 0, 7);

        mChannel1 = new Channel("bd");
        mChannel1.addSetting(new Setting("Pitch A", "Gain", .4f, .49f, 0, 7));
        mChannel1.addSetting(new Setting("Low-pass", "Square", .7f, 0, 5, 3));
        mChannel1.addSetting(new Setting("Pitch B", "Curve Time", .4f, .4f, 1, 2));
        mChannel1.addSetting(new Setting("Decay", "Noise Level", .49f, .7f, 6, 4));

        Channel mChannel2 = new Channel("sd");
        mChannel2.addSetting(new Setting("Pitch", "Gain", .49f, .45f, 0, 9));
        mChannel2.addSetting(new Setting("Low-pass", "Noise", .6f, .8f, 7, 1));
        mChannel2.addSetting(new Setting("X-fade", "Attack", .35f, .55f, 8, 6));
        mChannel2.addSetting(new Setting("Decay", "Body Decay", .55f, .42f, 4, 5));
        mChannel2.addSetting(new Setting("Band-pass", "Band-pass Q", .7f, .6f, 2, 3));

        List<Channel> mChannels = new ArrayList<>();
        mChannels.add(mChannel1);
        mChannels.add(mChannel2);

        int[] mSequence = new int[DMachActivity.GROUPS * DMachActivity.STEPS];
        String title = "title";
        int selectedChannel = 2;
        int tempo = 124;
        int swing = 31;
        mPatch = new Patch(title, mSequence, mChannels, selectedChannel, tempo, swing);
    }

    public void testSetting() throws Exception {
        Parcelable parcelable = Parcels.wrap(mSetting);
        Setting parceledSetting = Parcels.unwrap(parcelable);
        assertEquals(mSetting, parceledSetting);
    }

    public void testChannel() throws Exception {
        Parcelable parcelable = Parcels.wrap(mChannel1);
        Channel parceledChannel = Parcels.unwrap(parcelable);
        assertEquals(mChannel1, parceledChannel);
    }

    public void testPatch() throws Exception {
        final String channelsJson = Patch.channelsToJson(mPatch.getChannels());
        final String sequenceJson = Patch.sequenceToJson(mPatch.getSequence());

        Parcelable parcelable = Parcels.wrap(mPatch);
        Patch parceledPatch = Parcels.unwrap(parcelable);
        assertEquals(mPatch, parceledPatch);

        mPatch.setChannels(channelsJson);
        mPatch.setSequence(sequenceJson);
        assertEquals(channelsJson, Patch.channelsToJson(mPatch.getChannels()));
        assertEquals(sequenceJson, Patch.sequenceToJson(mPatch.getSequence()));
    }
}
