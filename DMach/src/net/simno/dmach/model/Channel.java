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

package net.simno.dmach.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public final class Channel implements Parcelable {
    private String mName;
    private List<Setting> mSettings;
    private int mSelectedSetting;

    public Channel(String name) {
        mName = name;
        mSettings = new ArrayList<Setting>();
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
    
    public int getSelection() {
        return mSelectedSetting;
    }

    public void selectSetting(int index) {
        mSelectedSetting = index;
    }
    
    public Setting getSelectedSetting() {
        return mSettings.get(mSelectedSetting);
    }

    public int getCount() {
        return mSettings.size();
    }

    public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {
        /**
         * Return a new patch from the data in the specified parcel.
         */
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        /**
         * Return an array of patches of the specified size.
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
     * Write this patch to the specified parcel. To restore a patch from
     * a parcel, use readFromParcel()
     *
     * @param out The parcel to write the patch's settings and selection into
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeList(mSettings);
        out.writeInt(mSelectedSetting);
    }

    /**
     * Set the patch's settings and selection from the data stored in the specified
     * parcel. To write a patch to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the patch's settings and selection from
     */
    private void readFromParcel(Parcel in) {
        mName = in.readString();
        in.readList(mSettings, Setting.class.getClassLoader());
        mSelectedSetting = in.readInt();
    }
}
