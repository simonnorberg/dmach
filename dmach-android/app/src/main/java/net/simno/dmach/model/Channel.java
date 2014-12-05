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

import java.util.ArrayList;
import java.util.List;

/**
 * The Channel class represents a channel in dmach.pd
 */
public final class Channel implements Parcelable {
    private String mName;
    private List<Setting> mSettings;
    private int mSelectedSetting; // Default selection is 0
    private float mPan;

    /**
     *
     * @param name  Channel name that must exist in dmach.pd
     * @param pan  Pan position
     */
    public Channel(String name, float pan) {
        mName = name;
        mPan = pan;
        mSettings = new ArrayList<>();
    }

    public Channel(Parcel in) {
        readFromParcel(in);
    }

    public void addSetting(Setting s) {
        mSettings.add(s);
    }

    public String getName() {
        return mName;
    }

    public float getPan() {
        return mPan;
    }

    public void setPan(float pan) {
        mPan = pan;
    }

    public int getSelection() {
        return mSelectedSetting;
    }

    public void selectSetting(int selection) {
        mSelectedSetting = selection;
    }

    public Setting getSetting() {
        return mSettings.get(mSelectedSetting);
    }

    public List<Setting> getSettings() {
        return mSettings;
    }

    public int getCount() {
        return mSettings.size();
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        /**
         * Return a new channel from the data in the specified parcel.
         */
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        /**
         * Return an array of channels of the specified size.
         */
        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this channel to the specified parcel. To restore a channel from
     * a parcel, use readFromParcel()
     *
     * @param out The parcel to write the channel's settings and selection into
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeFloat(mPan);
        out.writeList(mSettings);
        out.writeInt(mSelectedSetting);
    }

    /**
     * Set the channel's settings and selection from the data stored in the specified
     * parcel. To write a channel to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the patch's settings and selection from
     */
    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mPan = in.readFloat();
        mSettings = new ArrayList<>();
        in.readList(mSettings, Setting.class.getClassLoader());
        mSelectedSetting = in.readInt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Channel)) {
            return false;
        }

        Channel channel = (Channel) o;

        if (mName != null ? !mName.equals(channel.mName) : channel.mName != null) {
            return false;
        }
        if (mSettings != null ? !mSettings.equals(channel.mSettings) : channel.mSettings != null) {
            return false;
        }
        if (mSelectedSetting != channel.mSelectedSetting) {
            return false;
        }
        if (Float.compare(channel.mPan, mPan) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mSettings != null ? mSettings.hashCode() : 0);
        result = 31 * result + mSelectedSetting;
        result = 31 * result + (mPan != +0.0f ? Float.floatToIntBits(mPan) : 0);
        return result;
    }
}
