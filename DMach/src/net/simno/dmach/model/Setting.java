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

public final class Setting implements Parcelable {
    public String hText;
    public String vText;
    public float x;
    public float y;
    public int hIndex;
    public int vIndex;

    public Setting(String hText, String vText, float x, float y, int hIndex, int vIndex) {
        this.hText = hText;
        this.vText = vText;
        this.x = x;
        this.y = y;
        this.hIndex = hIndex;
        this.vIndex = vIndex;
    }

    public Setting(Parcel in) {
        readFromParcel(in);
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static final Parcelable.Creator<Setting> CREATOR = new Parcelable.Creator<Setting>() {
        /**
         * Return a new setting from the data in the specified parcel.
         */
        @Override
        public Setting createFromParcel(Parcel in) {
            return new Setting(in);
        }

        /**
         * Return an array of settings of the specified size.
         */
        @Override
        public Setting[] newArray(int size) {
            return new Setting[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this setting to the specified parcel. To restore a setting from
     * a parcel, use readFromParcel()
     *
     * @param out The parcel to write the settings's names and values into
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(hText);
        out.writeString(vText);
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeInt(hIndex);
        out.writeInt(vIndex);
    }

    /**
     * Set the setting's names and values from the data stored in the specified
     * parcel. To write a setting to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the settings's names and values from
     */
    private void readFromParcel(Parcel in) {
        hText = in.readString();
        vText = in.readString();
        x = in.readFloat();
        y = in.readFloat();
        hIndex = in.readInt();
        vIndex = in.readInt();
    }
}