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

package net.simno.android.dmach.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Patch holds a list of settings and knows which setting is selected.
 */
public final class Patch implements Parcelable {
    private List<Setting> mSettings;
    private int mSelectedSettingIndex;

    public Patch() {
        this(0, new ArrayList<Setting>());
    }

    public Patch(Patch p) {
        mSelectedSettingIndex = p.getSelectedSettingIndex();
        mSettings = new ArrayList<Setting>(p.getCount());
        for (int i = 0; i < p.getCount(); ++i) {
            mSettings.add(new Setting(p.getHText(i), p.getVText(i), p.getPos(i)));
        }
    }

    public Patch(int selectedSettingIndex, List<Setting> settings) {
        mSelectedSettingIndex = selectedSettingIndex;
        mSettings = new ArrayList<Setting>(settings.size());
        for (Setting s : settings) {
            mSettings.add(new Setting(s));
        }
    }

    public Patch(Parcel in) {
        readFromParcel(in);
    }

    public void addSetting(Setting s) {
        mSettings.add(new Setting(s));
    }

    public int getSelectedSettingIndex() {
        return mSelectedSettingIndex;
    }

    public void setSelectedSettingIndex(int selectedSettingIndex) {
        mSelectedSettingIndex = selectedSettingIndex;
    }

    public PointF getPos(int index) {
        return mSettings.get(index).getPos();
    }

    public void setPos(int index, PointF pos) {
        mSettings.get(index).setPos(pos);
    }

    public PointF getSelectedPos() {
        return getPos(mSelectedSettingIndex);
    }

    public void setSelectedPos(PointF pos) {
        setPos(mSelectedSettingIndex, pos);
    }

    private String getVText(int index) {
        return mSettings.get(index).getVText();
    }

    private String getHText(int index) {
        return mSettings.get(index).getHText();
    }

    public String getSelectedHText() {
        return getHText(mSelectedSettingIndex);
    }

    public String getSelectedVText() {
        return getVText(mSelectedSettingIndex);
    }

    public int getCount() {
        return mSettings.size();
    }

    public static final Parcelable.Creator<Patch> CREATOR = new Parcelable.Creator<Patch>() {
        /**
         * Return a new patch from the data in the specified parcel.
         */
        @Override
        public Patch createFromParcel(Parcel in) {
            return new Patch(in);
        }

        /**
         * Return an array of patches of the specified size.
         */
        @Override
        public Patch[] newArray(int size) {
            return new Patch[size];
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
        out.writeList(mSettings);
        out.writeInt(mSelectedSettingIndex);
    }

    /**
     * Set the patch's settings and selection from the data stored in the specified
     * parcel. To write a patch to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the patch's settings and selection from
     */
    private void readFromParcel(Parcel in) {
        in.readList(mSettings, Setting.class.getClassLoader());
        mSelectedSettingIndex = in.readInt();
    }
}