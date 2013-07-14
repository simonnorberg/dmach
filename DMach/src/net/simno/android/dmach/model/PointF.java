/**
 * Copyright (C) 2007 The Android Open Source Project
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
 * PointF holds two float coordinates
 */
public final class PointF implements Parcelable {
    private float mX;
    private float mY;

    public PointF() {}

    public PointF(float x, float y) {
        this.mX = x;
        this.mY = y;
    }

    public PointF(PointF point) {
        this(point.getX(), point.getY());
    }

    public PointF(Parcel in) {
        readFromParcel(in);
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    @Override
    public String toString() {
        return "x: " + getX() + ", y: " + getY();
    }

    public static final Parcelable.Creator<PointF> CREATOR = new Parcelable.Creator<PointF>() {
        /**
         * Return a new point from the data in the specified parcel.
         */
        @Override
        public PointF createFromParcel(Parcel in) {
            return new PointF(in);
        }

        /**
         * Return an array of points of the specified size.
         */
        @Override
        public PointF[] newArray(int size) {
            return new PointF[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this point to the specified parcel. To restore a point from
     * a parcel, use readFromParcel()
     *
     * @param out The parcel to write the point's coordinates into
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(mX);
        out.writeFloat(mY);
    }

    /**
     * Set the point's coordinates from the data stored in the specified
     * parcel. To write a point to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the point's coordinates from
     */
    private void readFromParcel(Parcel in) {
        mX = in.readFloat();
        mY = in.readFloat();
    }
}