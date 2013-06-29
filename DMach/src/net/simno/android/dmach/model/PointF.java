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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 * 
 */

package net.simno.android.dmach.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class PointF implements Parcelable {
	private float x;
	private float y;
	
	public PointF() {
		this(0, 0);
	}
	
	public PointF(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public PointF(PointF point) {
		this(point.getX(), point.getY());
	}

	public PointF(Parcel in) {
		readFromParcel(in);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
	
	public String toString() {
		return "x: " + getX() + ", y: " + getY(); 
	}

	public static final Parcelable.Creator<PointF> CREATOR = new Parcelable.Creator<PointF>() {

		@Override
		public PointF createFromParcel(Parcel in) {
			return new PointF(in);
		}

		@Override
		public PointF[] newArray(int size) {
			return new PointF[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeFloat(x);
		out.writeFloat(y);
	}
	
	private void readFromParcel(Parcel in) {
		x = in.readFloat();
		y = in.readFloat();
	}
}