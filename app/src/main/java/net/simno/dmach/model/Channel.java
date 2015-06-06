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

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * The Channel class represents a channel in dmach.pd
 */
@Parcel
public final class Channel {
    String name;
    List<Setting> settings;
    int selectedSetting; // Default 0
    float pan;

    public Channel() {
    }

    /**
     *  @param name  Channel name that must exist in dmach.pd
     *
     */
    public Channel(String name) {
        this.name = name;
        pan = 0.5f;
        settings = new ArrayList<>();
    }

    private void addSetting(Setting s) {
        settings.add(s);
    }

    public String getName() {
        return name;
    }

    public float getPan() {
        return pan;
    }

    public void setPan(float pan) {
        this.pan = pan;
    }

    public int getSelection() {
        return selectedSetting;
    }

    public void selectSetting(int selection) {
        selectedSetting = selection;
    }

    public Setting getSetting() {
        return settings.get(selectedSetting);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public int getCount() {
        return settings.size();
    }

    public static ArrayList<Channel> createChannels() {
        // Hardcoded values equal to the pd file
        ArrayList<Channel> channels = new ArrayList<>();

        Channel bd = new Channel("bd");
        bd.addSetting(new Setting("Pitch A", "Gain", .4f, .49f, 0, 7));
        bd.addSetting(new Setting("Low-pass", "Square", .7f, 0, 5, 3));
        bd.addSetting(new Setting("Pitch B", "Curve Time", .4f, .4f, 1, 2));
        bd.addSetting(new Setting("Decay", "Noise Level", .49f, .7f, 6, 4));
        channels.add(bd);

        Channel sd = new Channel("sd");
        sd.addSetting(new Setting("Pitch", "Gain", .49f, .45f, 0, 9));
        sd.addSetting(new Setting("Low-pass", "Noise", .6f, .8f, 7, 1));
        sd.addSetting(new Setting("X-fade", "Attack", .35f, .55f, 8, 6));
        sd.addSetting(new Setting("Decay", "Body Decay", .55f, .42f, 4, 5));
        sd.addSetting(new Setting("Band-pass", "Band-pass Q", .7f, .6f, 2, 3));
        channels.add(sd);

        Channel cp = new Channel("cp");
        cp.addSetting(new Setting("Pitch", "Gain", .55f, .3f, 0, 7));
        cp.addSetting(new Setting("Delay 1", "Delay 2", .3f, .3f, 4, 5));
        cp.addSetting(new Setting("Decay", "Filter Q", .59f, .2f, 6, 1));
        cp.addSetting(new Setting("Filter 1", "Filter 2", .9f, .15f, 2, 3));
        channels.add(cp);

        Channel tt = new Channel("tt");
        tt.addSetting(new Setting("Pitch", "Gain", .49f, .49f, 0, 1));
        channels.add(tt);

        Channel cb = new Channel("cb");
        cb.addSetting(new Setting("Pitch", "Gain", .3f, .49f, 0, 5));
        cb.addSetting(new Setting("Decay 1", "Decay 2", .1f, .75f, 1, 2));
        cb.addSetting(new Setting("Vcf", "Vcf Q", .3f, 0, 3, 4));
        channels.add(cb);

        Channel hh = new Channel("hh");
        hh.addSetting(new Setting("Pitch", "Gain", .45f, .4f, 0, 11));
        hh.addSetting(new Setting("Low-pass", "Snap", .8f, .1f, 10, 5));
        hh.addSetting(new Setting("Noise Pitch", "Noise", .55f, .6f, 4, 3));
        hh.addSetting(new Setting("Ratio B", "Ratio A", .9f, 1, 2, 1));
        hh.addSetting(new Setting("Release", "Attack", .55f, .4f, 7, 6));
        hh.addSetting(new Setting("Filter", "Filter Q", .7f, .6f, 8, 9));
        channels.add(hh);

        return channels;
    }
}
