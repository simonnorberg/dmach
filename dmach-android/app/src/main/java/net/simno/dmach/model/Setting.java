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

/**
 * The Setting class represents two parameters of a channel in dmach.pd
 */
public final class Setting implements Parcelable {
    private String hText;
    private String vText;
    private float x;
    private float y;
    private int hIndex;
    private int vIndex;

    /**
     *
     * @param hText  Text to be displayed horizontally
     * @param vText  Text to be displayed vertically
     * @param x  Horizontal value
     * @param y  Vertical value
     * @param hIndex  Index of the parameter to change in the Pure Data patch
     * @param vIndex  Index of the parameter to change in the Pure Data patch
     */
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

    public String getHText() {
        return hText;
    }

    public String getVText() {
        return vText;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getHIndex() {
        return hIndex;
    }

    public int getVIndex() {
        return vIndex;
    }

    public static final Creator<Setting> CREATOR = new Creator<Setting>() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Setting)) {
            return false;
        }

        Setting setting = (Setting) o;

        if (hText != null ? !hText.equals(setting.hText) : setting.hText != null) {
            return false;
        }
        if (vText != null ? !vText.equals(setting.vText) : setting.vText != null) {
            return false;
        }
        if (Float.compare(setting.x, x) != 0) {
            return false;
        }
        if (Float.compare(setting.y, y) != 0) {
            return false;
        }
        if (hIndex != setting.hIndex) {
            return false;
        }
        if (vIndex != setting.vIndex) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (hText != null ? hText.hashCode() : 0);
        result = 31 * result + (vText != null ? vText.hashCode() : 0);
        result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + hIndex;
        result = 31 * result + vIndex;
        return result;
    }
}
