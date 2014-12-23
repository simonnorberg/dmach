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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.parceler.Parcel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The Patch class contains all variables used in dmach.pd
 */
@Parcel
public class Patch {

    private static final Type CHANNEL_TYPE = new TypeToken<ArrayList<Channel>>() {}.getType();

    String mTitle;
    int[] mSequence;
    List<Channel> mChannels;
    int mSelectedChannel;
    int mTempo;
    int mSwing;

    public Patch() {
    }

    /**
     *
     * @param title  Title of the patch, unique in the database
     * @param sequence  The sequence that is used in dmach.pd
     * @param channels  List of channels in the patch
     * @param selectedChannel  The selected channel index
     * @param tempo  The tempo in BPM that is used in dmach.pd
     * @param swing  The swing value that is used in dmach.pd
     */
    public Patch(String title, int[] sequence, List<Channel> channels, int selectedChannel,
                 int tempo, int swing) {
        mTitle = title;
        mSequence = sequence;
        mChannels = channels;
        mSelectedChannel = selectedChannel;
        mTempo = tempo;
        mSwing = swing;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int[] getSequence() {
        return mSequence;
    }

    void setSequence(int[] sequence) {
        mSequence = sequence;
    }

    public void setSequence(String json) {
        setSequence(jsonToSequence(json));
    }

    public List<Channel> getChannels() {
        return mChannels;
    }

    void setChannels(List<Channel> channels) {
        mChannels = channels;
    }

    public void setChannels(String json) {
        setChannels(jsonToChannels(json));
    }

    public int getSelectedChannel() {
        return mSelectedChannel;
    }

    public void setSelectedChannel(int selectedChannel) {
        mSelectedChannel = selectedChannel;
    }

    public int getTempo() {
        return mTempo;
    }

    public void setTempo(int tempo) {
        mTempo = tempo;
    }

    public int getSwing() {
        return mSwing;
    }

    public void setSwing(int swing) {
        mSwing = swing;
    }

    public static String sequenceToJson(int[] sequence) {
        return new Gson().toJson(sequence);
    }

    public static String channelsToJson(List<Channel> channels) {
        return new Gson().toJson(channels);
    }

    public static int[] jsonToSequence(String json) {
        return new Gson().fromJson(json, int[].class);
    }

    public static List<Channel> jsonToChannels(String json) {
        return new Gson().fromJson(json, CHANNEL_TYPE);
    }
}
