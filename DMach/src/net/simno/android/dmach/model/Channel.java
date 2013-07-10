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

public final class Channel implements Parcelable {
	private String name;
	private Patch patch;
	private boolean[] sequence;
	
	public Channel(String name, Patch patch, boolean[] sequence) {
		this.name = name;
		this.patch = new Patch(patch);
		this.sequence = new boolean[sequence.length];
		for (int i = 0; i < sequence.length; ++i) {
			this.sequence[i] = sequence[i];
		}
	} 
	
	public Channel(Parcel in) {
		readFromParcel(in);
	}

	public String getName() {
		return name;
	}
	
	public Patch getPatch() {
		return patch;
	}
	
	public boolean[] getSequence() {
		boolean[] result = new boolean[sequence.length];
		for (int i = 0; i < sequence.length; ++i) {
			result[i] = sequence[i];
		}
		return result;
	}
	
	public boolean getStep(int index) {
		return sequence[index];
	}
	
	public void setStep(int index, boolean status) {
		sequence[index] = status;
	}
	
	public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {

		@Override
		public Channel createFromParcel(Parcel in) {
			return new Channel(in);
		}

		@Override
		public Channel[] newArray(int size) {
			return new Channel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}	

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeParcelable(patch, flags);
		out.writeBooleanArray(sequence);
	}
	
	private void readFromParcel(Parcel in) {
		name = in.readString();
		patch = in.readParcelable(Patch.class.getClassLoader());
		in.readBooleanArray(sequence);
	}
}