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
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.simno.dmach.DMachActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Patch implements Parcelable {

    private String mTitle;
    private int[] mSequence;
    private List<Channel> mChannels;
    private int mSelectedChannel;
    private int mTempo;
    private int mSwing;
    private Type mChannelsType = new TypeToken<ArrayList<Channel>>() {}.getType();

    public Patch() {
    }

    public Patch(String title, int[] sequence, List<Channel> channels, int selectedChannel,
                 int tempo, int swing) {
        mTitle = title;
        mSequence = sequence;
        mChannels = channels;
        mSelectedChannel = selectedChannel;
        mTempo = tempo;
        mSwing = swing;
    }

    public Patch(Parcel in) {
        readFromParcel(in);
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

    public void setSequence(int[] sequence) {
        mSequence = sequence;
    }

    public String getSequenceAsJson() {
        return new Gson().toJson(mSequence);
    }

    public void setSequenceFromJson(String sequenceJson) {
        mSequence = new Gson().fromJson(sequenceJson, int[].class);
    }

    public List<Channel> getChannels() {
        return mChannels;
    }

    public void setChannels(List<Channel> channels) {
        mChannels = channels;
    }

    public String getChannelsAsJson() {
        return new Gson().toJson(mChannels);
    }

    public void setChannelsFromJson(String channelsJson) {
        mChannels = new Gson().fromJson(channelsJson, mChannelsType);
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

    public static final Creator<Patch> CREATOR = new Creator<Patch>() {
        @Override
        public Patch createFromParcel(Parcel in) {
            return new Patch(in);
        }

        @Override
        public Patch[] newArray(int size) {
            return new Patch[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mTitle);
        out.writeIntArray(mSequence);
        out.writeList(mChannels);
        out.writeInt(mSelectedChannel);
        out.writeInt(mTempo);
        out.writeInt(mSwing);
    }

    private void readFromParcel(Parcel in) {
        mTitle = in.readString();
        mSequence = new int[DMachActivity.GROUPS * DMachActivity.STEPS];
        in.readIntArray(mSequence);
        mChannels = new ArrayList<Channel>();
        in.readList(mChannels, Channel.class.getClassLoader());
        mSelectedChannel = in.readInt();
        mTempo = in.readInt();
        mSwing = in.readInt();
    }
}
