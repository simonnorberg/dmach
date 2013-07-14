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

/**
 * Channel holds a name, a patch and a boolean sequence.
 */
public final class Channel implements Parcelable {
    private String mName;
    private Patch mPatch;
    private boolean[] mSequence;

    public Channel(String name, Patch patch, boolean[] sequence) {
        mName = name;
        mPatch = new Patch(patch);
        mSequence = new boolean[sequence.length];
        for (int i = 0; i < sequence.length; ++i) {
            mSequence[i] = sequence[i];
        }
    }

    public Channel(Parcel in) {
        readFromParcel(in);
    }

    public String getName() {
        return mName;
    }

    public Patch getPatch() {
        return mPatch;
    }

    public boolean[] getSequence() {
        boolean[] result = new boolean[mSequence.length];
        for (int i = 0; i < mSequence.length; ++i) {
            result[i] = mSequence[i];
        }
        return result;
    }

    public boolean getStep(int index) {
        return mSequence[index];
    }

    public void setStep(int index, boolean status) {
        mSequence[index] = status;
    }

    public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {
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
     * @param out The parcel to write the channel's name, patch and sequence into
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeParcelable(mPatch, flags);
        out.writeBooleanArray(mSequence);
    }

    /**
     * Set the channel's name, patch and sequence from the data stored in the specified
     * parcel. To write a patch to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the channel's name, patch and sequence from
     */
    private void readFromParcel(Parcel in) {
        mName = in.readString();
        mPatch = in.readParcelable(Patch.class.getClassLoader());
        in.readBooleanArray(mSequence);
    }
}