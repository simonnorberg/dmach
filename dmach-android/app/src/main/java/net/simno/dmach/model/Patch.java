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
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.simno.dmach.DMachActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Patch class contains all variables used in dmach.pd
 */
public class Patch implements Parcelable {

    private static Type CHANNEL_TYPE = new TypeToken<ArrayList<Channel>>() {}.getType();

    private String mTitle;
    private int[] mSequence;
    private List<Channel> mChannels;
    private int mSelectedChannel;
    private int mTempo;
    private int mSwing;

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

    public void setSequence(String json) {
        setSequence(jsonToSequence(json));
    }

    public List<Channel> getChannels() {
        return mChannels;
    }

    public void setChannels(List<Channel> channels) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Patch)) {
            return false;
        }

        Patch patch = (Patch) o;

        if (mTitle != null ? !mTitle.equals(patch.mTitle) : patch.mTitle != null) {
            return false;
        }
        if (!Arrays.equals(mSequence, patch.mSequence)) {
            return false;
        }
        if (mChannels != null ? !mChannels.equals(patch.mChannels) : patch.mChannels != null) {
            return false;
        }
        if (mSelectedChannel != patch.mSelectedChannel) {
            return false;
        }
        if (mTempo != patch.mTempo) {
            return false;
        }
        if (mSwing != patch.mSwing) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (mTitle != null ? mTitle.hashCode() : 0);
        result = 31 * result + (mSequence != null ? Arrays.hashCode(mSequence) : 0);
        result = 31 * result + (mChannels != null ? mChannels.hashCode() : 0);
        result = 31 * result + mSelectedChannel;
        result = 31 * result + mTempo;
        result = 31 * result + mSwing;
        return result;
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
